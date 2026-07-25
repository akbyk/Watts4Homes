import { useState, useEffect } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { fetchTrend } from "../lib/api";
import type { HistoricalTrend } from "../types/api";

interface TrendChartProps {
  homeId: number;
  color: string;
}

export function TrendChart({ homeId, color }: TrendChartProps) {
  // the trend data for this home
  const [trend, setTrend] = useState<HistoricalTrend | null>(null);
  const [loading, setLoading] = useState(true);

  // load the trend when the chart opens
  useEffect(() => {
    async function loadTrend() {
      const data = await fetchTrend(homeId);
      setTrend(data);
      setLoading(false);
    }
    loadTrend();
  }, [homeId]);

  // still loading -> grey box
  if (loading) {
    return <div className="h-48 rounded-2xl bg-mist/50" />;
  }

  // no data -> message
  if (!trend || trend.points.length === 0) {
    return (
      <div className="grid h-48 place-items-center rounded-2xl bg-mist/40 text-sm text-slate">
        Geçmiş veri bulunamadı
      </div>
    );
  }

  // prepare the data for the chart
  const chartData = trend.points.map((point) => ({
    date: point.date.slice(5), // "07-24" instead of "2026-07-24"
    usage: point.totalUsage,
  }));

  // draw the line chart
  return (
    <div className="h-48 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={chartData}>
          <XAxis dataKey="date" fontSize={11} />
          <YAxis fontSize={11} width={40} />
          <Tooltip />
          <Line dataKey="usage" stroke={color} strokeWidth={2} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}