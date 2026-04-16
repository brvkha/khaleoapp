import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { StudySessionPage } from '../../features/study-session/StudySessionPage'
import { useFolderStore } from '../../store/folderStore'

vi.mock('../../services/studySessionApi', () => ({
  getNextSessionCards: vi.fn(async () => [
    {
      cardId: 'c1',
      deckId: 'd1',
      term: 'Abstraction',
      answer: 'Generalized representation',
      imageUrl: 'https://images.unsplash.com/photo-1',
      partOfSpeech: 'noun',
      phonetic: 'ab-strak-shun',
      examples: ['Example one'],
      frontText: 'Abstraction',
      backText: 'Generalized representation',
      state: 'NEW',
      nextReviewDate: null,
      sourceTier: 'NEW',
    },
  ]),
  previewSessionCardRatings: vi.fn(async () => ({
    again: { nextReviewAt: new Date().toISOString(), scheduledDays: 0, nextState: 'LEARNING' },
    hard: { nextReviewAt: new Date().toISOString(), scheduledDays: 1, nextState: 'REVIEW' },
    good: { nextReviewAt: new Date().toISOString(), scheduledDays: 2, nextState: 'REVIEW' },
    easy: { nextReviewAt: new Date().toISOString(), scheduledDays: 4, nextState: 'REVIEW' },
  })),
  rateSessionCard: vi.fn(),
}))

describe('rating visibility with rich cards', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Setup folder tree state
    useFolderStore.setState({
      nodes: [
        {
          id: 'd1',
          name: 'Test Deck',
          totalCards: 1,
          newCards: 1,
          learningCards: 0,
          masteredCards: 0,
          children: [],
        },
      ],
      expanded: {},
      loading: false,
      error: '',
      loadTree: vi.fn(),
      toggleExpanded: vi.fn(),
      createNode: vi.fn(),
      deleteNode: vi.fn(),
      getBreadcrumb: vi.fn(() => [
        {
          id: 'd1',
          name: 'Test Deck',
          totalCards: 1,
          newCards: 1,
          learningCards: 0,
          masteredCards: 0,
          children: [],
        },
      ]),
    })
  })

  it('shows rating buttons only after back-face reveal', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter initialEntries={['/flashcard/study/session/d1']}>
        <Routes>
          <Route path="/flashcard/study/session/:deckId" element={<StudySessionPage />} />
        </Routes>
      </MemoryRouter>,
    )

    // First, click "Học Bây giờ" to start study
    const studyButton = await screen.findByRole('button', { name: /Học/i })
    await user.click(studyButton)

    await waitFor(() => expect(screen.getByText('Abstraction')).toBeInTheDocument())
    expect(screen.queryByRole('button', { name: 'Good' })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Flashcard front side' }))
    expect(screen.getByRole('button', { name: 'Good' })).toBeInTheDocument()
  })
})
