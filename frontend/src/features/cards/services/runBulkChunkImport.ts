import type { BulkChunkApiResponse, BulkImportRunState } from '../types/bulkImportOrchestration'

type ExecuteChunk = (chunkIndex: number) => Promise<BulkChunkApiResponse>

type RunOptions = {
  state: BulkImportRunState
  executeChunk: ExecuteChunk
  startChunkIndex?: number
}

export async function runBulkChunkImport(options: RunOptions): Promise<BulkImportRunState> {
  const startIndex = options.startChunkIndex ?? 0
  const next: BulkImportRunState = {
    ...options.state,
    status: 'SUBMITTING_CHUNK',
    currentChunkIndex: startIndex,
    failedChunk: undefined,
    rowErrorsByLine: { ...options.state.rowErrorsByLine },
  }

  for (let index = startIndex; index < next.chunks.length; index += 1) {
    next.currentChunkIndex = index
    try {
      const result = await options.executeChunk(index)
      next.savedCount += result.successCount
      next.failedCount += result.failedCount
      for (const error of result.errors) {
        next.rowErrorsByLine[error.line] = error
      }
      next.status = 'CHUNK_COMPLETED'
    } catch (error) {
      const failed = next.chunks[index]
      next.status = 'HALTED_ON_FAILURE'
      next.failedChunk = {
        index,
        startLine: failed.startLine,
        endLine: failed.endLine,
        reason: error instanceof Error ? error.message : 'Chunk request failed',
      }
      return next
    }
  }

  next.status = 'COMPLETED'
  return next
}

