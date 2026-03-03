-- =========================================
-- Migration: Corrigir índices de busca
-- Data: 2026-03-02
-- Descrição: Substitui o índice GIN tsvector (full-text search com @@)
--            por índices GIN trigram (pg_trgm) compatíveis com ILIKE,
--            que é o operador utilizado pelas queries do repositório.
--            Adiciona também índice trigram em users.name.
-- =========================================

-- Habilitar extensão pg_trgm (idempotente)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Remover índice GIN tsvector incompatível com ILIKE
-- (criado na V1 mas nunca utilizado pelas queries do repositório)
DROP INDEX IF EXISTS idx_notes_title;

-- Criar índice GIN trigram em notes.title
-- Utilizado por: findByTitleContainingIgnoreCase e findByUserIdAndTitleContainingIgnoreCase
CREATE INDEX IF NOT EXISTS idx_notes_title_trgm ON notes USING gin(title gin_trgm_ops);

-- Criar índice GIN trigram em users.name
-- Utilizado por: findByNameContainingIgnoreCase
CREATE INDEX IF NOT EXISTS idx_users_name_trgm ON users USING gin(name gin_trgm_ops);

