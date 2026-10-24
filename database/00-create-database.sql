-- Run this ONCE as the postgres superuser if you installed PostgreSQL locally
-- (skip it if you use docker-compose, which does this for you).
--   psql -U postgres -f database/00-create-database.sql
CREATE USER stms_user WITH PASSWORD 'stms_pass';
CREATE DATABASE stms_db OWNER stms_user;
GRANT ALL PRIVILEGES ON DATABASE stms_db TO stms_user;
