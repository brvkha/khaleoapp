import { describe, expect, it } from 'vitest'

import type { BulkChunkApiResponse } from '../../features/cards/types/bulkImportOrchestration'

describe('card bulk API contract', () => {
  it('matches bulk response envelope', () => {
    const payload: BulkChunkApiResponse = {
      successCount: 2,
      failedCount: 1,
      errors: [
        {
          line: 4,
          code: 'FRONT_REQUIRED',
          message: 'Front side required',
        },
      ],
    }

    expect(payload.successCount + payload.failedCount).toBe(3)
    expect(payload.errors[0].line).toBe(4)
  })
})

