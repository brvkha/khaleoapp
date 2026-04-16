import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { StudyWorkspacePage } from '../../features/study-workspace/StudyWorkspacePage'
import { useFolderStore } from '../../store/folderStore'

describe('StudyWorkspacePage', () => {
  beforeEach(() => {
    useFolderStore.setState({
      nodes: [
        {
          id: 'folder-root',
          name: 'IELTS',
          totalCards: 6,
          newCards: 2,
          learningCards: 2,
          masteredCards: 2,
          children: [],
        },
      ],
      expanded: { 'folder-root': true },
      loading: false,
      error: '',
      loadTree: vi.fn(async () => undefined),
      toggleExpanded: vi.fn(),
      createNode: vi.fn(async () => undefined),
      deleteNode: vi.fn(async () => undefined),
      getBreadcrumb: vi.fn(() => [
        {
          id: 'folder-root',
          name: 'IELTS',
          totalCards: 6,
          newCards: 2,
          learningCards: 2,
          masteredCards: 2,
          children: [],
        },
      ]),
    })
  })

  it('navigates to deck detail page when folder is clicked', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter initialEntries={['/flashcard/study']}>
        <Routes>
          <Route element={<StudyWorkspacePage />} path="/flashcard/study" />
          <Route element={<div>Deck Detail Page</div>} path="/flashcard/study/deck/:deckId" />
        </Routes>
      </MemoryRouter>,
    )

    // Click on folder to navigate to detail
    await user.click(screen.getByRole('button', { name: 'IELTS' }))
    // Should navigate to detail page
    expect(screen.getByText('Deck Detail Page')).toBeInTheDocument()
  })
})