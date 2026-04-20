import { useState } from 'react'
import { sanitizeRichHtml } from '../features/cards/utils/sanitizeRichHtml'

type StudyCardImageProps = {
  imageUrl?: string
  alt?: string
  htmlContent?: string
}

export function StudyCardImage({ imageUrl, alt = 'card media', htmlContent }: StudyCardImageProps) {
  const [failed, setFailed] = useState(false)

  if (htmlContent) {
    const safeHtml = sanitizeRichHtml(htmlContent)
    return (
      <div
        className="prose max-w-none rounded border border-slate-200 bg-white p-3"
        data-testid="study-rich-html"
        dangerouslySetInnerHTML={{ __html: safeHtml }}
      />
    )
  }

  if (!imageUrl || failed) {
    return (
      <div
        className="flex h-48 w-full items-center justify-center rounded border border-slate-300 bg-slate-100 text-slate-500"
        data-testid="study-image-fallback"
      >
        <span aria-hidden="true" className="text-xl">!</span>
      </div>
    )
  }

  return (
    <img
      src={imageUrl}
      alt={alt}
      className="h-auto max-h-64 w-full rounded object-contain"
      onError={() => setFailed(true)}
      data-testid="study-image"
    />
  )
}
