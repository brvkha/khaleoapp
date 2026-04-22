import { useEffect, useState } from 'react'
import type { ListeningSentence, SentenceImportResponse } from '../../../listening/types/listeningApi'

type Props = {
  open: boolean
  onClose: () => void
  onImport: (rows: ListeningSentence[]) => Promise<SentenceImportResponse>
}

export function SentenceJsonImportModal({ open, onClose, onImport }: Props) {
  const [rawJson, setRawJson] = useState('[]')
  const [busy, setBusy] = useState(false)
  const [result, setResult] = useState<SentenceImportResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) {
      setRawJson('[]')
      setResult(null)
      setError(null)
      setBusy(false)
    }
  }, [open])

  if (!open) {
    return null
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-3xl rounded-2xl bg-white p-5 shadow-xl">
        <div className="flex items-center justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold text-slate-900">Import JSON sentences</h2>
            <p className="text-sm text-slate-500">Paste an array of sentence objects that matches the listening schema.</p>
          </div>
          <button className="text-slate-500 hover:text-slate-900" onClick={onClose} type="button">
            ✕
          </button>
        </div>

        <textarea
          className="mt-4 h-64 w-full rounded-lg border border-slate-300 p-3 font-mono text-sm"
          onChange={(event) => setRawJson(event.target.value)}
          value={rawJson}
        />

        {error ? <p className="mt-3 text-sm text-rose-600">{error}</p> : null}
        {result ? (
          <p className="mt-3 text-sm text-emerald-700">
            Saved {result.successCount}/{result.successCount + result.failedCount} rows.
            {result.failedCount > 0 ? ` Failed rows: ${result.failedCount}.` : ''}
          </p>
        ) : null}

        <div className="mt-4 flex items-center justify-end gap-2">
          <button className="rounded border border-slate-300 px-4 py-2" onClick={onClose} type="button">
            Close
          </button>
          <button
            className="rounded bg-slate-900 px-4 py-2 font-medium text-white disabled:opacity-50"
            disabled={busy}
            onClick={async () => {
              try {
                setBusy(true)
                setError(null)
                const parsed = JSON.parse(rawJson) as ListeningSentence[]
                if (!Array.isArray(parsed)) {
                  throw new Error('JSON must be an array of sentences.')
                }
                const next = await onImport(parsed)
                setResult(next)
              } catch (exception) {
                setError(exception instanceof Error ? exception.message : 'Import failed.')
              } finally {
                setBusy(false)
              }
            }}
            type="button"
          >
            {busy ? 'Importing...' : 'Import'}
          </button>
        </div>
      </div>
    </div>
  )
}

