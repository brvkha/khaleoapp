export type BulkImportSeparator = 'tab' | 'comma'

export type ParsedBulkCandidate = {
  line: number
  frontContent: string
  backContent: string
}

export type ParsedBulkRejectedRow = {
  line: number
  raw: string
  code: 'ROW_TOO_FEW_COLUMNS' | 'FRONT_REQUIRED' | 'BACK_REQUIRED'
  message: string
}

export type ParsedBulkResult = {
  candidates: ParsedBulkCandidate[]
  rejected: ParsedBulkRejectedRow[]
  totalNonBlankLines: number
}

export type BulkRowErrorCode =
  | 'ROW_TOO_FEW_COLUMNS'
  | 'FRONT_REQUIRED'
  | 'BACK_REQUIRED'
  | 'MEDIA_DOMAIN_NOT_ALLOWED'
  | 'HTML_UNRECOVERABLE'

export type BulkRowError = {
  line: number
  code: BulkRowErrorCode
  message: string
}

