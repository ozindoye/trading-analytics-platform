export const RANGES = [
    { label: '1M', months: 1 },
    { label: '3M', months: 3 },
    { label: '6M', months: 6 },
    { label: '1Y', months: 12 },
    { label: 'Max', months: null },
]

export function computeFrom(months) {
    if (months === null) return null
    const d = new Date()
    d.setMonth(d.getMonth() - months)
    return d.toISOString().slice(0, 10)
}