#!/bin/bash

# Notes API - Script de Reinício
# Este script reinicia os containers Docker sem perder dados

set -e

echo "🔄 Notes API - Reiniciando containers Docker"
echo ""

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar se arquivo .env existe
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker Desktop."
    exit 1
fi

echo "📦 Reiniciando containers..."
docker-compose restart

echo ""
echo "⏳ Aguardando PostgreSQL ficar pronto..."
sleep 3

# Verificar se o PostgreSQL está saudável
until docker-compose exec -T postgres pg_isready -U ${POSTGRES_USER:-postgres} > /dev/null 2>&1; do
    echo "   Aguardando PostgreSQL..."
    sleep 2
done

echo -e "${GREEN}✅ PostgreSQL está pronto!${NC}"
echo ""

echo "📊 Status dos containers:"
docker-compose ps
echo ""
echo -e "${GREEN}✅ Containers reiniciados com sucesso!${NC}"
echo ""
