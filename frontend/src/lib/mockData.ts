import type { HomeStatus } from "../types/api";

// mock data shaped exactly like the backend's HomeStatusResponse.
// when Core is ready, we swap the data source in one place - this file goes away.

export const mockHomes: HomeStatus[] = [
  {
    homeId: 1,
    homeName: "Kadıköy Dairesi",
    accumulatedUsage: 0.21,
    accumulatedCost: 21.4,
    tariffState: "NORMAL",
    budgetQuota: 0.5,
    appliances: [
      { applianceId: 1, name: "Buzdolabı", type: "FRIDGE", safeLimitWatts: 200, consecutiveBreachCount: 0, status: "NORMAL" },
      { applianceId: 2, name: "Fırın", type: "OVEN", safeLimitWatts: 2500, consecutiveBreachCount: 0, status: "NORMAL" },
      { applianceId: 3, name: "Su Isıtıcısı", type: "KETTLE", safeLimitWatts: 2000, consecutiveBreachCount: 0, status: "NORMAL" },
      { applianceId: 4, name: "Çamaşır Makinesi", type: "WASHER", safeLimitWatts: 2200, consecutiveBreachCount: 0, status: "NORMAL" },
    ],
  },
  {
    homeId: 2,
    homeName: "Bahçelievler Evi",
    accumulatedUsage: 0.43,
    accumulatedCost: 43.1,
    tariffState: "NORMAL",
    budgetQuota: 0.5,
    appliances: [
      { applianceId: 5, name: "Buzdolabı", type: "FRIDGE", safeLimitWatts: 200, consecutiveBreachCount: 0, status: "NORMAL" },
      { applianceId: 6, name: "Klima", type: "AC", safeLimitWatts: 1800, consecutiveBreachCount: 2, status: "ANOMALOUS" },
      { applianceId: 7, name: "Su Isıtıcısı", type: "KETTLE", safeLimitWatts: 1500, consecutiveBreachCount: 0, status: "NORMAL" },
    ],
  },
  {
    homeId: 3,
    homeName: "Ofis Katı",
    accumulatedUsage: 0.59,
    accumulatedCost: 118.7,
    tariffState: "PENALTY",
    budgetQuota: 0.5,
    appliances: [
      { applianceId: 8, name: "Fırın", type: "OVEN", safeLimitWatts: 2500, consecutiveBreachCount: 4, status: "ANOMALOUS" },
      { applianceId: 9, name: "Su Isıtıcısı", type: "KETTLE", safeLimitWatts: 900, consecutiveBreachCount: 3, status: "ANOMALOUS" },
      { applianceId: 10, name: "Buzdolabı", type: "FRIDGE", safeLimitWatts: 200, consecutiveBreachCount: 0, status: "NORMAL" },
    ],
  },
];

// mock appliance names/types, keyed by applianceId.
// the /status endpoint only returns IDs, so the frontend keeps this lookup itself.
export const mockApplianceMeta: Record<number, { name: string; type: string }> = {
  1: { name: "Buzdolabı", type: "FRIDGE" },
  2: { name: "Fırın", type: "OVEN" },
  3: { name: "Su Isıtıcısı", type: "KETTLE" },
  4: { name: "Çamaşır Makinesi", type: "WASHER" },
  5: { name: "Buzdolabı", type: "FRIDGE" },
  6: { name: "Klima", type: "AC" },
  7: { name: "Su Isıtıcısı", type: "KETTLE" },
  8: { name: "Fırın", type: "OVEN" },
  9: { name: "Su Isıtıcısı", type: "KETTLE" },
  10: { name: "Buzdolabı", type: "FRIDGE" },
};

export const mockHomeNames: Record<number, string> = {
  1: "Kadıköy Dairesi",
  2: "Bahçelievler Evi",
  3: "Ofis Katı",
};

import type { HistoricalTrend } from "../types/api";

// mock daily trend per home, shaped like /api/homes/{id}/trend
// 7 days of usage/cost so the chart has something to draw
function makeTrend(homeId: number, base: number): HistoricalTrend {
  const points = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    // gentle random walk around the base value
    const usage = +(base + Math.sin(i) * 0.08 + Math.random() * 0.05).toFixed(3);
    return {
      date: d.toISOString().slice(0, 10),
      totalUsage: usage,
      totalCost: +(usage * 100).toFixed(1),
    };
  });
  return { homeId, points };
}

export const mockTrends: Record<number, HistoricalTrend> = {
  1: makeTrend(1, 0.18),
  2: makeTrend(2, 0.32),
  3: makeTrend(3, 0.51),
};