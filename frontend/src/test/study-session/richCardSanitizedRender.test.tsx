import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { StudyCardImage } from '../../components/StudyCardImage'

describe('StudyCardImage rich html sanitization', () => {
  it('strips disallowed script tag before render', () => {
    render(<StudyCardImage htmlContent="<p>Hello</p><script>alert(1)</script>" />)
    const element = screen.getByTestId('study-rich-html')
    expect(element.innerHTML).toContain('Hello')
    expect(element.innerHTML).not.toContain('<script>')
  })
})

