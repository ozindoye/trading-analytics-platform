const BASE_URL = import.meta.env.VITE_API_BASE_URL

export async function getPriceHistory(ticker, from) {
    const url = new URL(`${BASE_URL}/api/funds/${ticker}/prices`)
    if (from) {
        url.searchParams.set('from', from)
    }
    const response = await fetch(url)
    if (!response.ok) {
        throw new Error(`Failed to load prices for ${ticker} (status ${response.status})`)
    }
    return response.json()
}

export async function getComparison(tickers, from) {
    const url = new URL(`${BASE_URL}/api/funds/compare`)
    url.searchParams.set('tickers', tickers.join(','))
    if (from) {
        url.searchParams.set('from', from)
    }
    const response = await fetch(url)
    if (!response.ok) {
        throw new Error(`Failed to load comparison (status ${response.status})`)
    }
    return response.json()
}

export async function getMetrics(ticker, from) {
    const url = new URL(`${BASE_URL}/api/funds/${ticker}/metrics`)
    if (from) {
        url.searchParams.set('from', from)
    }
    const response = await fetch(url)
    if (!response.ok) {
        throw new Error(`Failed to load metrics for ${ticker} (status ${response.status})`)
    }
    return response.json()
}