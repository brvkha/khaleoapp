import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { PlainTextPreview } from '../../features/cards/components/PlainTextPreview'

describe('PlainTextPreview', () => {
  it('renders stripped plain text with tooltip from rich html', () => {
    render(<PlainTextPreview content={'<p>Hello <strong>World</strong></p>'} />)

    const text = screen.getByText('Hello World')
    expect(text).toBeInTheDocument()
    expect(text).toHaveAttribute('title', 'Hello World')
  })

  it('renders fallback text when content is empty', () => {
    render(<PlainTextPreview content={''} emptyText="(empty)" />)

    expect(screen.getByText('(empty)')).toBeInTheDocument()
  })
})

