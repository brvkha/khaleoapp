import { type FormEvent, useMemo, useState } from 'react'
import { BulkImportProgressBanner } from '../features/cards/components/BulkImportProgressBanner'
import { RichTextEditor } from '../features/cards/components/RichTextEditor'
import { runBulkChunkImport } from '../features/cards/services/runBulkChunkImport'
import { buildBulkChunks } from '../features/cards/utils/buildBulkChunks'
import { parseBulkCardInput } from '../features/cards/utils/parseBulkCardInput'
import { bulkCreateCardsChunk } from '../services/cardBulkApi'
import { useAddCardModalStore } from '../store/addCardModalStore'

interface AddCardModalProps {
  isOpen: boolean
  onClose: () => void
  onSubmit: (front: string, back: string) => void
  deckName: string
  deckId?: string
}

export function AddCardModal({ isOpen, onClose, onSubmit, deckName, deckId }: AddCardModalProps) {
  const [front, setFront] = useState('')
  const [back, setBack] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const {
    mode,
    setMode,
    bulkRawText,
    setBulkRawText,
    separator,
    setSeparator,
    parsedCandidates,
    setParsedCandidates,
    runState,
    setRunState,
    retryFromChunkIndex,
    setRetryFromChunkIndex,
    resetBulk,
  } = useAddCardModalStore()

  const parsedPreview = useMemo(() => parseBulkCardInput(bulkRawText, separator), [bulkRawText, separator])

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!front.trim() || !back.trim()) return

    setIsLoading(true)
    try {
      await Promise.resolve(onSubmit(front.trim(), back.trim()))
      setFront('')
      setBack('')
      onClose()
    } finally {
      setIsLoading(false)
    }
  }

  const handleParseBulk = () => {
    setParsedCandidates(parsedPreview.candidates)
  }

  const runBulkImport = async (startAt = 0) => {
    if (!deckId) {
      return
    }
    const chunks = buildBulkChunks(parsedCandidates)
    const initialState = {
      runId: crypto.randomUUID(),
      status: 'READY_TO_SUBMIT' as const,
      chunks,
      currentChunkIndex: 0,
      totalCandidates: parsedCandidates.length,
      savedCount: startAt === 0 ? 0 : runState?.savedCount ?? 0,
      failedCount: startAt === 0 ? 0 : runState?.failedCount ?? 0,
      rowErrorsByLine: startAt === 0 ? {} : runState?.rowErrorsByLine ?? {},
      failedChunk: undefined,
    }

    const completed = await runBulkChunkImport({
      state: initialState,
      startChunkIndex: startAt,
      executeChunk: async (index) => {
        return bulkCreateCardsChunk(deckId, chunks[index].cards)
      },
    })
    setRunState(completed)
    if (completed.status === 'HALTED_ON_FAILURE' && completed.failedChunk) {
      setRetryFromChunkIndex(completed.failedChunk.index)
      return
    }
    if (completed.failedCount === 0) {
      resetBulk()
      onClose()
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="w-full max-w-3xl rounded-lg bg-white shadow-lg">
        <div className="border-b border-slate-200 px-6 py-4">
          <h2 className="text-xl font-semibold text-slate-900">Add Card to {deckName}</h2>
          <p className="mt-1 text-sm text-slate-600">Create single rich card or import many cards in bulk</p>
        </div>

        <div className="p-6 space-y-4">
          <div className="flex gap-2">
            <button type="button" className={`rounded px-3 py-1 text-sm ${mode === 'single' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-700'}`} onClick={() => setMode('single')}>Single card</button>
            <button type="button" className={`rounded px-3 py-1 text-sm ${mode === 'bulk' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-700'}`} onClick={() => setMode('bulk')}>Bulk import</button>
          </div>

          {mode === 'single' ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Front content</label>
                <RichTextEditor value={front} onChange={setFront} placeholder="Front canonical HTML" />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">Back content</label>
                <RichTextEditor value={back} onChange={setBack} placeholder="Back canonical HTML" />
              </div>
              <button type="submit" disabled={!front.trim() || !back.trim() || isLoading} className="rounded bg-emerald-600 px-6 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-50">
                {isLoading ? 'Adding...' : 'Add Card'}
              </button>
            </form>
          ) : (
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm">
                <label>Separator</label>
                <select className="rounded border border-slate-300 px-2 py-1" value={separator} onChange={(e) => setSeparator(e.target.value as 'tab' | 'comma')}>
                  <option value="tab">Tab</option>
                  <option value="comma">Comma</option>
                </select>
              </div>
              <textarea
                className="min-h-40 w-full rounded border border-slate-300 px-3 py-2 text-sm"
                value={bulkRawText}
                onChange={(event) => setBulkRawText(event.target.value)}
                placeholder="Paste spreadsheet rows"
              />
              <div className="flex gap-2">
                <button type="button" className="rounded border border-slate-300 px-3 py-1 text-sm hover:bg-slate-50" onClick={handleParseBulk}>Preview</button>
                <button type="button" className="rounded bg-emerald-600 px-3 py-1 text-sm text-white disabled:opacity-50" disabled={!parsedCandidates.length || !deckId} onClick={() => runBulkImport(0)}>Import</button>
              </div>
              <p className="text-xs text-slate-500">Candidates: {parsedPreview.candidates.length} | Rejected: {parsedPreview.rejected.length}</p>
              <BulkImportProgressBanner runState={runState} onRetry={() => runBulkImport(retryFromChunkIndex ?? 0)} />
            </div>
          )}

          <div className="flex justify-end gap-3 pt-4 border-t border-slate-200">
            <button
              type="button"
              onClick={onClose}
              className="rounded border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              disabled={isLoading}
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
