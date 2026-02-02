#!/bin/bash

# Notes API - Script de Parada
# Este script para os containers Docker de forma segura

set -e

echo "🛑 Notes API - Parando ambiente de desenvolvimento"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Verificar se arquivo .env existe e carregar variáveis
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# Mostrar status antes de parar
echo "📊 Status atual dos containers:"
docker-compose ps
echo ""

# Verificar se há containers rodando
if docker-compose ps | grep -q "Up"; then
    echo "📦 Parando containers Docker..."
    docker-compose down
    echo -e "${GREEN}✅ Containers parados com sucesso!${NC}"
    echo ""
    echo -e "${BLUE}ℹ️  Os dados foram preservados nos volumes Docker${NC}"
else
    echo -e "${YELLOW}⚠️  Nenhum container em execução.${NC}"
fi

echo ""
echo "📊 Status final dos containers:"
docker-compose ps
echo ""
echo -e "${GREEN}✅ Ambiente parado.${NC}"
echo ""
echo "Para iniciar novamente:"
echo -e "  ${GREEN}./start.sh${NC}"
echo ""
echo "Para remover dados e volumes:"
echo -e "  ${RED}docker-compose down -v${NC} ${YELLOW}(⚠️  Isso apagará todos os dados!)${NC}"
echo ""
