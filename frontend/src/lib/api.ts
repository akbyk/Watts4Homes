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

// -------- hidden homes --------
// no backend delete exists -> we just stop showing a home locally.
// the home still lives in the backend, we simply filter it out here
const HIDDEN_KEY = "w4h:hidden";

// loading the set of hidden home ids from localStorage
function loadHidden(): Set<number> {
  try {
    const raw = localStorage.getItem(HIDDEN_KEY);
    if (raw) return new Set(JSON.parse(raw));
  } catch {
    // ignore a corrupt value -> nothing hidden
  }
  return new Set();
}

const hidden = loadHidden();

// persisting the hidden set after every change
function saveHidden() {
  try {
    localStorage.setItem(HIDDEN_KEY, JSON.stringify([...hidden]));
  } catch {
    // ignore write failures
  }
}

// stop tracking a home -> hide it from the dashboard
export function hideHome(homeId: number) {
  hidden.add(homeId);
  saveHidden();
}

// check whether a home is hidden
export function isHidden(homeId: number): boolean {
  return hidden.has(homeId);
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

  const homes: HomeStatus[] = await res.json();

  // the status response now carries names -> seed the cache from it so labels
  // are correct on any device, not just the one that registered the home
  let changed = false;
  for (const home of homes) {
    if (home.homeName && cache.homeNames[home.homeId] !== home.homeName) {
      cache.homeNames[home.homeId] = home.homeName;
      changed = true;
    }
    for (const a of home.appliances) {
      const meta = cache.applianceMeta[a.applianceId];
      if (a.name && (meta?.name !== a.name || meta?.type !== a.type)) {
        cache.applianceMeta[a.applianceId] = { name: a.name, type: a.type };
        changed = true;
      }
    }
  }
  if (changed) saveCache();

  return homes;
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
        name: a.name,
        type: a.type,
        safeLimitWatts: a.safeLimitWatts,
        consecutiveBreachCount: 0,
        status: "NORMAL" as const,
      };
    });
    mockHomes.push({
      homeId,
      homeName: req.name,
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