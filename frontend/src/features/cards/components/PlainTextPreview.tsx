import { richHtmlToPlainText } from '../utils/richHtmlToPlainText'

type PlainTextPreviewProps = {
  content: string | null | undefined
  lines?: number
  emptyText?: string
  className?: string
}

export function PlainTextPreview({
  content,
  lines = 2,
  emptyText = '-',
  className = '',
}: PlainTextPreviewProps) {
  const plain = richHtmlToPlainText(content)
  const displayText = plain || emptyText

  const clampStyle = lines > 0
    ? {
        display: '-webkit-box',
        WebkitLineClamp: lines,
        WebkitBoxOrient: 'vertical' as const,
        overflow: 'hidden',
      }
    : undefined

  return (
    <p
      className={`whitespace-pre-line break-words ${className}`.trim()}
      style={clampStyle}
      title={plain || undefined}
    >
      {displayText}
    </p>
  )
}

