# Деплой StudyTracker backend (homelab: Proxmox + LXC)

Топология (выяснено на месте):

| Роль | LXC | Адрес |
|---|---|---|
| Traefik (reverse-proxy, file-провайдер) | 102 | 192.168.2.126 |
| MinIO (S3) | 103 | внутр. 192.168.2.167:9000 · публ. **https://minio-api.disspear574.ru** |
| Postgres 16 | 104 | **192.168.2.238:5432** (listen `*`, pg_hba пускает 192.168.2.0/24) |
| **Docker host (сюда бэк)** | 105 | **192.168.2.243** (порт **8080** свободен) |

Бэк = docker-контейнер в LXC 105, публикует `:8080`; Traefik роутит `studytracker.disspear574.ru` → `http://192.168.2.243:8080`.

---

## 1. DNS
Создай A-запись `studytracker.disspear574.ru` → твой публичный IP (как у `react.`/`minio-api.`).
Нужно для Let's Encrypt (HTTP-challenge) и доступа.

## 2. Postgres (LXC 104) — создать БД и юзера
`listen_addresses` и `pg_hba` уже готовы (LAN+md5), нужно только завести БД/пользователя:
```bash
# на Proxmox-хосте:
pct exec 104 -- su postgres -c "psql -c \"CREATE USER studytracker WITH PASSWORD 'СГЕНЕРИРУЙ_СИЛЬНЫЙ';\""
pct exec 104 -- su postgres -c "psql -c \"CREATE DATABASE studytracker OWNER studytracker;\""
```
Схему `V1` накатит Flyway на старте (юзер — владелец БД, прав на public-схему хватает).

## 3. MinIO
- В MinIO-консоли (`https://minio.disspear574.ru`) заведи access/secret key (или возьми существующий
  с правами на создание бакетов). Бакет `studytracker-avatars` приложение создаст само (`ensureBucket`),
  либо создай заранее.

## 4. Приложение (LXC 105)
Вариант А — собрать прямо в LXC 105 (зайди в его консоль/SSH):
```bash
git clone <repo> studytracker && cd studytracker   # или scp файлов
cp .env.example .env
# заполни: JWT_SECRET (openssl rand -base64 48), DB_PASSWORD (из шага 2), MINIO_ACCESS_KEY/SECRET_KEY
docker compose build
docker compose up -d
docker compose logs -f studytracker-backend         # увидишь Flyway V1 + старт Netty на :8080
```
Вариант Б — со своего ноута через docker-контекст `homelab-docker` (ssh://192.168.2.243):
`docker --context homelab-docker compose up -d` (файлы должны быть на 105).

Проверка локально на хосте: `curl http://192.168.2.243:8080/health` → `{"status":"UP",...}`.

## 5. Traefik (LXC 102) — добавить роут
```bash
# с Proxmox-хоста скопируй файл в conf.d (watch=true перечитает сам):
pct push 102 deploy/traefik/studytracker.yaml /etc/traefik/conf.d/studytracker.yaml
# либо вручную создай /etc/traefik/conf.d/studytracker.yaml с содержимым из репо.
```

## 6. Проверка
```bash
curl https://studytracker.disspear574.ru/health
```
Полный прогон API — `http/studytracker.http` (поставь `@host = https://studytracker.disspear574.ru`).
Аватары: presigned-ссылки будут на `https://minio-api.disspear574.ru` — открываются и снаружи.

## Бэкапы
`scripts/pg-backup.sh` (cron на Proxmox-хосте): `pct exec 104 pg_dump` → MinIO. Настрой mc-алиас.

## Откат
Тегируй образ по git-sha; откат = собрать/запустить предыдущий тег. Миграции аддитивные (V1).
