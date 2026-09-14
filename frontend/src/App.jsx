import { useEffect, useState } from 'react'
import { getPriceHistory } from './services/api'
import PriceChart from './PriceChart'

const FUNDS = ['SPY', 'QQQ', 'VTI']

const RANGES = [
    { label: '1M', months: 1 },
    { label: '3M', months: 3 },
    { label: '6M', months: 6 },
    { label: '1Y', months: 12 },
    { label: 'Max', months: null },
]

function computeFrom(months) {
    if (months === null) return null
    const d = new Date()
    d.setMonth(d.getMonth() - months)
    return d.toISOString().slice(0, 10)
}

function App() {
    const [ticker, setTicker] = useState('SPY')
    const [data, setData] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [range, setRange] = useState('1Y')

    useEffect(() => {
        setLoading(true)
        setError(null)
        const months = RANGES.find((r) => r.label === range).months
        const from = computeFrom(months)
        getPriceHistory(ticker, from)
            .then((prices) => setData(prices))
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false))
    }, [ticker, range])

    return (
        <div className="min-h-screen bg-gray-50 text-gray-900">
            <div className="max-w-4xl mx-auto px-4 py-10">
                <h1 className="text-2xl font-bold">Trading Analytics Platform</h1>
                <p className="text-gray-500 mt-1">Historical closing prices for index-tracking ETFs.</p>

                <div className="flex gap-2 mt-6">
                    {FUNDS.map((f) => (
                        <button
                            key={f}
                            onClick={() => setTicker(f)}
                            className={
                                'px-4 py-2 rounded-md text-sm font-medium border ' +
                                (f === ticker
                                    ? 'bg-teal-700 text-white border-teal-700'
                                    : 'bg-white text-gray-700 border-gray-300 hover:border-gray-400')
                            }
                        >
                            {f}
                        </button>
                    ))}
                </div>

                <div className="flex gap-2 mt-3">
                    {RANGES.map((r) => (
                        <button
                            key={r.label}
                            onClick={() => setRange(r.label)}
                            className={
                                'px-3 py-1.5 rounded-md text-sm font-medium border ' +
                                (r.label === range
                                    ? 'bg-gray-900 text-white border-gray-900'
                                    : 'bg-white text-gray-600 border-gray-300 hover:border-gray-400')
                            }
                        >
                            {r.label}
                        </button>
                    ))}
                </div>

                <div className="mt-6 bg-white border border-gray-200 rounded-lg p-4">
                    {loading && <p className="text-gray-500 py-20 text-center">Loading {ticker}...</p>}
                    {error && <p className="text-red-600 py-20 text-center">Could not load data: {error}</p>}
                    {!loading && !error && <PriceChart data={data} />}
                </div>
            </div>
        </div>
    )
}

export default App