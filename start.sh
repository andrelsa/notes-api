#!/bin/bash

# Notes API - Script de Inicialização
# Este script inicia os containers Docker e a aplicação Spring Boot

set -e

echo "🚀 Notes API - Iniciando ambiente de desenvolvimento"
echo ""

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar se arquivo .env existe
if [ ! -f .env ]; then
    echo -e "${RED}❌ Arquivo .env não encontrado!${NC}"
    echo ""
    echo "Por favor, crie o arquivo .env com base no .env.example:"
    echo "  cp .env.example .env"
    echo ""
    exit 1
fi

# Carregar variáveis de ambiente do .env
echo "📋 Carregando configurações do .env..."
export $(grep -v '^#' .env | xargs)
echo -e "${GREEN}✅ Variáveis de ambiente carregadas${NC}"
echo ""

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker não está rodando. Por favor, inicie o Docker Desktop.${NC}"
    exit 1
fi

echo "📦 Passo 1/3: Iniciando containers Docker..."
docker-compose up -d

echo ""
echo "⏳ Aguardando PostgreSQL ficar pronto..."
sleep 5

# Verificar se o PostgreSQL está saudável
until docker-compose exec -T postgres pg_isready -U ${POSTGRES_USER} > /dev/null 2>&1; do
    echo "   Aguardando PostgreSQL..."
    sleep 2
done

echo -e "${GREEN}✅ PostgreSQL está pronto!${NC}"
echo ""

echo "📊 Passo 2/3: Status dos containers:"
docker-compose ps
echo ""

echo -e "${BLUE}📝 Informações de acesso:${NC}"
echo -e "   🌐 API:      http://localhost:${APP_PORT}/api/notes"
echo -e "   🎨 pgAdmin:  http://localhost:${PGADMIN_PORT}"
echo -e "   🗄️  Database: postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}"
echo ""

echo "🏗️  Passo 3/3: Iniciando aplicação Spring Boot..."
echo ""
echo -e "${YELLOW}⚠️  A aplicação será iniciada agora. Use Ctrl+C para parar.${NC}"
echo ""

# Aguardar um momento antes de iniciar a aplicação
sleep 2

# Iniciar a aplicação
./gradlew bootRun --debug-jvm
