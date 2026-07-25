import { useEffect, useState } from "react";
import type { ApplianceRequest } from "../types/api";
import { registerHome } from "../lib/api";

interface AddHomeModalProps {
  onClose: () => void;
  onAdded: () => void; // called after a successful registration so the grid refetches
}

// appliance presets the user can pick, with sensible default watt limits
// limits sit above the simulator's per-second output so appliances stay
// normal -> the card color is then driven by budget, not constant anomalies
const APPLIANCE_PRESETS = [
  { type: "FRIDGE", name: "Buzdolabı", safeLimitWatts: 10000 },
  { type: "OVEN", name: "Fırın", safeLimitWatts: 15000 },
  { type: "KETTLE", name: "Su Isıtıcısı", safeLimitWatts: 12000 },
  { type: "WASHER", name: "Çamaşır Makinesi", safeLimitWatts: 12000 },
  { type: "AC", name: "Klima", safeLimitWatts: 13000 },
];

export function AddHomeModal({ onClose, onAdded }: AddHomeModalProps) {
  const [name, setName] = useState("");
  const [address, setAddress] = useState("");
  const [email, setEmail] = useState("");
  const [budgetQuota, setBudgetQuota] = useState("2000");
  const [appliances, setAppliances] = useState<ApplianceRequest[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  function addAppliance(preset: ApplianceRequest) {
    // copy the current list, add the new appliance, then save it back
    const newList = [...appliances, preset];
    setAppliances(newList);
  }

  function removeAppliance(index: number) {
    // build a new list with everything except the item at this index
    const newList = [];
    for (let i = 0; i < appliances.length; i++) {
      if (i !== index) {
        newList.push(appliances[i]);
      }
    }
    setAppliances(newList);
  }

  // basic validation before allowing submit
  const isValid =
    name.trim().length > 0 &&
    email.trim().length > 0 &&
    Number(budgetQuota) > 0 &&
    appliances.length > 0;

  async function handleSubmit() {
    if (!isValid) return;
    setSubmitting(true);
    setErrorMsg(null);
    try {
      await registerHome({
        name: name.trim(),
        address: address.trim() || undefined,
        contactEmail: email.trim(),
        budgetQuota: Number(budgetQuota),
        currentRate: 100,
        penaltyRate: 1000,
        appliances,
      });
      onAdded();
      onClose();
    } catch (e) {
      setErrorMsg((e as Error).message);
      setSubmitting(false);
    }
  }

  const inputClass =
    "w-full rounded-xl border border-line bg-white px-3 py-2 text-sm text-ink outline-none transition focus:border-energy focus:ring-2 focus:ring-energy/20";

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 p-4 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="max-h-[88vh] w-full max-w-lg overflow-y-auto rounded-[var(--radius-card)] bg-paper p-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between">
          <h2 className="font-display text-2xl font-500 text-ink">Yeni ev ekle</h2>
          <button
            onClick={onClose}
            aria-label="Kapat"
            className="grid h-8 w-8 place-items-center rounded-full text-slate transition hover:bg-mist hover:text-ink"
          >
            ✕
          </button>
        </div>

        {/* home fields */}
        <div className="mt-5 space-y-3">
          <div>
            <label className="mb-1 block text-sm font-500 text-ink">Ev adı</label>
            <input
              className={inputClass}
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Kadıköy Dairesi"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-500 text-ink">Adres</label>
            <input
              className={inputClass}
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              placeholder="Kadıköy, İstanbul"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-500 text-ink">
              İletişim e-postası
            </label>
            <input
              className={inputClass}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ornek@eposta.com"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-500 text-ink">
              Bütçe kotası (kWh)
            </label>
            <input
              className={inputClass}
              type="number"
              step="0.1"
              value={budgetQuota}
              onChange={(e) => setBudgetQuota(e.target.value)}
            />
          </div>
        </div>

        {/* appliance picker */}
        <h3 className="mb-2 mt-6 font-display text-lg font-500 text-ink">
          Cihazlar
        </h3>
        <div className="mb-3 flex flex-wrap gap-2">
          {APPLIANCE_PRESETS.map((p) => (
            <button
              key={p.type}
              onClick={() => addAppliance(p)}
              className="rounded-full border border-line bg-white px-3 py-1.5 text-xs font-500 text-ink transition hover:border-energy hover:bg-energy-soft/40 hover:text-energy"
            >
              + {p.name}
            </button>
          ))}
        </div>

        {/* selected appliances */}
        {appliances.length === 0 ? (
          <p className="rounded-xl bg-mist/50 px-3 py-3 text-center text-sm text-slate">
            En az bir cihaz ekleyin
          </p>
        ) : (
          <div className="space-y-2">
            {appliances.map((a, i) => (
              <div
                key={i}
                className="flex items-center justify-between rounded-xl border border-line bg-white px-3 py-2 text-sm"
              >
                <span className="text-ink">{a.name}</span>
                <div className="flex items-center gap-3">
                  <span className="font-mono text-xs text-slate">
                    {a.safeLimitWatts} W
                  </span>
                  <button
                    onClick={() => removeAppliance(i)}
                    aria-label="Kaldır"
                    className="text-slate transition hover:text-coral"
                  >
                    ✕
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* error */}
        {errorMsg && (
          <p className="mt-4 rounded-xl bg-coral-soft/60 px-3 py-2 text-sm text-coral">
            {errorMsg}
          </p>
        )}

        {/* actions */}
        <div className="mt-6 flex justify-end gap-3">
          <button
            onClick={onClose}
            className="rounded-full px-4 py-2 text-sm font-500 text-slate transition hover:bg-mist"
          >
            İptal
          </button>
          <button
            onClick={handleSubmit}
            disabled={!isValid || submitting}
            className="rounded-full bg-energy px-5 py-2 text-sm font-500 text-white transition hover:brightness-105 disabled:cursor-not-allowed disabled:opacity-40"
          >
            {submitting ? "Kaydediliyor…" : "Evi kaydet"}
          </button>
        </div>
      </div>
    </div>
  );
}