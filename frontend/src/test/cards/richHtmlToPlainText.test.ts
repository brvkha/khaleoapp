import { describe, expect, it } from 'vitest'

import { richHtmlToPlainText } from '../../features/cards/utils/richHtmlToPlainText'

describe('richHtmlToPlainText', () => {
  it('strips tags and keeps line breaks for block content', () => {
    const input = '<p>Apple</p><p>Qua tao<br/>Trai cay</p>'

    expect(richHtmlToPlainText(input)).toBe('Apple\nQua tao\nTrai cay')
  })

  it('decodes entities and handles list bullets', () => {
    const input = '<ul><li>One &amp; Two</li><li>Three</li></ul>'

    expect(richHtmlToPlainText(input)).toBe('- One & Two\n- Three')
  })

  it('returns empty string for nullish input', () => {
    expect(richHtmlToPlainText(undefined)).toBe('')
    expect(richHtmlToPlainText(null)).toBe('')
  })
})

