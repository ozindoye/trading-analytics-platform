import { useEffect, useState } from 'react'
import { getComparison } from '../services/api'
import { RANGES, computeFrom } from '../ranges'
import CompareChart from './CompareChart'

const ALL_FUNDS = ['SPY', 'QQQ', 'VTI']

function CompareSection() {
    const [selected, setSelected] = useState(ALL_FUNDS)
    const [range, setRange] = useState('1Y')
    const [data, setData] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        if (selected.length === 0) {
            setData([])
            setLoading(false)
            return
        }
        setLoading(true)
        setError(null)
        const months = RANGES.find((r) => r.label === range).months
        const from = computeFrom(months)
        getComparison(selected, from)
            .then((rows) => setData(rows))
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false))
    }, [selected, range])

    function toggleFund(ticker) {
        setSelected((current) =>
            current.includes(ticker)
                ? current.filter((t) => t !== ticker)
                : [...current, ticker]
        )
    }

    return (
        <div className="mt-10">
            <h2 className="text-xl font-semibold">Compare funds</h2>
            <p className="text-gray-500 mt-1 mb-4">
                Percentage change over the selected window, rebased so each fund starts at 0%.
            </p>

            <div className="flex gap-2 mb-3">
                {ALL_FUNDS.map((f) => (
                    <button
                        key={f}
                        onClick={() => toggleFund(f)}
                        className={
                            'px-4 py-2 rounded-md text-sm font-medium border ' +
                            (selected.includes(f)
                                ? 'bg-teal-700 text-white border-teal-700'
                                : 'bg-white text-gray-700 border-gray-300 hover:border-gray-400')
                        }
                    >
                        {f}
                    </button>
                ))}
            </div>

            <div className="flex gap-2 mb-4">
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

            <div className="bg-white border border-gray-200 rounded-lg p-4">
                {selected.length === 0 && (
                    <p className="text-gray-500 py-20 text-center">Select at least one fund to compare.</p>
                )}
                {selected.length > 0 && loading && (
                    <p className="text-gray-500 py-20 text-center">Loading comparison...</p>
                )}
                {selected.length > 0 && error && (
                    <p className="text-red-600 py-20 text-center">Could not load comparison: {error}</p>
                )}
                {selected.length > 0 && !loading && !error && (
                    <CompareChart data={data} tickers={selected} />
                )}
            </div>
        </div>
    )
}

export default CompareSection