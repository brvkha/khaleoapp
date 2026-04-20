import type { BulkRowError, ParsedBulkCandidate } from './bulkImport'

export type BulkChunk = {
  index: number
  startLine: number
  endLine: number
  cards: ParsedBulkCandidate[]
}

export type BulkChunkFailure = {
  index: number
  startLine: number
  endLine: number
  reason: string
}

export type BulkOrchestratorStatus =
  | 'IDLE'
  | 'READY_TO_SUBMIT'
  | 'SUBMITTING_CHUNK'
  | 'CHUNK_COMPLETED'
  | 'HALTED_ON_FAILURE'
  | 'COMPLETED'

export type BulkImportRunState = {
  runId: string
  status: BulkOrchestratorStatus
  chunks: BulkChunk[]
  currentChunkIndex: number
  totalCandidates: number
  savedCount: number
  failedCount: number
  rowErrorsByLine: Record<number, BulkRowError>
  failedChunk?: BulkChunkFailure
}

export type BulkChunkApiResponse = {
  successCount: number
  failedCount: number
  errors: BulkRowError[]
}

