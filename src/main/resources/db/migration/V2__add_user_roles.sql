-- =========================================
-- Migration: Adicionar Sistema de Roles
-- Data: 2026-02-05
-- Descrição: Cria tabela user_roles e adiciona roles padrão aos usuários existentes
-- =========================================

-- 1. Criar tabela user_roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Adicionar índice para melhorar performance
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- 3. Adicionar role padrão ROLE_USER para todos os usuários existentes
-- (caso haja usuários sem roles)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER'
FROM users
WHERE id NOT IN (SELECT user_id FROM user_roles);

-- =========================================
-- Exemplos de uso:
-- =========================================

-- Adicionar role ADMIN a um usuário específico (substituir 1 pelo ID real)
-- INSERT INTO user_roles (user_id, role) VALUES (1, 'ROLE_ADMIN');

-- Adicionar múltiplas roles a um usuário
-- INSERT INTO user_roles (user_id, role) VALUES
--   (1, 'ROLE_USER'),
--   (1, 'ROLE_ADMIN');

-- Remover uma role de um usuário
-- DELETE FROM user_roles WHERE user_id = 1 AND role = 'ROLE_ADMIN';

-- Listar todas as roles de um usuário
-- SELECT role FROM user_roles WHERE user_id = 1;

-- Listar usuários com uma role específica
-- SELECT u.* FROM users u
-- INNER JOIN user_roles ur ON u.id = ur.user_id
-- WHERE ur.role = 'ROLE_ADMIN';

-- =========================================
-- Roles sugeridas para o sistema:
-- =========================================
-- ROLE_USER    - Usuário padrão (pode gerenciar suas próprias notas)
-- ROLE_ADMIN   - Administrador (acesso total ao sistema)
-- ROLE_MANAGER - Gerente (pode visualizar todas as notas, mas não deletar usuários)
-- ROLE_VIEWER  - Visualizador (apenas leitura)

-- =========================================
-- Verificações:
-- =========================================

-- Verificar estrutura da tabela
-- DESCRIBE user_roles;

-- Contar usuários por role
-- SELECT role, COUNT(*) as total
-- FROM user_roles
-- GROUP BY role;

-- Verificar integridade (usuários sem roles)
-- SELECT u.id, u.email, u.name
-- FROM users u
-- LEFT JOIN user_roles ur ON u.id = ur.user_id
-- WHERE ur.user_id IS NULL;
