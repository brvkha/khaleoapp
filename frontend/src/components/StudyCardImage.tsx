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
        className="prose prose-slate prose-base max-w-none rounded-2xl border border-slate-200 bg-white/95 p-4 leading-relaxed shadow-sm transition [&_audio]:w-full [&_audio]:rounded-lg [&_img]:mx-auto [&_img]:my-3 [&_img]:max-h-64 [&_img]:rounded-xl [&_ol]:my-2 [&_p]:my-0 [&_p+p]:mt-2 [&_ul]:my-2"
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
