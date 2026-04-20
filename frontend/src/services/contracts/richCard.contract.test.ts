import { describe, expect, it } from 'vitest'
import { validateCardContract } from './dto'

describe('rich card contract mapping', () => {
  it('accepts valid rich card payload', () => {
    expect(
      validateCardContract({
        cards: [
          {
            id: 'c1',
            deckId: 'd1',
            frontContent: '<p>Abstraction</p>',
            backContent: '<p>Generalized representation</p>',
            examples: ['Example one'],
            version: 1,
            imageUrl: null,
            partOfSpeech: null,
            phonetic: null,
          },
        ],
      }),
    ).toBe(true)
  })

  it('rejects missing required rich card fields', () => {
    expect(
      validateCardContract({
        cards: [
          {
            id: 'c2',
            deckId: 'd1',
            frontContent: '',
            backContent: '<p>Answer</p>',
            examples: [],
            version: 0,
          },
        ],
      }),
    ).toBe(false)
  })
})
