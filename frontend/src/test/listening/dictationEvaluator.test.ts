import { describe, expect, it } from 'vitest'
import { generateMaskDisplay, normalizeDictation } from '../../features/listening/hooks/useDictationSession'
import { maskDictationResult } from '../../features/listening/utils/maskDictationResult'
import { normalizeDictationText } from '../../features/listening/utils/normalizeDictation'

describe('dictation evaluator', () => {
  it('normalizes unicode punctuation and spaces consistently', () => {
    expect(normalizeDictation("  Hello,   WORLD!  ")).toBe('hello world')
    expect(normalizeDictationText('Good-morning!!!   Vietnam')).toBe('goodmorning vietnam')
  })

  it('masks from first mismatch onward', () => {
    expect(generateMaskDisplay('the cat sleep', 'The cat sat on the mat', false)).toBe('the cat ***')
    expect(generateMaskDisplay('the cat sat on the mat', 'The cat sat on the mat', true)).toBe('the cat sat on the mat')
  })

  it('produces token mask structure for UI rendering', () => {
    const result = maskDictationResult('The quick brown fox', 'The quick blue fox')
    expect(result.revealedTokens).toEqual(['The', 'quick'])
    expect(result.maskedTokens).toEqual(['The', 'quick', '***', '***'])
  })
})

