-- =========================================
-- Script de Testes - Sistema de Roles
-- =========================================

-- 1. Verificar estrutura da tabela user_roles
SELECT * FROM information_schema.columns
WHERE table_name = 'user_roles';

-- 2. Verificar todos os usuários e suas roles
SELECT
    u.id,
    u.name,
    u.email,
    GROUP_CONCAT(ur.role ORDER BY ur.role SEPARATOR ', ') as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
GROUP BY u.id, u.name, u.email
ORDER BY u.id;

-- 3. Contar usuários por role
SELECT
    role,
    COUNT(*) as total_users
FROM user_roles
GROUP BY role
ORDER BY total_users DESC;

-- 4. Verificar usuários sem roles (problema!)
SELECT u.id, u.name, u.email
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
WHERE ur.user_id IS NULL;

-- 5. Criar usuário de teste com ROLE_USER
INSERT INTO users (name, email, password, created_at, updated_at)
VALUES ('Test User', 'test@example.com', '$2a$10$encrypted', NOW(), NOW());

INSERT INTO user_roles (user_id, role)
VALUES (LAST_INSERT_ID(), 'ROLE_USER');

-- 6. Criar usuário admin de teste
INSERT INTO users (name, email, password, created_at, updated_at)
VALUES ('Admin User', 'admin@example.com', '$2a$10$encrypted', NOW(), NOW());

INSERT INTO user_roles (user_id, role)
VALUES
    (LAST_INSERT_ID(), 'ROLE_USER'),
    (LAST_INSERT_ID(), 'ROLE_ADMIN');

-- 7. Promover usuário existente a ADMIN
-- (Substitua 1 pelo ID real do usuário)
INSERT INTO user_roles (user_id, role)
VALUES (1, 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE role = role; -- Evita erro se já existe

-- 8. Rebaixar usuário (remover role ADMIN)
DELETE FROM user_roles
WHERE user_id = 1 AND role = 'ROLE_ADMIN';

-- 9. Atribuir múltiplas roles a um usuário
INSERT INTO user_roles (user_id, role)
VALUES
    (1, 'ROLE_USER'),
    (1, 'ROLE_MANAGER'),
    (1, 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE role = role;

-- 10. Listar apenas usuários ADMIN
SELECT DISTINCT u.id, u.name, u.email
FROM users u
INNER JOIN user_roles ur ON u.id = ur.user_id
WHERE ur.role = 'ROLE_ADMIN';

-- 11. Listar usuários que têm ADMIN OU MANAGER
SELECT DISTINCT u.id, u.name, u.email
FROM users u
INNER JOIN user_roles ur ON u.id = ur.user_id
WHERE ur.role IN ('ROLE_ADMIN', 'ROLE_MANAGER');

-- 12. Listar usuários com EXATAMENTE as roles USER e ADMIN
SELECT u.id, u.name, u.email
FROM users u
WHERE u.id IN (
    SELECT user_id
    FROM user_roles
    WHERE role = 'ROLE_USER'
) AND u.id IN (
    SELECT user_id
    FROM user_roles
    WHERE role = 'ROLE_ADMIN'
);

-- 13. Verificar integridade referencial
SELECT
    'Orphaned roles' as issue,
    ur.*
FROM user_roles ur
LEFT JOIN users u ON ur.user_id = u.id
WHERE u.id IS NULL;

-- 14. Estatísticas gerais
SELECT
    'Total de usuários' as metric,
    COUNT(*) as value
FROM users
UNION ALL
SELECT
    'Total de roles atribuídas',
    COUNT(*)
FROM user_roles
UNION ALL
SELECT
    'Usuários com múltiplas roles',
    COUNT(DISTINCT user_id)
FROM user_roles
GROUP BY user_id
HAVING COUNT(*) > 1;

-- 15. Resetar roles de um usuário para apenas USER
DELETE FROM user_roles WHERE user_id = 1;
INSERT INTO user_roles (user_id, role) VALUES (1, 'ROLE_USER');

-- =========================================
-- Queries para aplicação
-- =========================================

-- Buscar usuário com roles (usado pelo CustomUserDetailsService)
SELECT
    u.id,
    u.name,
    u.email,
    u.password,
    ur.role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
WHERE u.email = 'admin@example.com';

-- Verificar se usuário tem role específica
SELECT COUNT(*) as has_role
FROM user_roles
WHERE user_id = 1 AND role = 'ROLE_ADMIN';
