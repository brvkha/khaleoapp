import { MAX_BULK_CHUNK_SIZE } from '../config/richCardConfig'
import type { ParsedBulkCandidate } from '../types/bulkImport'
import type { BulkChunk } from '../types/bulkImportOrchestration'

export function buildBulkChunks(candidates: ParsedBulkCandidate[]): BulkChunk[] {
  const chunks: BulkChunk[] = []
  for (let i = 0; i < candidates.length; i += MAX_BULK_CHUNK_SIZE) {
    const cards = candidates.slice(i, i + MAX_BULK_CHUNK_SIZE)
    const startLine = cards[0]?.line ?? 0
    const endLine = cards[cards.length - 1]?.line ?? startLine
    chunks.push({
      index: chunks.length,
      startLine,
      endLine,
      cards,
    })
  }
  return chunks
}

