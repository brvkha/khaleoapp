import { useEffect, useState } from 'react'
import { RichTextEditor } from '../../cards/components/RichTextEditor'
import { PlainTextPreview } from '../../cards/components/PlainTextPreview'
import { richHtmlToPlainText } from '../../cards/utils/richHtmlToPlainText'
import { useLearningStore } from '../../../store/learningStore'
import { ConfirmActionDialog } from '../components/ConfirmActionDialog'

export function AdminCardsPage() {
  const cards = useLearningStore((state) => state.cards)
  const updateCard = useLearningStore((state) => state.updateCard)
  const [frontContent, setFrontContent] = useState('')
  const [backContent, setBackContent] = useState('')

  if (!cards.length) {
    return <p className="rounded border border-slate-200 bg-white p-4">No card to moderate.</p>
  }

  const candidate = cards[0]
  const frontSummary = richHtmlToPlainText(candidate.frontContent || candidate.front)
  const backSummary = richHtmlToPlainText(candidate.backContent || candidate.back)

  useEffect(() => {
    setFrontContent(candidate.frontContent)
    setBackContent(candidate.backContent)
  }, [candidate.frontContent, candidate.backContent])

  return (
    <section>
      <h1 className="text-2xl font-semibold">Admin Card Moderation</h1>
      <div className="mt-4 max-w-md">
        <div className="mb-3 rounded border border-slate-200 bg-slate-50 p-3">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Current card preview</p>
          <PlainTextPreview content={frontSummary} lines={2} emptyText="(No front text)" className="mt-2 font-medium text-slate-900" />
          <PlainTextPreview content={backSummary} lines={2} emptyText="(No back text)" className="mt-1 text-sm text-slate-600" />
        </div>
        <div className="mb-3 space-y-2 rounded border border-slate-200 bg-white p-3">
          <RichTextEditor value={frontContent} onChange={setFrontContent} placeholder="Front rich content" />
          <RichTextEditor value={backContent} onChange={setBackContent} placeholder="Back rich content" />
        </div>
        <ConfirmActionDialog
          title={`Normalize card: ${frontSummary || 'Untitled card'}`}
          description="Admin can edit card content to enforce quality standards."
          onConfirm={() => {
            updateCard(
              candidate.id,
              frontContent.trim(),
              `${backContent.trim()} (reviewed by admin)`,
              candidate.tags,
            )
            return `Card ${frontContent} normalized by admin.`
          }}
        />
      </div>
    </section>
  )
}
