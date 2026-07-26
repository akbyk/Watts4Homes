// two house doodles in one file:
// HouseMark - a small house icon for card headers
// EmptyState - the friendly welcome scene when no homes exist yet

interface HouseMarkProps {
  color?: string;
  size?: number;
  className?: string;
}

// small filled house, meant to sit next to a card title
export function HouseMark({
  color = "var(--color-energy)",
  size = 34,
  className = "",
}: HouseMarkProps) {
  return (
    <svg
      viewBox="0 0 48 48"
      width={size}
      height={size}
      className={className}
      role="img"
      aria-label="Ev"
    >
      <path
        d="M8 24 L24 10 L40 24 L40 40 L8 40 Z"
        fill="none"
        stroke={color}
        strokeWidth={2.4}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <rect
        x="20"
        y="28"
        width="8"
        height="12"
        rx="1.5"
        fill="none"
        stroke={color}
        strokeWidth={2.4}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

interface EmptyStateProps {
  onAddHome?: () => void;
}

// shown on the dashboard before the first home is registered
export function EmptyState({ onAddHome }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center rounded-[var(--radius-card)] border-2 border-dashed border-line bg-mist/30 px-6 py-16 text-center">
      <svg
        viewBox="0 0 200 130"
        width={220}
        height={143}
        role="img"
        aria-label="Henüz ev yok"
      >
        {/* ground line */}
        <path
          d="M30 108 q70 -14 140 0"
          fill="none"
          stroke="var(--color-energy)"
          strokeWidth={2}
          strokeLinecap="round"
          opacity={0.4}
        />
        {/* house */}
        <path
          d="M62 78 L100 44 L138 78 L138 108 L62 108 Z"
          fill="var(--color-energy-soft)"
          stroke="var(--color-energy)"
          strokeWidth={2.4}
          strokeLinejoin="round"
        />
        <rect
          x="90"
          y="88"
          width="20"
          height="20"
          rx="2"
          fill="none"
          stroke="var(--color-energy)"
          strokeWidth={2.4}
        />
        {/* floating plus */}
        <g transform="translate(146,46)">
          <circle cx="0" cy="0" r="16" fill="var(--color-energy-soft)" />
          <line x1="-7" y1="0" x2="7" y2="0" stroke="var(--color-energy)" strokeWidth={2.4} strokeLinecap="round" />
          <line x1="0" y1="-7" x2="0" y2="7" stroke="var(--color-energy)" strokeWidth={2.4} strokeLinecap="round" />
        </g>
        {/* little cloud */}
        <path
          d="M36 40 q8 -10 20 -4 q10 -6 16 6"
          fill="none"
          stroke="var(--color-deep)"
          strokeWidth={2}
          strokeLinecap="round"
          opacity={0.45}
        />
      </svg>

      <h3 className="mt-6 font-display text-xl font-500 text-ink">
        Henüz ev yok
      </h3>
      <p className="mt-2 max-w-sm text-slate">
        İlk evinizi ekleyin, canlı enerji takibini hemen başlatalım.
      </p>
      <button
        onClick={onAddHome}
        className="mt-5 rounded-full bg-energy px-5 py-2.5 text-sm font-500 text-white transition hover:brightness-105 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-energy"
      >
        İlk evi ekle
      </button>
    </div>
  );
}