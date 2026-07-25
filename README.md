# Watts4Homes Frontend

Real-time IoT energy dashboard for the Watts4Homes project.
React + TypeScript + Vite + Tailwind v4 + Recharts.

## Setup

```bash
npm install
npm run dev
```

Opens on http://localhost:5173

During development, requests to `/api` are proxied to the Core backend on
`http://localhost:8083` (see vite.config.ts). Make sure Core is running.

## Structure

- `src/pages/Dashboard.tsx` - main view
- `src/components/` - TopBar, PulseLine (the signature energy doodle), cards
- `src/types/api.ts` - TypeScript types mirroring the backend DTOs
- `src/index.css` - Tailwind v4 theme tokens (the design system)

## Current state

First skeleton: theme, layout, and the pulse doodle are working with
placeholder cards. Live telemetry wiring is the next step.
