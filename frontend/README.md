# CodeCollab — Frontend

React + TypeScript single-page app for the CodeCollab platform (write, run and share code snippets).

The app talks **only** to the API gateway (`http://localhost:8080`) — never to individual microservices.
Authentication is **session-based** (a `JSESSIONID` cookie), not JWT: the browser holds the cookie, the
gateway validates the session and injects the `X-User-Id` header downstream. The frontend never sends a user id.

## Tech stack

- React 18 + TypeScript (strict), Vite 5
- ChakraUI v2 (dark mode only) for UI
- React Router v6 for routing
- Axios (central instance, `withCredentials: true`) for HTTP
- CodeMirror 6 (`@uiw/react-codemirror`) for the code editor
- Auth state in React Context; server state via custom hooks

## Getting started

```bash
# 1. install dependencies
npm install

# 2. create the local env file (points at the gateway)
cp .env.example .env        # PowerShell: Copy-Item .env.example .env

# 3. run the dev server (http://localhost:5173)
npm run dev
```

The backend gateway must be running on `http://localhost:8080`, and the dev server must run on port
`5173` (the gateway's CORS allows that origin with credentials). If you change the port, ask the backend
team to update `GATEWAY_CORS_ALLOWED_ORIGINS`, otherwise the session cookie is rejected and every
protected request returns 401.

## Scripts

| Script              | Description                                |
| ------------------- | ------------------------------------------ |
| `npm run dev`       | Start the Vite dev server on port 5173     |
| `npm run build`     | Type-check and build for production        |
| `npm run preview`   | Preview the production build               |
| `npm run lint`      | Run ESLint                                 |
| `npm run typecheck` | Type-check without emitting                |

## Configuration

| Variable            | Description                                          |
| ------------------- | ---------------------------------------------------- |
| `VITE_API_BASE_URL` | Base URL of the gateway, e.g. `http://localhost:8080/api/v1` |

## Project structure

```
src/
  api/        Axios instance + per-resource API modules
  types/      TypeScript DTOs and enums mirroring the backend contracts
  lib/        Framework-agnostic helpers (error normalization, validation, clipboard, ...)
  context/    React contexts (auth)
  hooks/      Custom data hooks (one per resource)
  components/ Shared, cross-feature components
  features/   Feature folders (auth, dashboard, editor, account, public-share)
  pages/      Thin route components (params + layout + auth gating)
```
