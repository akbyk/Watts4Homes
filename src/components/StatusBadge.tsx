// a status badge whose color and icon both encode the home's state
// one component, four looks - driven by the `status` prop

type BadgeStatus = "NORMAL" | "WARNING" | "BREACH" | "ANOMALY";

interface StatusBadgeProps {
  status: BadgeStatus;
  label?: string;
}

// each state carries its own color family, icon, and default wording
// each state carries its own color family, icon, and default wording
const config = {
  NORMAL: {
    text: "normal",
    chip: "bg-energy-soft text-energy",
    stroke: "var(--color-energy)",
    icon: <path d="M4 8 L7 11 L12 5" />,
  },
  WARNING: {
    text: "%80 uyarı",
    chip: "bg-amber-soft text-amber",
    stroke: "var(--color-amber)",
    // a little bell
    icon: <path d="M5 10 Q5 4 8 3 Q11 4 11 10 Z M6.5 12 q1.5 1.6 3 0" />,
  },
  BREACH: {
    text: "%100 aşım",
    chip: "bg-coral-soft text-coral",
    stroke: "var(--color-coral)",
    // exclamation
    icon: <path d="M8 3 L8 9 M8 12 L8 12.2" />,
  },
  ANOMALY: {
    text: "anomali",
    chip: "bg-coral-soft text-coral",
    stroke: "var(--color-coral)",
    // spark / bolt
    icon: <path d="M9 3 L4 9 L8 9 L7 13 L12 7 L8 7 Z" />,
  },
};

export function StatusBadge({ status, label }: StatusBadgeProps) {
  const c = config[status];
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-500 ${c.chip}`}
    >
      <svg
        viewBox="0 0 16 16"
        width={13}
        height={13}
        fill="none"
        stroke={c.stroke}
        strokeWidth={1.8}
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        {c.icon}
      </svg>
      {label ?? c.text}
    </span>
  );
}