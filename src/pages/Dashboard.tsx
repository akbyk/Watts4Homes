import { useState, useEffect } from "react";
import { TopBar } from "../components/TopBar";
import { HomeCard } from "../components/HomeCard";
import { EmptyState } from "../components/HouseDoodle";
import { PulseLine } from "../components/PulseLine";
import { HomeDetailModal } from "../components/HomeDetailModal";
import { AddHomeModal } from "../components/AddHomeModal";
import { fetchHomeStatus, getHomeName, getApplianceMeta } from "../lib/api";
import type { HomeStatus } from "../types/api";

// derive a home's overall status from its numbers
function deriveStatus(home: HomeStatus): "NORMAL" | "WARNING" | "BREACH" | "ANOMALY" {
  const usedRatio = home.accumulatedCost / home.budgetQuota;
  const hasAnomaly = home.appliances.some((a) => a.status === "ANOMALOUS");

  if (home.tariffState === "PENALTY" || usedRatio >= 1) return "BREACH";
  if (hasAnomaly) return "ANOMALY";
  if (usedRatio >= 0.8) return "WARNING";
  return "NORMAL";
}

// budget usage as a percentage
function budgetPercent(home: HomeStatus): number {
  return (home.accumulatedCost / home.budgetQuota) * 100;
}

export function Dashboard() {
  const [homes, setHomes] = useState<HomeStatus[]>([]);
  const [loading, setLoading] = useState(true);
  // holds a user-friendly message when polling fails -> null means all good
  const [error, setError] = useState<string | null>(null);
  const [selectedHomeId, setSelectedHomeId] = useState<number | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);

  // load the homes from the api
  // on failure -> keep the last good data on screen, surface a soft warning
  async function loadHomes() {
    try {
      const data = await fetchHomeStatus();
      setHomes(data);
      setError(null); // recovered -> clear any previous warning
    } catch {
      // suppress the raw error, show a human-readable message instead
      setError("Canlı veriye ulaşılamıyor. Bağlantı yeniden deneniyor...");
    } finally {
      setLoading(false);
    }
  }

  // load once when the page opens, then every 2 seconds
  useEffect(() => {
    loadHomes();
    const timer = setInterval(loadHomes, 2000);
    return () => clearInterval(timer);
  }, []);

  const hasHomes = homes.length > 0;
  const selectedHome = homes.find((h) => h.homeId === selectedHomeId);

  return (
    <div className="min-h-screen">
      <TopBar onAddHome={() => setShowAddModal(true)} />

      <main className="mx-auto max-w-6xl px-6 py-10">
        {/* hero */}
        <section className="mb-12">
          <p className="text-sm font-500 uppercase tracking-wider text-energy">
            Canlı enerji paneli
          </p>
          <h1 className="mt-2 max-w-2xl font-display text-4xl font-500 leading-tight text-ink sm:text-5xl">
            Evinizin enerjisini
            <br />
            gerçek zamanlı izleyin.
          </h1>
          <p className="mt-4 max-w-xl text-slate">
            Her ev bir nabız. Tüketim yükseldikçe dalga büyür, bütçe sınırına
            yaklaşınca renk değişir. Bir bakışta neyin yolunda olduğunu görün.
          </p>
          <div className="mt-6 h-14 max-w-md">
            <PulseLine intensity={0.7} color="var(--color-deep)" />
          </div>
        </section>

        {/* home grid */}
        <section>
          <div className="mb-4 flex items-baseline justify-between">
            <h2 className="font-display text-2xl font-500 text-ink">Evleriniz</h2>
            {hasHomes && <span className="text-sm text-slate">{homes.length} ev</span>}
          </div>

          {/* soft warning banner -> shows only when polling is failing */}
          {error && (
            <div
              role="alert"
              className="mb-4 flex items-center gap-2 rounded-2xl border border-coral/30 bg-coral-soft/50 px-4 py-3 text-sm text-coral"
            >
              <span className="grid h-5 w-5 place-items-center rounded-full bg-coral text-xs text-white">
                !
              </span>
              {error}
            </div>
          )}

          {loading ? (
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
              <div className="h-[220px] rounded-[var(--radius-card)] border border-line bg-mist/50" />
              <div className="h-[220px] rounded-[var(--radius-card)] border border-line bg-mist/50" />
              <div className="h-[220px] rounded-[var(--radius-card)] border border-line bg-mist/50" />
            </div>
          ) : hasHomes ? (
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {homes.map((home) => (
                <HomeCard
                  key={home.homeId}
                  name={getHomeName(home.homeId)}
                  status={deriveStatus(home)}
                  budgetPercent={budgetPercent(home)}
                  appliances={home.appliances.map((a) => ({
                    type: getApplianceMeta(a.applianceId).type,
                    anomalous: a.status === "ANOMALOUS",
                  }))}
                  onClick={() => setSelectedHomeId(home.homeId)}
                />
              ))}

              <button
                onClick={() => setShowAddModal(true)}
                className="grid min-h-[180px] place-items-center rounded-[var(--radius-card)] border-2 border-dashed border-line bg-mist/40 text-slate transition hover:border-energy hover:bg-energy-soft/40 hover:text-energy"
              >
                <span className="flex flex-col items-center gap-2">
                  <span className="grid h-10 w-10 place-items-center rounded-full border border-current text-xl">
                    +
                  </span>
                  <span className="text-sm font-500">Yeni ev ekle</span>
                </span>
              </button>
            </div>
          ) : (
            <EmptyState onAddHome={() => setShowAddModal(true)} />
          )}
        </section>
      </main>

      {selectedHome && (
        <HomeDetailModal
          home={selectedHome}
          name={getHomeName(selectedHome.homeId)}
          status={deriveStatus(selectedHome)}
          onClose={() => setSelectedHomeId(null)}
        />
      )}

      {showAddModal && (
        <AddHomeModal onClose={() => setShowAddModal(false)} onAdded={loadHomes} />
      )}
    </div>
  );
}