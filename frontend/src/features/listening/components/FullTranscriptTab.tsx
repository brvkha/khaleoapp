import { useEffect, useMemo, useRef } from 'react'
import type { SentenceWithProgress } from '../services/listeningApi'

type FullTranscriptTabProps = {
  sentences: SentenceWithProgress[]
  currentSentenceId: string | null
  transcriptAutoScroll: boolean
  transcriptLoop: boolean
  onSelectSentence: (sentence: SentenceWithProgress, index: number) => void
  onReplaySentence: (sentence: SentenceWithProgress) => void
  onWordClick: (word: string) => void
}

function tokenizeTranscript(text: string): string[] {
  return text
    .split(/\s+/)
    .map((token) => token.trim())
    .filter(Boolean)
}

export function FullTranscriptTab({
  sentences,
  currentSentenceId,
  transcriptAutoScroll,
  transcriptLoop,
  onSelectSentence,
  onReplaySentence,
  onWordClick,
}: FullTranscriptTabProps) {
  const activeIndex = useMemo(
    () => sentences.findIndex((sentence) => sentence.id === currentSentenceId),
    [sentences, currentSentenceId],
  )

  const activeSentenceRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    if (!transcriptAutoScroll || !activeSentenceRef.current) {
      return
    }
    activeSentenceRef.current.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  }, [activeIndex, transcriptAutoScroll])

  if (sentences.length === 0) {
    return <div className="rounded-xl border border-dashed border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">No transcript available.</div>
  }

  return (
    <section className="space-y-3 rounded-xl border border-slate-200 bg-white p-4">
      <div className="flex items-center justify-between gap-2 text-xs text-slate-500">
        <span>{transcriptAutoScroll ? 'Auto-scroll enabled' : 'Auto-scroll disabled'}</span>
        <span>{transcriptLoop ? 'Loop enabled' : 'Loop disabled'}</span>
      </div>

      <div className="max-h-80 space-y-2 overflow-y-auto pr-1">
        {sentences.map((sentence, index) => {
          const isActive = sentence.id === currentSentenceId
          return (
            <div
              className={`rounded-lg border px-3 py-2 ${isActive ? 'border-sky-300 bg-sky-50' : 'border-slate-200 bg-slate-50'}`}
              key={sentence.id}
              ref={isActive ? activeSentenceRef : undefined}
            >
              <div className="mb-2 flex items-center justify-between gap-2">
                <button
                  className="text-left text-xs font-semibold text-slate-600"
                  onClick={() => onSelectSentence(sentence, index)}
                  type="button"
                >
                  #{index + 1}
                </button>
                <button className="rounded border border-slate-300 px-2 py-1 text-xs" onClick={() => onReplaySentence(sentence)} type="button">
                  Play
                </button>
              </div>

              <p className="flex flex-wrap gap-1 text-sm leading-6 text-slate-900">
                {tokenizeTranscript(sentence.transcript).map((word, wordIndex) => (
                  <button
                    className="rounded px-1 hover:bg-slate-200"
                    key={`${sentence.id}-${wordIndex}`}
                    onClick={() => onWordClick(word.replace(/[^\p{L}\p{N}'-]/gu, ''))}
                    type="button"
                  >
                    {word}
                  </button>
                ))}
              </p>
            </div>
          )
        })}
      </div>
    </section>
  )
}

