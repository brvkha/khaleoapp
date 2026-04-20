const TAGS_TO_LINE_BREAK = /<\s*(br\s*\/?)\s*>|<\/\s*(p|div|li|ul|ol|h[1-6])\s*>/gi
const TAGS_TO_SPACE = /<\/?\s*(span|strong|em|u|b|i)\b[^>]*>/gi
const LI_OPEN_TAG = /<\s*li\b[^>]*>/gi
const ANY_HTML_TAG = /<[^>]+>/g

function decodeHtmlEntities(input: string): string {
  if (typeof document === 'undefined') {
    return input
  }

  const textarea = document.createElement('textarea')
  textarea.innerHTML = input
  return textarea.value
}

export function richHtmlToPlainText(input: string | null | undefined): string {
  if (!input) {
    return ''
  }

  const normalized = input.replace(/\r\n?/g, '\n')
  const withLineBreaks = normalized
    .replace(LI_OPEN_TAG, '- ')
    .replace(TAGS_TO_LINE_BREAK, '\n')
    .replace(TAGS_TO_SPACE, ' ')

  const withoutTags = withLineBreaks.replace(ANY_HTML_TAG, '')
  const decoded = decodeHtmlEntities(withoutTags)

  return decoded
    .split('\n')
    .map((line) => line.replace(/\s+/g, ' ').trim())
    .filter((line, index, lines) => line.length > 0 || (index > 0 && lines[index - 1].length > 0))
    .join('\n')
    .trim()
}

