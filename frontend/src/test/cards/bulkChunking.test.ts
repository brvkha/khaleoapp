import { describe, expect, it } from 'vitest'

import { buildBulkChunks } from '../../features/cards/utils/buildBulkChunks'

function candidate(line: number) {
  return { line, frontContent: `<p>f-${line}</p>`, backContent: `<p>b-${line}</p>` }
}

describe('buildBulkChunks', () => {
  it('splits >500 candidates into bounded chunks preserving line ranges', () => {
    const candidates = Array.from({ length: 501 }, (_, index) => candidate(index + 1))
    const chunks = buildBulkChunks(candidates)

    expect(chunks).toHaveLength(2)
    expect(chunks[0].cards).toHaveLength(500)
    expect(chunks[0].startLine).toBe(1)
    expect(chunks[0].endLine).toBe(500)
    expect(chunks[1].cards).toHaveLength(1)
    expect(chunks[1].startLine).toBe(501)
  })
})

