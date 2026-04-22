const STRICT_NORMALIZE_REGEX = /[\p{P}\p{S}]/gu

export function normalizeDictationText(value: string): string {
  return value
    .normalize('NFKC')
    .toLowerCase()
    .replace(STRICT_NORMALIZE_REGEX, '')
    .replace(/\s+/g, ' ')
    .trim()
}

export function normalizeAliasMap(aliasMap: Record<string, string>): Record<string, string> {
  return Object.entries(aliasMap).reduce<Record<string, string>>((accumulator, [key, value]) => {
    const normalizedKey = normalizeDictationText(key)
    const normalizedValue = normalizeDictationText(value)
    if (normalizedKey) {
      accumulator[normalizedKey] = normalizedValue
    }
    return accumulator
  }, {})
}


