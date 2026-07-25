// each appliance type maps to a hand-drawn doodle
// color is a prop, so the same icon turns coral when a device goes anomalous

type ApplianceType =
  | "FRIDGE"
  | "OVEN"
  | "KETTLE"
  | "WASHER"
  | "AC"
  | "DEFAULT";

interface ApplianceIconProps {
  type: string;
  color?: string;
  size?: number;
  className?: string;
}

// normalize whatever the backend sends into one of our known keys
function normalize(raw: string): ApplianceType {
  const t = raw.toUpperCase();
  if (t.includes("FRIDGE") || t.includes("BUZDOLAB")) return "FRIDGE";
  if (t.includes("OVEN") || t.includes("FIRIN") || t.includes("FIRIN")) return "OVEN";
  if (t.includes("KETTLE") || t.includes("KAYNA")) return "KETTLE";
  if (t.includes("WASH") || t.includes("CAMASIR") || t.includes("ÇAMAŞIR")) return "WASHER";
  if (t.includes("AC") || t.includes("KLIMA") || t.includes("COND")) return "AC";
  return "DEFAULT";
}

// shared stroke style for every doodle
function strokeProps(color: string) {
  return {
    fill: "none",
    stroke: color,
    strokeWidth: 2.2,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
  };
}

export function ApplianceIcon({
  type,
  color = "var(--color-ink)",
  size = 40,
  className = "",
}: ApplianceIconProps) {
  const key = normalize(type);
  const s = strokeProps(color);

  const paths: Record<ApplianceType, JSX.Element> = {
    FRIDGE: (
      <g {...s}>
        <rect x="10" y="4" width="24" height="40" rx="5" />
        <line x1="10" y1="20" x2="34" y2="20" />
        <line x1="28" y1="10" x2="28" y2="15" />
        <line x1="28" y1="25" x2="28" y2="34" />
      </g>
    ),
    OVEN: (
      <g {...s}>
        <rect x="6" y="6" width="36" height="38" rx="5" />
        <rect x="12" y="20" width="24" height="20" rx="4" />
        <circle cx="13" cy="12" r="1.6" fill={color} stroke="none" />
        <circle cx="24" cy="12" r="1.6" fill={color} stroke="none" />
        <circle cx="35" cy="12" r="1.6" fill={color} stroke="none" />
      </g>
    ),
    KETTLE: (
      <g {...s}>
        <path d="M12 18 Q12 40 24 40 Q36 40 36 18 Z" />
        <path d="M36 22 Q46 22 45 32" />
        <rect x="18" y="11" width="16" height="7" rx="3" />
        <line x1="26" y1="6" x2="26" y2="11" />
      </g>
    ),
    WASHER: (
      <g {...s}>
        <rect x="6" y="4" width="36" height="40" rx="5" />
        <circle cx="24" cy="27" r="11" />
        <circle cx="24" cy="27" r="5" />
        <circle cx="13" cy="11" r="1.6" fill={color} stroke="none" />
        <rect x="30" y="8" width="8" height="5" rx="2" />
      </g>
    ),
    AC: (
      <g {...s}>
        <rect x="5" y="10" width="38" height="20" rx="5" />
        <line x1="11" y1="24" x2="37" y2="24" />
        <path d="M13 36 q3 5 6 0 M21 36 q3 5 6 0 M29 36 q3 5 6 0" strokeWidth={1.8} />
      </g>
    ),
    DEFAULT: (
      <g {...s}>
        <rect x="8" y="8" width="32" height="32" rx="6" />
        <path d="M18 24 L23 29 L31 19" />
      </g>
    ),
  };

  return (
    <svg
      viewBox="0 0 48 48"
      width={size}
      height={size}
      className={className}
      role="img"
      aria-label={`${type} cihazı`}
    >
      {paths[key]}
    </svg>
  );
}