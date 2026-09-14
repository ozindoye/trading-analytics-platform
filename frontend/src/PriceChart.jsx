import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

function PriceChart({ data }) {
    return (
        <ResponsiveContainer width="100%" height={400}>
            <LineChart data={data} margin={{ top: 10, right: 20, left: 10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="date" minTickGap={60} tick={{ fontSize: 12 }} />
                <YAxis domain={['auto', 'auto']} width={60} tick={{ fontSize: 12 }} />
                <Tooltip />
                <Line type="monotone" dataKey="close" stroke="#0f766e" dot={false} strokeWidth={2} isAnimationActive={false}/>
            </LineChart>
        </ResponsiveContainer>
    )
}

export default PriceChart