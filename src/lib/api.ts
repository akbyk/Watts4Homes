import { mockHomes, mockApplianceMeta, mockHomeNames } from "./mockData";
import type { HomeStatus, HomeRegistrationRequest } from "../types/api";

// single switch: flip to false when the backend is ready
const USE_MOCK = true;

// fetch all homes' live status
// mock now, real /api/homes/status later - callers don't need to know which
export async function fetchHomeStatus(): Promise<HomeStatus[]> {
  if (USE_MOCK) {
    // simulate a tiny network delay so loading states are visible
    await new Promise((r) => setTimeout(r, 300));
    return mockHomes;
  }

  const res = await fetch("/api/homes/status");
  if (!res.ok) {
    throw new Error(`Durum alınamadı (${res.status})`);
  }
  return res.json();
}

// the /status endpoint returns only IDs, so we resolve display names here.
// with the real backend these come from the registration response we cache.
export function getHomeName(homeId: number): string {
  if (USE_MOCK) return mockHomeNames[homeId] ?? `Ev #${homeId}`;
  return `Ev #${homeId}`;
}

export function getApplianceMeta(applianceId: number): { name: string; type: string } {
  if (USE_MOCK) {
    return mockApplianceMeta[applianceId] ?? { name: "Cihaz", type: "DEFAULT" };
  }
  return { name: "Cihaz", type: "DEFAULT" };
}

// register a new home
// mock now (adds to the in-memory list), POST /api/homes later
let nextHomeId = 100; // mock ids start high to avoid clashing with seed data
let nextApplianceId = 100;

export async function registerHome(
  req: HomeRegistrationRequest
): Promise<void> {
  if (USE_MOCK) {
    await new Promise((r) => setTimeout(r, 400));
    const homeId = nextHomeId++;
    mockHomeNames[homeId] = req.name;
    const appliances = req.appliances.map((a) => {
      const id = nextApplianceId++;
      mockApplianceMeta[id] = { name: a.name, type: a.type };
      return {
        applianceId: id,
        safeLimitWatts: a.safeLimitWatts,
        consecutiveBreachCount: 0,
        status: "NORMAL" as const,
      };
    });
    mockHomes.push({
      homeId,
      accumulatedUsage: 0,
      accumulatedCost: 0,
      tariffState: "NORMAL",
      budgetQuota: req.budgetQuota,
      appliances,
    });
    return;
  }

  const res = await fetch("/api/homes", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });
  if (!res.ok) {
    throw new Error(`Ev kaydedilemedi (${res.status})`);
  }
}

import type { HistoricalTrend } from "../types/api";
import { mockTrends } from "./mockData";

// fetch a home's historical trend
// mock now, GET /api/homes/{id}/trend later
export async function fetchTrend(homeId: number): Promise<HistoricalTrend> {
  if (USE_MOCK) {
    await new Promise((r) => setTimeout(r, 250));
    return mockTrends[homeId] ?? { homeId, points: [] };
  }
  const res = await fetch(`/api/homes/${homeId}/trend`);
  if (!res.ok) throw new Error(`Trend alınamadı (${res.status})`);
  return res.json();
}