import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'

const COLORS = ['#0f766e', '#b45309', '#6d28d9', '#be123c', '#1d4ed8']

function CompareChart({ data, tickers }) {
    return (
        <ResponsiveContainer width="100%" height={400}>
            <LineChart data={data} margin={{ top: 10, right: 20, left: 10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="date" minTickGap={60} tick={{ fontSize: 12 }} />
                <YAxis tickFormatter={(v) => `${v}%`} width={60} tick={{ fontSize: 12 }} />
                <Tooltip formatter={(v) => `${v}%`} />
                <Legend />
                {tickers.map((ticker, i) => (
                    <Line
                        key={ticker}
                        type="monotone"
                        dataKey={ticker}
                        stroke={COLORS[i % COLORS.length]}
                        dot={false}
                        strokeWidth={2}
                        isAnimationActive={false}
                    />
                ))}
            </LineChart>
        </ResponsiveContainer>
    )
}

export default CompareChart