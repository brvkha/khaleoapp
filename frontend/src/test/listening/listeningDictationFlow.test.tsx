import React from 'react'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { DictationTab } from '../../features/listening/components/DictationTab'

const markSentenceCompleteMock = vi.fn()

vi.mock('../../features/listening/services/listeningApi', async () => {
  const actual = await vi.importActual('../../features/listening/services/listeningApi')
  return {
    ...actual,
    markSentenceComplete: (...args: unknown[]) => markSentenceCompleteMock(...args),
  }
})

const sentences = [
  {
    id: '00000000-0000-0000-0000-000000000011',
    orderIndex: 0,
    transcript: 'The cat sat on the mat',
    translation: null,
    aliasesJson: '["the cat sat on mat"]',
    mediaUrl: null,
    startTime: null,
    endTime: null,
    isCompleted: false,
  },
  {
    id: '00000000-0000-0000-0000-000000000012',
    orderIndex: 1,
    transcript: 'I am studying English now',
    translation: null,
    aliasesJson: null,
    mediaUrl: null,
    startTime: null,
    endTime: null,
    isCompleted: false,
  },
]

describe('listening dictation flow', () => {
  afterEach(() => {
    cleanup()
  })

  beforeEach(() => {
    markSentenceCompleteMock.mockReset()
    markSentenceCompleteMock.mockResolvedValue({ lessonProgressPercent: 50 })
  })

  it('checks a correct answer and sends correct_check', async () => {
    const user = userEvent.setup()
    render(<DictationTab sentences={sentences} />)

    const textarea = screen.getByPlaceholderText('Type here... (Enter to check, Esc to skip)')
    await user.type(textarea, 'The cat sat on the mat')
    await user.click(screen.getByRole('button', { name: 'Check (Enter)' }))

    await waitFor(() => {
      expect(markSentenceCompleteMock).toHaveBeenCalledWith(sentences[0].id, 'correct_check')
    })
    expect(await screen.findByText('✓ Correct!')).not.toBeNull()
  })

  it('supports Esc shortcut to skip and sends skip', async () => {
    const user = userEvent.setup()
    render(<DictationTab sentences={sentences} />)

    const textarea = screen.getByPlaceholderText('Type here... (Enter to check, Esc to skip)')
    await user.click(textarea)
    await user.keyboard('{Escape}')

    await waitFor(() => {
      expect(markSentenceCompleteMock).toHaveBeenCalledWith(sentences[0].id, 'skip')
    })
    expect(await screen.findByText(/Skipped\. Answer:/)).not.toBeNull()
  })

  it('supports Ctrl+Enter shortcut to check', async () => {
    const user = userEvent.setup()
    render(<DictationTab sentences={sentences} />)

    const textarea = screen.getByPlaceholderText('Type here... (Enter to check, Esc to skip)')
    await user.type(textarea, 'The cat sat on the mat')
    await user.keyboard('{Control>}{Enter}{/Control}')

    await waitFor(() => {
      expect(markSentenceCompleteMock).toHaveBeenCalledWith(sentences[0].id, 'correct_check')
    })
  })
})




