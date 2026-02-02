#!/bin/bash

# Script de Teste - Validação do Ambiente Docker
# Verifica se tudo está configurado corretamente

set -e

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔍 Iniciando validação do ambiente Docker...${NC}"
echo ""

# Função para verificar sucesso/falha
check_result() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        return 0
    else
        echo -e "${RED}❌ $1${NC}"
        return 1
    fi
}

# 1. Verificar Docker
echo "1️⃣  Verificando Docker..."
docker --version > /dev/null 2>&1
check_result "Docker instalado"

docker-compose --version > /dev/null 2>&1
check_result "Docker Compose instalado"

# 2. Verificar se containers estão rodando
echo ""
echo "2️⃣  Verificando containers..."
docker-compose ps | grep -q "notesdb-postgres.*Up"
check_result "Container PostgreSQL rodando"

docker-compose ps | grep -q "notesdb-pgadmin.*Up"
check_result "Container pgAdmin rodando"

# 3. Verificar healthcheck do PostgreSQL
echo ""
echo "3️⃣  Verificando saúde do PostgreSQL..."
docker inspect notesdb-postgres | grep -q '"Status": "healthy"'
check_result "PostgreSQL está saudável"

# 4. Testar conexão com PostgreSQL
echo ""
echo "4️⃣  Testando conexão com PostgreSQL..."
docker-compose exec -T postgres psql -U postgres -d notesdb -c "SELECT 1;" > /dev/null 2>&1
check_result "Conexão com PostgreSQL OK"

# 5. Verificar se o banco de dados existe
echo ""
echo "5️⃣  Verificando banco de dados..."
docker-compose exec -T postgres psql -U postgres -c "\l" | grep -q "notesdb"
check_result "Database 'notesdb' existe"

# 6. Verificar arquivos de configuração
echo ""
echo "6️⃣  Verificando arquivos de configuração..."
test -f docker-compose.yml
check_result "docker-compose.yml existe"

test -f .env
check_result ".env existe"

test -f src/main/resources/application.yml
check_result "application.yml existe"

test -f src/main/resources/application-dev.yml
check_result "application-dev.yml existe"

# 7. Verificar configuração do application-dev.yml
echo ""
echo "7️⃣  Verificando configuração do Spring Boot..."
grep -q "ddl-auto: update" src/main/resources/application-dev.yml
check_result "ddl-auto configurado como 'update'"

grep -q "jdbc:postgresql://localhost:5432/notesdb" src/main/resources/application-dev.yml
check_result "URL do banco de dados correta"

# 8. Verificar portas
echo ""
echo "8️⃣  Verificando portas..."
lsof -i :5432 > /dev/null 2>&1
check_result "Porta 5432 (PostgreSQL) em uso"

lsof -i :5050 > /dev/null 2>&1
check_result "Porta 5050 (pgAdmin) em uso"

# 9. Verificar volumes
echo ""
echo "9️⃣  Verificando volumes Docker..."
docker volume ls | grep -q "app-notes-api_postgres-data"
check_result "Volume postgres-data existe"

docker volume ls | grep -q "app-notes-api_pgadmin-data"
check_result "Volume pgadmin-data existe"

# Resumo
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Validação concluída!${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${YELLOW}📊 Informações de Acesso:${NC}"
echo ""
echo -e "  🌐 API:      http://localhost:8080/api/notes"
echo -e "  🎨 pgAdmin:  http://localhost:5050"
echo -e "     Email:    admin@notesapi.com"
echo -e "     Senha:    admin"
echo ""
echo -e "  🗄️  PostgreSQL:"
echo -e "     Host:     localhost (ou 'postgres' dentro do Docker)"
echo -e "     Port:     5432"
echo -e "     Database: notesdb"
echo -e "     User:     postgres"
echo -e "     Password: postgres"
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${YELLOW}🚀 Próximos passos:${NC}"
echo -e "   1. Inicie a aplicação: ${GREEN}./gradlew bootRun${NC}"
echo -e "   2. Teste a API: ${GREEN}curl http://localhost:8080/api/notes${NC}"
echo -e "   3. Acesse o pgAdmin: ${GREEN}http://localhost:5050${NC}"
echo ""
echo -e "${BLUE}📖 Mais informações: ${GREEN}DOCKER_SETUP_GUIDE.md${NC}"
echo ""
