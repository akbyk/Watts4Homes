// these mirror the backend records in watts4homes-core exactly
// keep them in sync with HomeStatusResponse, HistoricalTrendResponse, etc.

export type TariffState = string;
export type ApplianceLiveStatus = string;

// GET /api/homes/status  and  /api/homes/{id}/status
export interface ApplianceStatus {
  applianceId: number;
  safeLimitWatts: number;
  consecutiveBreachCount: number;
  status: ApplianceLiveStatus;
}

export interface HomeStatus {
  homeId: number;
  accumulatedUsage: number;
  accumulatedCost: number;
  tariffState: TariffState;
  budgetQuota: number;
  appliances: ApplianceStatus[];
}

// GET /api/homes/{id}/trend
export interface DailyPoint {
  date: string; // ISO date, e.g. "2026-07-24"
  totalUsage: number;
  totalCost: number;
}

export interface HistoricalTrend {
  homeId: number;
  points: DailyPoint[];
}

// POST /api/homes  request
export interface ApplianceRequest {
  name: string;
  type: string;
  safeLimitWatts: number;
}

export interface HomeRegistrationRequest {
  name: string;
  address?: string;
  contactEmail: string;
  budgetQuota: number;
  currentRate: number;
  penaltyRate: number;
  appliances: ApplianceRequest[];
}

// POST /api/homes  response
export interface ApplianceResponse {
  applianceId: number;
  name: string;
  type: string;
  safeLimitWatts: number | null;
}

export interface HomeRegistrationResponse {
  homeId: number;
  name: string;
  contactEmail: string;
  appliances: ApplianceResponse[];
}
