import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { AddCardModal } from '../../components/AddCardModal'
import { useAddCardModalStore } from '../../store/addCardModalStore'

describe('AddCardModal bulk preview', () => {
  it('updates preview rows in real time and shows rejected row reasons', () => {
    useAddCardModalStore.setState({
      mode: 'bulk',
      bulkRawText: '',
      separator: 'tab',
      parsedCandidates: [],
      runState: null,
      retryFromChunkIndex: null,
    })

    render(
      <AddCardModal
        isOpen
        onClose={() => {}}
        onSubmit={() => {}}
        deckName="Deck"
        deckId="deck-1"
      />,
    )

    const textarea = screen.getByPlaceholderText('Paste spreadsheet rows')
    fireEvent.change(textarea, {
      target: {
        value: 'Apple\tQua tao\nInvalidOnlyOneColumn\n\tMissing front\nCar\tXe hoi',
      },
    })

    const preview = screen.getByTestId('bulk-preview-list')
    expect(preview.textContent).toContain('Line 1')
    expect(preview.textContent).toContain('Front: Apple')
    expect(preview.textContent).toContain('Line 4')

    const rejected = screen.getByTestId('bulk-preview-rejected')
    expect(rejected.textContent).toContain('Line 2')
    expect(rejected.textContent).toContain('Line 3')
  })
})

