import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { FullTranscriptTab } from '../../features/listening/components/FullTranscriptTab'

const sentences = [
  {
    id: '00000000-0000-0000-0000-000000000021',
    orderIndex: 0,
    transcript: 'Hello world',
    translation: null,
    aliasesJson: null,
    mediaUrl: null,
    startTime: null,
    endTime: null,
    isCompleted: false,
  },
  {
    id: '00000000-0000-0000-0000-000000000022',
    orderIndex: 1,
    transcript: 'Practice every day',
    translation: null,
    aliasesJson: null,
    mediaUrl: null,
    startTime: null,
    endTime: null,
    isCompleted: false,
  },
]

describe('transcript playback behavior', () => {
  beforeEach(() => {
    vi.spyOn(Element.prototype, 'scrollIntoView').mockImplementation(() => {})
  })

  it('auto-scrolls to current sentence when enabled', () => {
    render(
      React.createElement(FullTranscriptTab, {
        currentSentenceId: sentences[1].id,
        onReplaySentence: () => {},
        onSelectSentence: () => {},
        onWordClick: () => {},
        sentences,
        transcriptAutoScroll: true,
        transcriptLoop: false,
      }),
    )

    expect(Element.prototype.scrollIntoView).toHaveBeenCalled()
    expect(screen.getByText('Auto-scroll enabled')).not.toBeNull()
  })

  it('exposes play/select/word callbacks and loop state', async () => {
    const user = userEvent.setup()
    const onReplaySentence = vi.fn()
    const onSelectSentence = vi.fn()
    const onWordClick = vi.fn()

    render(
      React.createElement(FullTranscriptTab, {
        currentSentenceId: sentences[0].id,
        onReplaySentence,
        onSelectSentence,
        onWordClick,
        sentences,
        transcriptAutoScroll: false,
        transcriptLoop: true,
      }),
    )

    await user.click(screen.getAllByRole('button', { name: 'Play' })[0])
    expect(onReplaySentence).toHaveBeenCalledWith(sentences[0])

    await user.click(screen.getByRole('button', { name: '#1' }))
    expect(onSelectSentence).toHaveBeenCalledWith(sentences[0], 0)

    await user.click(screen.getByRole('button', { name: 'Hello' }))
    expect(onWordClick).toHaveBeenCalledWith('Hello')

    expect(screen.getByText('Loop enabled')).not.toBeNull()
  })
})


