const BASE_URL = import.meta.env.VITE_API_BASE_URL

export async function getPriceHistory(ticker) {
    const response = await fetch(`${BASE_URL}/api/funds/${ticker}/prices`)
    if (!response.ok) {
        throw new Error(`Failed to load prices for ${ticker} (status ${response.status})`)
    }
    return response.json()
}