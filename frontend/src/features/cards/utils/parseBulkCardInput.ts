import type { BulkImportSeparator, ParsedBulkResult } from '../types/bulkImport'

export function parseBulkCardInput(rawText: string, separator: BulkImportSeparator): ParsedBulkResult {
  const normalized = rawText.replace(/\r\n/g, '\n')
  const lines = normalized.split('\n')
  const delimiter = separator === 'tab' ? '\t' : ','

  const candidates: ParsedBulkResult['candidates'] = []
  const rejected: ParsedBulkResult['rejected'] = []
  let totalNonBlankLines = 0

  lines.forEach((raw, index) => {
    const line = index + 1
    if (!raw.trim()) {
      return
    }

    totalNonBlankLines += 1
    const columns = raw.split(delimiter)
    if (columns.length < 2) {
      rejected.push({
        line,
        raw,
        code: 'ROW_TOO_FEW_COLUMNS',
        message: 'Expected at least 2 columns: front and back.',
      })
      return
    }

    const frontContent = columns[0]?.trim() ?? ''
    const backContent = columns.slice(1).join('\n').trim()
    candidates.push({ line, frontContent, backContent })
  })

  return { candidates, rejected, totalNonBlankLines }
}

