-- =========================================
-- Migration: Schema Inicial
-- Data: 2026-02-03
-- Descrição: Criação das tabelas base users, refresh_tokens e notes
-- =========================================

-- Habilitar extensão pg_trgm para suporte a índices GIN com ILIKE
-- Necessário para os índices de busca por nome e título
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 1. Tabela de usuários
-- Nota: a constraint UNIQUE em email já cria um índice implícito no PostgreSQL
CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255)        NOT NULL,
    email      VARCHAR(255)        NOT NULL UNIQUE,
    password   VARCHAR(255)        NOT NULL,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- Índice GIN trigram em users.name para suportar buscas com ILIKE (%nome%)
-- Utilizado por: findByNameContainingIgnoreCase
CREATE INDEX IF NOT EXISTS idx_users_name_trgm ON users USING gin(name gin_trgm_ops);

-- 2. Tabela de refresh tokens
-- Nota: a constraint UNIQUE em token já cria um índice implícito no PostgreSQL
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(512)        NOT NULL UNIQUE,
    user_id    BIGINT              NOT NULL,
    revoked    BOOLEAN             NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP           NOT NULL,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- 3. Tabela de notas
CREATE TABLE IF NOT EXISTS notes (
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(255)        NOT NULL,
    content    TEXT                NOT NULL,
    user_id    BIGINT              NOT NULL,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notes_user_id ON notes(user_id);

-- Índice GIN trigram em notes.title para suportar buscas com ILIKE (%título%)
-- Utilizado por: findByTitleContainingIgnoreCase e findByUserIdAndTitleContainingIgnoreCase
-- Nota: GIN tsvector ('to_tsvector') foi descartado pois só funciona com operador @@
--       e as queries do repositório usam ILIKE, que requer pg_trgm
CREATE INDEX IF NOT EXISTS idx_notes_title_trgm ON notes USING gin(title gin_trgm_ops);
