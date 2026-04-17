import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'

import { BulkImportProgressBanner } from '../../features/cards/components/BulkImportProgressBanner'

describe('BulkImportProgressBanner', () => {
  it('renders cumulative saved X/Y label and failed range message', () => {
    render(
      <BulkImportProgressBanner
        runState={{
          runId: 'run-1',
          status: 'HALTED_ON_FAILURE',
          chunks: [],
          currentChunkIndex: 1,
          totalCandidates: 600,
          savedCount: 500,
          failedCount: 50,
          rowErrorsByLine: {},
          failedChunk: { index: 1, startLine: 501, endLine: 600, reason: 'HTTP:500' },
        }}
        onRetry={vi.fn()}
      />,
    )

    expect(screen.getByTestId('bulk-progress-label').textContent).toBe('saved 500/600')
    expect(screen.getByTestId('bulk-failed-range').textContent).toContain('lines 501-600')
  })
})

