# Watts4Homes Frontend

Real-time IoT energy analytics dashboard for the Watts4Homes platform. It
visualizes live household energy consumption, budget usage, quota breaches,
and appliance anomalies by polling the Watts4Homes Core backend.

Built with React, TypeScript, Vite, Tailwind CSS v4, and Recharts.

## Features

- **Live dashboard grid** — polls every 2 seconds; each home is rendered with
  a status color (green / amber / coral) driven by budget usage and anomalies.
- **Home detail modal** — cumulative usage, cost, and budget percentage;
  tariff state (normal / penalty); per-appliance list; historical trend chart.
- **Quota thresholds** — WARNING at 80% and BREACH at 100% of the budget quota.
- **Anomaly highlighting** — appliances flagged as anomalous (repeated safe-limit
  violations) are visually separated from normal ones.
- **Home registration** — a form to create a new home with a budget quota and a
  selectable list of appliances.
- **Graceful error handling** — if the backend is unreachable, the last known
  data stays on screen and a non-blocking warning banner is shown.
- **Stop tracking** — homes can be hidden from the dashboard locally.

## Requirements

- Node.js 18 or newer
- A running instance of the Watts4Homes Core backend (default: `http://localhost:8083`)

## Setup

```bash
npm install
npm run dev
```

The app runs at `http://localhost:5173`.

To create a production build:

```bash
npm run build
npm run preview
```

## Backend connection

During development, requests to `/api` are proxied to the Core backend at
`http://localhost:8083` (configured in `vite.config.ts`). Ensure Core is
running before starting the frontend.

Consumed endpoints:

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `GET`  | `/api/homes/status` | Live status of all homes (dashboard grid) |
| `GET`  | `/api/homes/{id}/status` | Live status of a single home |
| `GET`  | `/api/homes/{id}/trend` | Historical consumption trend |
| `POST` | `/api/homes` | Register a new home |


## Implementation notes

- **Name cache** — the `/status` endpoints return only IDs, not home or
  appliance names. Names are read from the `POST /api/homes` response and
  cached in `localStorage` so labels persist across refreshes.
- **Local hide** — the backend exposes no delete endpoint. "Stop tracking"
  hides a home in the UI only (persisted in `localStorage`); the home continues
  to exist in the backend.
- **Trend chart cap** — the chart renders only the most recent 30 data points
  to keep the DOM light when a home accumulates a long history.
- **Status derivation** — a home is BREACH when the tariff is `PENALTY` or cost
  ≥ budget, ANOMALY when any appliance is anomalous, WARNING at ≥ 80% budget,
  otherwise NORMAL (see `deriveStatus` in `Dashboard.tsx`).