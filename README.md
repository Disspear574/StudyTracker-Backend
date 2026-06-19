# StudyTracker Backend

Бэкенд трекера учебных задач. Kotlin · Ktor 3.5 (Netty) · Exposed · PostgreSQL · Koin · MinIO.
Архитектура — Clean/Hexagonal по фичам (модульный монолит). Полный спек: см. design-doc.

## Стек
- **Ktor 3.5** (Netty) — HTTP-слой
- **Exposed + HikariCP + PostgreSQL** — данные, **Flyway** — миграции
- **Koin** — DI · **JWT** (access + refresh) — auth · **BCrypt** — пароли
- **MinIO** — аватары + бэкапы
- **Micrometer/Prometheus + Logback (JSON)** — наблюдаемость
- **Testcontainers + ktor-server-test-host** — тесты

## Запуск (локально)
```bash
./gradlew run        # поднимет сервер на :8080
curl localhost:8080/health
```

## API-документация
Swagger UI — `GET /swagger` (интерактивная OpenAPI-спека из `src/main/resources/openapi/documentation.yaml`).
Прод: `https://studytracker-api.disspear574.ru/swagger`. Ручной прогон — `http/studytracker.http`.

## Сборка / тесты
```bash
./gradlew build      # компиляция + тесты
./gradlew test
./gradlew installDist   # дистрибутив для рантайма (используется в Docker-образе)
```

## Статус (по фазам роадмапа)
- [x] Фаза 0 — скелет + `/health`
- [x] Фаза 1 — данные (Exposed 1.0 + Flyway `V1`, таблицы; H2 round-trip тест)
- [x] Фаза 2 — платформенные плагины (Koin, Micrometer/Prometheus `/metrics`, CallLogging+CallId, Compression, CORS, RateLimit)
- [x] Фаза 3 — security core (JWT access, BCrypt, единая error-модель, защита роутов)
- [x] Фаза 4 — auth + device info (логика + репозиторий + роуты, TDD)
- [x] Фаза 5 — профиль + аватар (MinIO presigned, TokenIssuer)
- [x] Фаза 6 — задачи (CRUD + роуты, owner-scope)
- [x] Фаза 4–6 web — DTO/роуты/валидация + Koin-проводка + MinIO/TokenIssuer
- [x] Фаза 7 — деплой под homelab (Proxmox+LXC): `Dockerfile`, `docker-compose.yml` (publish :8080),
  `deploy/traefik/studytracker.yaml` (file-провайдер), `DEPLOY.md`, `scripts/pg-backup.sh` (CI/CD — позже)

Деплой: `DEPLOY.md` (бэк → LXC 105 `192.168.2.243:8080`, Postgres LXC 104, MinIO `minio-api.disspear574.ru`,
домен `studytracker-api.disspear574.ru`). Прогон API: `http/studytracker.http`.

Тесты: **47 зелёных** — юзкейсы на фейках (TDD) + репозитории на H2 + **E2E роутов**
(register → JWT `/me` → задачи) + платформа/security.

### Hardening после adversarial-review (17 находок)
Исправлено: гонка ротации refresh (условный отзыв + reuse под гонкой), атомарный upsert
device-сессий, **атомарность register/login** (порт `Transactor`, проверено rollback-тестом на H2),
нормализация email, unique-violation → 409 (не 500), fail-fast на дефолтный `JWT_SECRET`,
валидация пустых имён в PATCH /me, проверка аватара на confirm (существование + image/*, через
`FileStorage.stat`), `replaced_by` → self-FK, UTC-offset в `accessExpiresAt`.
Отложено (с обоснованием): подпись content-type/size в presigned PUT (confirm-проверка закрывает
практический риск), auth на `/metrics` (внутренний, не публикуется через Traefik), clear для middleName.

> Локально нет Docker/Postgres → DB-код проверяется на H2 (`SchemaUtils`); прод-путь
> (Flyway на Postgres) + полный E2E — на CI/VPS через Testcontainers.

## Конфигурация
Все настройки — через env (см. `.env.example`), читаются в `application.conf` (HOCON).
