# Ignite Cache Design

## Cache: home-state
- Key: `Long homeId`
- Value: `HomeState`
    - `accumulatedUsage: double`
    - `accumulatedCost: double`
    - `tariffState: String` (NORMAL | PENALTY)
    - `budgetQuota: double`
    - `breachedEightyPercent: boolean`   // prevents repeat-firing the 80% alert
    - `breachedHundredPercent: boolean`  // prevents repeat-firing the 100% alert
- No TTL / no eviction — Postgres is the source of truth for anything permanent;
  this cache is rebuilt from Postgres on Core startup if empty (built Day 3).

## Cache: appliance-breach
- Key: `String` composite — `homeId + ":" + applianceId`
- Value: `ApplianceBreachState`
    - `consecutiveBreachCount: int`
    - `lastStatus: String` (NORMAL | ANOMALOUS)
- Reset to 0 the instant a reading comes back under `safeLimitWatts`.
- Flip to ANOMALOUS at 3 consecutive over-limit readings.

## Naming constants
- `IgniteCacheNames.HOME_STATE = "home-state"`
- `IgniteCacheNames.APPLIANCE_BREACH = "appliance-breach"`