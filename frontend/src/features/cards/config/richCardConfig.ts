export const MAX_BULK_CHUNK_SIZE = 500
export const BULK_PREVIEW_LIMIT = 100

export const ALLOWED_RICH_TAGS = [
  'p',
  'br',
  'strong',
  'em',
  'u',
  'img',
  'audio',
  'source',
  'ul',
  'ol',
  'li',
  'span',
  'div',
] as const

export const BULK_ERROR_CODES = [
  'ROW_TOO_FEW_COLUMNS',
  'FRONT_REQUIRED',
  'BACK_REQUIRED',
  'MEDIA_DOMAIN_NOT_ALLOWED',
  'HTML_UNRECOVERABLE',
] as const

