import { mockHomes, mockApplianceMeta, mockHomeNames, mockTrends } from "./mockData";
import type {
  HomeStatus,
  HomeRegistrationRequest,
  HomeRegistrationResponse,
  HistoricalTrend,
} from "../types/api";

// single switch: flip to false when the backend is ready
const USE_MOCK = false;

// -------- name cache --------
// the /status endpoints return ids only, never names -> we remember the
// names from each registration response and reuse them across refreshes
const CACHE_KEY = "w4h:meta";

type MetaCache = {
  homeNames: Record<number, string>;
  applianceMeta: Record<number, { name: string; type: string }>;
};

// loading the cache from localStorage on startup -> empty if nothing saved
function loadCache(): MetaCache {
  try {
    const raw = localStorage.getItem(CACHE_KEY);
    if (raw) return JSON.parse(raw);
  } catch {
    // ignore a corrupt cache -> start fresh
  }
  return { homeNames: {}, applianceMeta: {} };
}

const cache = loadCache();

// persisting the cache after every change
function saveCache() {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify(cache));
  } catch {
    // ignore write failures -> names just won't survive a refresh
  }
}

// fetch all homes' live status
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

// resolve a home's display name
// real -> cached registration name, fall back to a generic label
export function getHomeName(homeId: number): string {
  if (USE_MOCK) return mockHomeNames[homeId] ?? `Ev #${homeId}`;
  return cache.homeNames[homeId] ?? `Ev #${homeId}`;
}

// resolve an appliance's name + type
export function getApplianceMeta(applianceId: number): { name: string; type: string } {
  if (USE_MOCK) {
    return mockApplianceMeta[applianceId] ?? { name: "Cihaz", type: "DEFAULT" };
  }
  return cache.applianceMeta[applianceId] ?? { name: "Cihaz", type: "DEFAULT" };
}

// mock-only id counters
let nextHomeId = 100;
let nextApplianceId = 100;

// register a new home
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

  // the response carries the assigned ids plus the names we sent ->
  // cache them so the dashboard and modal can show real labels
  const created: HomeRegistrationResponse = await res.json();
  cache.homeNames[created.homeId] = created.name;
  for (const a of created.appliances) {
    cache.applianceMeta[a.applianceId] = { name: a.name, type: a.type };
  }
  saveCache();
}

// fetch a home's historical trend
export async function fetchTrend(homeId: number): Promise<HistoricalTrend> {
  if (USE_MOCK) {
    await new Promise((r) => setTimeout(r, 250));
    return mockTrends[homeId] ?? { homeId, points: [] };
  }
  const res = await fetch(`/api/homes/${homeId}/trend`);
  if (!res.ok) throw new Error(`Trend alınamadı (${res.status})`);
  return res.json();
}