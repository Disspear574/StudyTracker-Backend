package com.studytracker

import com.studytracker.feature.user.domain.model.User
import com.studytracker.feature.user.domain.port.NewUser
import com.studytracker.feature.user.domain.port.ProfilePatch
import com.studytracker.feature.user.domain.usecase.ConfirmAvatarUseCase
import com.studytracker.feature.user.domain.usecase.DeleteAccountUseCase
import com.studytracker.feature.user.domain.usecase.GetMeUseCase
import com.studytracker.feature.user.domain.usecase.IssueAvatarUploadUrlUseCase
import com.studytracker.feature.user.domain.usecase.UpdateProfileUseCase
import com.studytracker.shared.error.AppException
import com.studytracker.support.FakeFileStorage
import com.studytracker.support.FakeUserRepository
import com.studytracker.support.MutableTimeProvider
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserUseCasesTest {
    private val time = MutableTimeProvider()
    private val users = FakeUserRepository()
    private val storage = FakeFileStorage()

    private val getMe = GetMeUseCase(users)
    private val updateProfile = UpdateProfileUseCase(users, time)
    private val deleteAccount = DeleteAccountUseCase(users, storage)
    private val issueAvatarUrl = IssueAvatarUploadUrlUseCase(storage, time)
    private val confirmAvatar = ConfirmAvatarUseCase(users, storage, time)

    private suspend fun seedUser(email: String = "a@b.c"): User =
        users.create(NewUser(email, "enc:x", "Иван", "Иванов", "Иванович"), time.now())

    @Test
    fun `getMe returns the user`() = runBlocking {
        val user = seedUser()
        assertEquals(user.id, getMe(user.id).id)
    }

    @Test
    fun `getMe with unknown id throws NotFound`() = runBlocking {
        assertFailsWith<AppException.NotFound> { getMe(UUID.randomUUID()) }
        Unit
    }

    @Test
    fun `updateProfile changes only provided fields`() = runBlocking {
        val user = seedUser()
        val updated = updateProfile(user.id, ProfilePatch(firstName = "Пётр"))
        assertEquals("Пётр", updated.firstName)
        assertEquals("Иванов", updated.lastName)
    }

    @Test
    fun `updateProfile to an email used by another user throws Conflict`() = runBlocking {
        val a = seedUser("a@b.c")
        seedUser("b@b.c")
        assertFailsWith<AppException.Conflict> { updateProfile(a.id, ProfilePatch(email = "b@b.c")) }
        Unit
    }

    @Test
    fun `deleteAccount deletes the user and removes its avatar from storage`() = runBlocking {
        val user = seedUser()
        users.updateAvatarKey(user.id, "avatars/${user.id}/pic", time.now())

        deleteAccount(user.id)

        assertNull(users.findById(user.id))
        assertTrue(storage.deleted.contains("avatars/${user.id}/pic"))
    }

    @Test
    fun `issueAvatarUploadUrl returns a key scoped to the user and a put url`() = runBlocking {
        val user = seedUser()
        val target = issueAvatarUrl(user.id, "image/png")
        assertTrue(target.objectKey.startsWith("avatars/${user.id}/"), "key must be scoped to the user")
        assertTrue(target.uploadUrl.contains(target.objectKey))
    }

    @Test
    fun `issueAvatarUploadUrl rejects non-image content type`() = runBlocking {
        val user = seedUser()
        assertFailsWith<AppException.Validation> { issueAvatarUrl(user.id, "application/pdf") }
        Unit
    }

    @Test
    fun `confirmAvatar sets the avatar key when it belongs to the user`() = runBlocking {
        val user = seedUser()
        val updated = confirmAvatar(user.id, "avatars/${user.id}/pic.png")
        assertEquals("avatars/${user.id}/pic.png", updated.avatarKey)
    }

    @Test
    fun `confirmAvatar rejects a key belonging to another user`() = runBlocking {
        val user = seedUser()
        val otherId = UUID.randomUUID()
        assertFailsWith<AppException.Forbidden> { confirmAvatar(user.id, "avatars/$otherId/pic.png") }
        Unit
    }

    @Test
    fun `confirmAvatar rejects a key that was never uploaded`() = runBlocking {
        val user = seedUser()
        storage.nextStat = null
        assertFailsWith<AppException.Validation> { confirmAvatar(user.id, "avatars/${user.id}/missing.png") }
        Unit
    }

    @Test
    fun `confirmAvatar rejects a non-image object`() = runBlocking {
        val user = seedUser()
        storage.nextStat = com.studytracker.shared.storage.StoredObject("application/pdf", 10)
        assertFailsWith<AppException.Validation> { confirmAvatar(user.id, "avatars/${user.id}/doc.pdf") }
        Unit
    }
}
