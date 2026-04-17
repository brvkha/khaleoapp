import { describe, expect, it } from 'vitest'

import { runBulkChunkImport } from '../../features/cards/services/runBulkChunkImport'

describe('runBulkChunkImport failure and retry behavior', () => {
  it('halts on first request-level failure and captures failed chunk range', async () => {
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

    const result = await runBulkChunkImport({
      state,
      executeChunk: async (index) => {
        if (index === 1) {
          throw new Error('timeout')
        }
        return { successCount: 1, failedCount: 0, errors: [] }
      },
    })

    expect(result.status).toBe('HALTED_ON_FAILURE')
    expect(result.failedChunk?.startLine).toBe(3)
    expect(result.savedCount).toBe(1)
  })
})

