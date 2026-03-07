# MDQueue Frontend

## Run (Development)

1. Start the backend (Spring Boot) on `http://localhost:8080`
2. In a new terminal:

```bash
cd frontend
npm install
npm run dev
```

The app runs on `http://localhost:5173`.

## Pages

- `/login` – Login UI (matches provided design)
- `/register` – Registration UI (matches provided design)
- `/dashboard` – Protected route (requires JWT)

## API

During development, requests to `/api/*` are proxied to `http://localhost:8080` via `vite.config.ts`.

