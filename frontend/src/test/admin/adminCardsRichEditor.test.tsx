import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { AdminCardsPage } from '../../features/admin/cards/AdminCardsPage'

describe('AdminCardsPage rich editor', () => {
  it('renders moderation page heading', () => {
    render(<AdminCardsPage />)
    expect(screen.getByText('Admin Card Moderation')).toBeInTheDocument()
  })
})

