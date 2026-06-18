package com.studytracker.shared.ports

import com.fasterxml.uuid.Generators
import java.util.UUID

object UuidV7 {
    private val generator = Generators.timeBasedEpochGenerator()

    fun next(): UUID = generator.generate()
}
