import { useState, useEffect } from "react";
import { getMetrics } from "../services/api";

export default function StatCards({ ticker, from }) {
    const [metrics, setMetrics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!ticker) return;

        let cancelled = false;
        setLoading(true);
        setError(null);

        getMetrics(ticker, from)
            .then((data) => {
                if (!cancelled) {
                    setMetrics(data);
                    setLoading(false);
                }
            })
            .catch((err) => {
                if (!cancelled) {
                    setError(err.message);
                    setLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, [ticker, from]);

    if (loading) {
        return <p className="text-sm text-gray-500">Loading metrics…</p>;
    }

    if (error) {
        return <p className="text-sm text-red-600">Could not load metrics: {error}</p>;
    }

    if (!metrics) {
        return null;
    }

    const cards = [
        { label: "Period return", value: formatPercent(metrics.periodReturnPct), tone: signTone(metrics.periodReturnPct) },
        { label: "Annualised volatility", value: formatPercent(metrics.annualisedVolatilityPct) },
        { label: "Max drawdown", value: formatDrawdown(metrics.maxDrawdownPct), tone: "negative" },
        { label: "Sharpe ratio", value: formatNumber(metrics.sharpeRatio) },
        { label: "Period high", value: formatPrice(metrics.high) },
        { label: "Period low", value: formatPrice(metrics.low) },
    ];

    return (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
            {cards.map((card) => (
                <div key={card.label} className="rounded-lg border border-gray-200 bg-white p-4">
                    <p className="text-sm text-gray-500">{card.label}</p>
                    <p className={`mt-1 text-xl font-semibold ${toneClass(card.tone)}`}>{card.value}</p>
                </div>
            ))}
        </div>
    );
}

function formatPercent(value) {
    if (value === null || value === undefined) return "–";
    return `${Number(value).toFixed(2)}%`;
}

function formatPrice(value) {
    if (value === null || value === undefined) return "–";
    return `$${Number(value).toFixed(2)}`;
}

function formatNumber(value) {
    if (value === null || value === undefined) return "–";
    return Number(value).toFixed(2);
}

function formatDrawdown(value) {
    if (value === null || value === undefined) return "–";
    const n = Number(value);
    return n === 0 ? "0.00%" : `-${n.toFixed(2)}%`;
}

function signTone(value) {
    if (value === null || value === undefined) return "neutral";
    return Number(value) >= 0 ? "positive" : "negative";
}

function toneClass(tone) {
    if (tone === "positive") return "text-green-600";
    if (tone === "negative") return "text-red-600";
    return "text-gray-900";
}