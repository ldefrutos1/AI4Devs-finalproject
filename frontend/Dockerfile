# Frontend MyTreeLibrary: build del SPA (Vite) y servido con Nginx.
# Nginx hace proxy de /api al api-gateway (mismo origen que el SPA), igual que
# el proxy de Vite en desarrollo.

# ── Etapa 1: build del SPA ───────────────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Variables publicas inyectadas en el bundle en tiempo de build (VITE_*).
# En E2E dockerizado, el navegador (contenedor Playwright) alcanza Keycloak por
# su nombre de red: VITE_OIDC_ISSUER=http://keycloak:8080/realms/mtl-e2e.
ARG VITE_OIDC_ISSUER
ARG VITE_OIDC_CLIENT_ID
ARG VITE_OIDC_SCOPE
ARG VITE_GATEWAY_BASE_URL
ENV VITE_OIDC_ISSUER=$VITE_OIDC_ISSUER \
    VITE_OIDC_CLIENT_ID=$VITE_OIDC_CLIENT_ID \
    VITE_OIDC_SCOPE=$VITE_OIDC_SCOPE \
    VITE_GATEWAY_BASE_URL=$VITE_GATEWAY_BASE_URL

RUN npm run build

# ── Etapa 2: servidor estatico (Nginx) ───────────────────────────────────
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
