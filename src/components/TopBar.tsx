interface TopBarProps {
  onAddHome?: () => void;
}

export function TopBar({ onAddHome }: TopBarProps) {
  return (
    <header className="sticky top-0 z-10 border-b border-line bg-paper/80 backdrop-blur-sm">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <a href="/" className="flex items-center gap-2.5">
          <span className="grid h-9 w-9 place-items-center rounded-2xl bg-energy-soft">
            <svg
              viewBox="0 0 24 24"
              className="h-5 w-5"
              fill="none"
              stroke="var(--color-energy)"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M13 2 3 14h7l-1 8 10-12h-7l1-8z" />
            </svg>
          </span>
          <span className="font-display text-xl font-600 tracking-tight text-ink">
            Watts4Homes
          </span>
        </a>

        <button
          onClick={onAddHome}
          className="rounded-full bg-energy px-4 py-2 text-sm font-500 text-white transition hover:brightness-105 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-energy"
        >
          Ev ekle
        </button>
      </div>
    </header>
  );
}
