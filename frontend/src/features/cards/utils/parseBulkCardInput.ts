import type { BulkImportSeparator, ParsedBulkResult } from '../types/bulkImport'

export function parseBulkCardInput(rawText: string, separator: BulkImportSeparator): ParsedBulkResult {
  const normalized = rawText.replace(/\r\n?/g, '\n')
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
    const columns = raw.split(delimiter).map((column) => column.trim())
    if (columns.length < 2) {
      rejected.push({
        line,
        raw,
        code: 'ROW_TOO_FEW_COLUMNS',
        message: 'Expected at least 2 columns: front and back.',
      })
      return
    }

    const frontContent = columns[0] ?? ''
    if (!frontContent) {
      rejected.push({
        line,
        raw,
        code: 'FRONT_REQUIRED',
        message: `Missing front content at line ${line}.`,
      })
      return
    }

    const backColumns = columns.slice(1)
    const hasBackText = backColumns.some((column) => column.length > 0)
    if (!hasBackText) {
      rejected.push({
        line,
        raw,
        code: 'BACK_REQUIRED',
        message: `Missing back content at line ${line}.`,
      })
      return
    }

    // Preserve intentionally empty middle cells when joining multiple back columns.
    const backContent = backColumns.join('\n')
    candidates.push({ line, frontContent, backContent })
  })

  return { candidates, rejected, totalNonBlankLines }
}

