import { useEffect, useMemo, useRef } from 'react'
import { StudyCardImage } from '../../components/StudyCardImage'
import type { StudySessionCardDto } from '../../services/studySessionApi'

type RichCardContentProps = {
  card: StudySessionCardDto
  revealed: boolean
}

export function RichCardContent({ card, revealed }: RichCardContentProps) {
  const examplesRef = useRef<HTMLDivElement | null>(null)

  const frontContent = useMemo(() => card.frontContent ?? card.frontText, [card.frontContent, card.frontText])
  const backContent = useMemo(() => card.backContent ?? card.backText, [card.backContent, card.backText])
  const term = useMemo(() => card.term ?? frontContent, [frontContent, card.term])
  const examples = useMemo(
    () =>
      ((card.examples ?? []) as Array<string | { text?: string } | null>)
        .map((example) => (typeof example === 'string' ? example : example?.text ?? ''))
        .map((example) => example.trim())
        .filter(Boolean),
    [card.examples],
  )

  useEffect(() => {
    if (examplesRef.current) {
      examplesRef.current.scrollTop = 0
    }
  }, [revealed, card.cardId])

  if (!revealed) {
    return (
      <div className="space-y-4">
        <div className="rounded-2xl border border-indigo-100 bg-gradient-to-br from-indigo-50 to-white p-3">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-indigo-500">Front</p>
          <div className="min-h-48 flex items-center justify-center text-center">
            <StudyCardImage htmlContent={frontContent} />
          </div>
        </div>
        {card.imageUrl ? <StudyCardImage imageUrl={card.imageUrl} alt={term} /> : null}
      </div>
    )
  }

  return (
    <div className="space-y-4" data-testid="rich-card-back-content">
      <div className="rounded-2xl border border-indigo-100 bg-gradient-to-br from-indigo-50 to-white p-3">
        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-indigo-500">Front</p>
        <StudyCardImage htmlContent={frontContent} />
      </div>
      {card.imageUrl ? <StudyCardImage imageUrl={card.imageUrl} alt={term} /> : null}
      <div className="rounded-2xl border border-emerald-100 bg-gradient-to-br from-emerald-50 to-white p-3">
        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-emerald-600">Back</p>
        <StudyCardImage htmlContent={backContent} />
      </div>
      {examples.length > 0 ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-3">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">Examples</p>
          <div ref={examplesRef} data-testid="rich-card-examples-scroll" className="max-h-40 space-y-2 overflow-y-auto">
            {examples.map((example) => (
              <p className="text-sm leading-6 text-slate-700" key={example}>
                {example}
              </p>
            ))}
          </div>
        </div>
      ) : null}
      {card.phonetic || card.partOfSpeech ? (
        <div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600" data-testid="rich-card-metadata">
          {card.phonetic ? <p>Phonetic: {card.phonetic}</p> : null}
          {card.partOfSpeech ? <p>Part of speech: {card.partOfSpeech}</p> : null}
        </div>
      ) : null}
    </div>
  )
}
