interface PulseLineProps {
  // 0 = calm flat line, 1 = tall energetic spikes
  intensity?: number;
  color?: string;
  animated?: boolean;
  className?: string;
}

// the signature doodle: a hand-drawn heartbeat wave for a home's energy
// higher intensity -> taller, busier spikes. deliberately imperfect, sketch-like.
export function PulseLine({
  intensity = 0.4,
  color = "var(--color-energy)",
  animated = true,
  className = "",
}: PulseLineProps) {
  const clamped = Math.max(0, Math.min(1, intensity));
  const amp = 6 + clamped * 22; // spike height in px
  const mid = 24;

  // build an irregular pulse path so it reads as drawn, not generated
  const d = [
    `M 0 ${mid}`,
    `L 24 ${mid}`,
    `L 30 ${mid - amp * 0.35}`,
    `L 36 ${mid + amp * 0.55}`,
    `L 44 ${mid - amp}`,
    `L 52 ${mid + amp * 0.4}`,
    `L 58 ${mid}`,
    `L 92 ${mid}`,
    `L 98 ${mid - amp * 0.5}`,
    `L 106 ${mid + amp * 0.3}`,
    `L 112 ${mid}`,
    `L 160 ${mid}`,
  ].join(" ");

  return (
    <svg
      viewBox="0 0 160 48"
      className={className}
      preserveAspectRatio="none"
      role="img"
      aria-label="Enerji nabzı"
    >
      <path
        d={d}
        fill="none"
        stroke={color}
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        style={{
          strokeDasharray: animated ? 320 : undefined,
          strokeDashoffset: animated ? 320 : undefined,
          animation: animated ? "pulse-draw 2.4s ease-out forwards" : undefined,
        }}
      />
      <style>{`
        @keyframes pulse-draw {
          to { stroke-dashoffset: 0; }
        }
      `}</style>
    </svg>
  );
}
