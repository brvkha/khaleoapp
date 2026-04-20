import { describe, expect, it, vi } from 'vitest'

import { runBulkChunkImport } from '../../features/cards/services/runBulkChunkImport'

describe('runBulkChunkImport sequential behavior', () => {
  it('dispatches chunks strictly in order', async () => {
    const calls: number[] = []
    const executeChunk = vi.fn(async (chunkIndex: number) => {
      calls.push(chunkIndex)
      return { successCount: 1, failedCount: 0, errors: [] }
    })

    const state = {
      runId: 'run-1',
      status: 'READY_TO_SUBMIT' as const,
      chunks: [
        { index: 0, startLine: 1, endLine: 2, cards: [{ line: 1, frontContent: 'a', backContent: 'b' }] },
        { index: 1, startLine: 3, endLine: 4, cards: [{ line: 3, frontContent: 'c', backContent: 'd' }] },
      ],
      currentChunkIndex: 0,
      totalCandidates: 2,
      savedCount: 0,
      failedCount: 0,
      rowErrorsByLine: {},
    }

    const result = await runBulkChunkImport({ state, executeChunk })

    expect(calls).toEqual([0, 1])
    expect(result.status).toBe('COMPLETED')
  })
})

