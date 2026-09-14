import { useEffect, useState } from 'react'
import { getPriceHistory } from './services/api'
import PriceChart from './PriceChart'

const FUNDS = ['SPY', 'QQQ', 'VTI']

function App() {
    const [ticker, setTicker] = useState('SPY')
    const [data, setData] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        setLoading(true)
        setError(null)
        getPriceHistory(ticker)
            .then((prices) => setData(prices))
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false))
    }, [ticker])

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