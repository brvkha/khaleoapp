import type { BulkImportRunState } from '../types/bulkImportOrchestration'

type BulkImportProgressBannerProps = {
  runState: BulkImportRunState | null
  onRetry: () => void
}

export function BulkImportProgressBanner({ runState, onRetry }: BulkImportProgressBannerProps) {
  if (!runState) {
    return null
  }

  const progressLabel = `saved ${runState.savedCount}/${runState.totalCandidates}`
  const failedChunk = runState.failedChunk

  return (
    <div className="rounded border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700" data-testid="bulk-progress-banner">
      <p className="font-medium" data-testid="bulk-progress-label">{progressLabel}</p>
      {runState.status === 'HALTED_ON_FAILURE' && failedChunk ? (
        <div className="mt-1 flex items-center gap-2">
          <span data-testid="bulk-failed-range">Failed at lines {failedChunk.startLine}-{failedChunk.endLine}</span>
          <button type="button" onClick={onRetry} className="rounded border border-slate-300 px-2 py-1 text-xs hover:bg-slate-100">
            Retry failed range
          </button>
        </div>
      ) : null}
    </div>
  )
}

