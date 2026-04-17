import DOMPurify from 'dompurify'
import { ALLOWED_RICH_TAGS } from '../config/richCardConfig'

export function sanitizeRichHtml(input: string): string {
  return DOMPurify.sanitize(input, {
    ALLOWED_TAGS: [...ALLOWED_RICH_TAGS],
    ALLOWED_ATTR: ['class', 'src', 'alt', 'controls', 'type'],
    FORBID_ATTR: ['style'],
    ALLOW_DATA_ATTR: false,
  })
}

