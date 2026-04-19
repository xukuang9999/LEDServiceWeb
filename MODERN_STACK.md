# Modern Stack Notes

This repository now supports a dual-track setup:

- The existing Spring Boot + Thymeleaf CMS remains intact.
- A new React + Vite + TypeScript frontend lives in `frontend/`.

## Version Baseline

- Java runtime: OpenJDK 25.x recommended
- Java build target: 21
- Maven: 3.9.14
- Node.js: 24.x LTS recommended
- React: 19.2.x
- Vite: 8.x
- TypeScript: 6.x

## Local Commands

Run the environment check:

```bash
npm run doctor
```

Start the React frontend:

```bash
npm run frontend:install
npm run frontend:dev
```

Start the Spring Boot backend:

```bash
npm run backend:dev
```

Build the React app and sync it into Spring Boot static assets:

```bash
npm run frontend:build:backend
```

After syncing, the frontend is served from:

```text
/zhglxt/modern/
```

## macOS Toolchain Upgrade

If Homebrew is available, install the recommended Java and Maven versions with:

```bash
./scripts/install-macos-toolchain.sh
```

If you also want the latest non-LTS Node release from Homebrew:

```bash
./scripts/install-macos-toolchain.sh --with-node
```

## Backend Runtime Defaults

The backend helper script overrides the old hard-coded profile path and uses:

- `ZHGLXT_PROFILE=.local/runtime-userfiles`
- `server.port=8889`
- `server.servlet.context-path=/zhglxt`
- `spring.profiles.active=druid-dev,mybatis-dev,shiro-dev,doc-dev,file-manager`

Override database credentials when needed:

```bash
DB_URL=jdbc:mysql://localhost:3306/zhglxt \
DB_USERNAME=root \
DB_PASSWORD=secret \
npm run backend:dev
```
