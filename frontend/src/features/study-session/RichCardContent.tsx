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
  const examples = useMemo(() => card.examples ?? [], [card.examples])

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
      {card.phonetic || card.partOfSpeech ? (
        <div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600" data-testid="rich-card-metadata">
          {card.phonetic ? <p>Phonetic: {card.phonetic}</p> : null}
          {card.partOfSpeech ? <p>Part of speech: {card.partOfSpeech}</p> : null}
        </div>
      ) : null}
      <div className="max-h-36 overflow-y-auto rounded-xl border border-slate-200 bg-slate-50/70 p-3" data-testid="rich-card-examples-scroll" ref={examplesRef}>
        {examples.length === 0 ? (
          <p className="text-sm text-slate-400">No examples</p>
        ) : (
          <ul className="list-disc space-y-1 pl-5 text-sm text-slate-700">
            {examples.map((example, index) => (
              <li key={`${card.cardId}-ex-${index}`}>{example}</li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
