# StudyTracker Backend

Бэкенд для трекера учебных задач. Написан на Kotlin с Ktor, данные в PostgreSQL,
аватары и бэкапы лежат в MinIO.

Под капотом — модульный монолит, разбитый по фичам (auth, profile, tasks),
с привычным делением на data / domain / web слои.

## Стек

- Ktor 3.5 на Netty
- Exposed + HikariCP, миграции через Flyway
- Koin для DI
- JWT (access + refresh) для авторизации, BCrypt для паролей
- MinIO для файлов
- Micrometer/Prometheus и Logback (JSON-логи)
- Тесты на Testcontainers и ktor-server-test-host

## Запуск

```bash
./gradlew run
curl localhost:8080/health
```

Сервер поднимается на `:8080`. Все настройки берутся из переменных окружения —
смотри `.env.example`, оттуда они подхватываются в `application.conf`.

## Сборка и тесты

```bash
./gradlew build        # компиляция + тесты
./gradlew test
./gradlew installDist  # дистрибутив для рантайма, его же использует Docker-образ
```

## API

Swagger UI доступен на `GET /swagger` (спека — `src/main/resources/openapi/documentation.yaml`).

Готовый набор запросов для ручного прогона — `http/studytracker.http`.

## Что уже есть

- регистрация и логин с device info, ротация refresh-токенов
- профиль и загрузка аватара (presigned URL в MinIO)
- CRUD задач с привязкой к владельцу
- единая модель ошибок, rate limit, CORS, compression
- метрики на `/metrics`

Тесты покрывают юзкейсы (на фейках), репозитории (на H2) и E2E-сценарии роутов —
от регистрации и получения JWT до работы с задачами.

## Деплой

Приложение собирается в Docker-образ и запускается за reverse-proxy.
В репозитории есть `Dockerfile`, `docker-compose.yml`, пример конфига Traefik
(`deploy/traefik/`) и скрипт бэкапа БД (`scripts/pg-backup.sh`).

## Замечание про локальную разработку

Локально Docker и Postgres не используются, поэтому код работы с БД проверяется
на H2 через `SchemaUtils`. Полный путь с Flyway на Postgres и сквозные E2E-тесты
гоняются на CI/VPS через Testcontainers.
