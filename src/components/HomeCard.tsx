import { PulseLine } from "./PulseLine";
import { HouseMark } from "./HouseDoodle";
import { StatusBadge } from "./StatusBadge";
import { ApplianceIcon } from "./ApplianceIcon";

// one appliance as the card shows it: a type (for the icon) and whether it's flagged
interface CardAppliance {
  type: string;
  anomalous: boolean;
}

interface HomeCardProps {
  name: string;
  status: "NORMAL" | "WARNING" | "BREACH" | "ANOMALY";
  budgetPercent: number; // 0-100+, how much of the quota is used
  appliances: CardAppliance[];
  onClick?: () => void;
  onHide?: () => void; // stop tracking this home (local hide, no backend delete)
}

// pick the accent color from the home's overall status
const statusColor = {
  NORMAL: "var(--color-energy)",
  WARNING: "var(--color-amber)",
  BREACH: "var(--color-coral)",
  ANOMALY: "var(--color-coral)",
};

export function HomeCard({
  name,
  status,
  budgetPercent,
  appliances,
  onClick,
  onHide,
}: HomeCardProps) {
  const accent = statusColor[status];
  // pulse intensity tracks budget usage, capped at 1
  const intensity = Math.min(budgetPercent / 100, 1);

  return (
    <article
      onClick={onClick}
      className="group relative cursor-pointer rounded-[var(--radius-card)] border border-line bg-white p-5 transition hover:-translate-y-0.5 hover:shadow-[0_12px_32px_-16px_rgba(28,43,51,0.25)] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-energy"
      tabIndex={0}
      role="button"
      aria-label={`${name} detayını aç`}
    >
      {/* stop-tracking button -> appears on hover, sits in the top corner */}
      {onHide && (
        <button
          onClick={(e) => {
            // don't let the click bubble up and open the detail modal
            e.stopPropagation();
            onHide();
          }}
          aria-label={`${name} takibini bırak`}
          title="Takibi bırak"
          className="absolute right-3 top-3 z-20 grid h-7 w-7 place-items-center rounded-full bg-mist text-slate opacity-0 transition hover:bg-coral-soft hover:text-coral group-hover:opacity-100"
        >
          ✕
        </button>
      )}

      {/* header: house icon + name + status badge */}
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-center gap-2.5">
          <HouseMark color={accent} size={30} />
          <div>
            <h3 className="font-display text-lg font-500 leading-tight text-ink">
              {name}
            </h3>
            <p className="mt-0.5 text-sm text-slate">
              {appliances.length} cihaz izleniyor
            </p>
          </div>
        </div>
        <StatusBadge status={status} />
      </div>

      {/* pulse doodle - amplitude reflects budget usage */}
      <div className="my-4 h-12">
        <PulseLine intensity={intensity} color={accent} />
      </div>

      {/* appliance icons - anomalous ones turn coral */}
      <div className="mb-4 flex flex-wrap gap-3">
        {appliances.map((a, i) => (
          <ApplianceIcon
            key={i}
            type={a.type}
            size={28}
            color={a.anomalous ? "var(--color-coral)" : "var(--color-slate)"}
          />
        ))}
      </div>

      {/* footer: budget usage as a real percentage */}
      <div className="flex items-center justify-between border-t border-line pt-3 text-sm">
        <span className="text-slate">Bütçe kullanımı</span>
        <span className="font-mono font-500" style={{ color: accent }}>
          %{Math.round(budgetPercent)}
        </span>
      </div>
    </article>
  );
}