import { useEffect } from "react";
import type { HomeStatus } from "../types/api";
import { ApplianceIcon } from "./ApplianceIcon";
import { StatusBadge } from "./StatusBadge";
import { HouseMark } from "./HouseDoodle";
import { getApplianceMeta } from "../lib/api";
import { TrendChart } from "./TrendChart";

interface HomeDetailModalProps {
  home: HomeStatus;
  name: string;
  status: "NORMAL" | "WARNING" | "BREACH" | "ANOMALY";
  onClose: () => void;
}

const statusColor = {
  NORMAL: "var(--color-energy)",
  WARNING: "var(--color-amber)",
  BREACH: "var(--color-coral)",
  ANOMALY: "var(--color-coral)",
};

export function HomeDetailModal({
  home,
  name,
  status,
  onClose,
}: HomeDetailModalProps) {
  const accent = statusColor[status];

  // close on Escape key - a small accessibility nicety
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  const usedPercent = Math.round(
    (home.accumulatedCost / (home.budgetQuota * 100)) * 100
  );

  return (
    // backdrop - clicking it closes the modal
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 p-4 backdrop-blur-sm"
      onClick={onClose}
    >
      {/* panel - stopPropagation so clicks inside don't close it */}
      <div
        className="max-h-[85vh] w-full max-w-lg overflow-y-auto rounded-[var(--radius-card)] bg-paper p-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        {/* header */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <HouseMark color={accent} size={38} />
            <div>
              <h2 className="font-display text-2xl font-500 text-ink">{name}</h2>
              <p className="mt-0.5 text-sm text-slate">
                {home.appliances.length} cihaz izleniyor
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            aria-label="Kapat"
            className="grid h-8 w-8 place-items-center rounded-full text-slate transition hover:bg-mist hover:text-ink"
          >
            ✕
          </button>
        </div>

        {/* summary stats */}
        <div className="mt-5 grid grid-cols-3 gap-3">
          <div className="rounded-2xl bg-mist/60 p-3 text-center">
            <p className="text-xs text-slate">Kullanım</p>
            <p className="mt-1 font-mono text-lg font-500 text-ink">
              {home.accumulatedUsage.toFixed(2)}
              <span className="text-xs text-slate"> kWh</span>
            </p>
          </div>
          <div className="rounded-2xl bg-mist/60 p-3 text-center">
            <p className="text-xs text-slate">Maliyet</p>
            <p className="mt-1 font-mono text-lg font-500 text-ink">
              {home.accumulatedCost.toFixed(0)}
              <span className="text-xs text-slate"> ₺</span>
            </p>
          </div>
          <div className="rounded-2xl bg-mist/60 p-3 text-center">
            <p className="text-xs text-slate">Bütçe</p>
            <p className="mt-1 font-mono text-lg font-500" style={{ color: accent }}>
              %{usedPercent}
            </p>
          </div>
        </div>

        {/* tariff + status row */}
        <div className="mt-4 flex items-center justify-between rounded-2xl border border-line px-4 py-3">
          <span className="text-sm text-slate">Tarife durumu</span>
          <div className="flex items-center gap-2">
            <StatusBadge status={status} />
            <span
              className={`rounded-full px-2.5 py-1 text-xs font-500 ${
                home.tariffState === "PENALTY"
                  ? "bg-coral-soft text-coral"
                  : "bg-energy-soft text-energy"
              }`}
            >
              {home.tariffState === "PENALTY" ? "Ceza tarifesi" : "Normal tarife"}
            </span>
          </div>
        </div>
        {/* consumption trend */}
        <h3 className="mb-3 mt-6 font-display text-lg font-500 text-ink">
          7 günlük tüketim
        </h3>
        <TrendChart homeId={home.homeId} color={accent} />
        {/* appliance list */}
        <h3 className="mb-3 mt-6 font-display text-lg font-500 text-ink">
          Cihazlar
        </h3>
        <div className="space-y-2">
          {home.appliances.map((a) => {
            const meta = getApplianceMeta(a.applianceId);
            const isAnomalous = a.status === "ANOMALOUS";
            return (
              <div
                key={a.applianceId}
                className={`flex items-center gap-3 rounded-2xl border px-3 py-2.5 ${
                  isAnomalous
                    ? "border-coral/30 bg-coral-soft/40"
                    : "border-line bg-white"
                }`}
              >
                <ApplianceIcon
                  type={meta.type}
                  size={30}
                  color={isAnomalous ? "var(--color-coral)" : "var(--color-slate)"}
                />
                <div className="flex-1">
                  <p className="text-sm font-500 text-ink">{meta.name}</p>
                  <p className="font-mono text-xs text-slate">
                    Limit: {a.safeLimitWatts} W
                  </p>
                </div>
                {isAnomalous ? (
                  <span className="rounded-full bg-coral-soft px-2.5 py-1 text-xs font-500 text-coral">
                    {a.consecutiveBreachCount}× ihlal
                  </span>
                ) : (
                  <span className="rounded-full bg-energy-soft px-2.5 py-1 text-xs font-500 text-energy">
                    normal
                  </span>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}