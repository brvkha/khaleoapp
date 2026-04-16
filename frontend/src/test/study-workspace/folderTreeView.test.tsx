import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FolderTreeView } from '../../components/FolderTreeView'

const baseTree = [
  {
    id: 'root-1',
    name: 'IELTS',
    totalCards: 7,
    newCards: 2,
    learningCards: 3,
    masteredCards: 2,
    children: [
      {
        id: 'child-1',
        name: 'Reading',
        totalCards: 3,
        newCards: 1,
        learningCards: 1,
        masteredCards: 1,
        children: [],
      },
    ],
  },
]

describe('FolderTreeView', () => {
  afterEach(() => {
    cleanup()
  })

  it('toggles only from arrow and runs study from folder label click', async () => {
    const user = userEvent.setup()
    const onToggle = vi.fn()
    const onStudy = vi.fn()

    render(
      <FolderTreeView
        expanded={{ 'root-1': true }}
        nodes={baseTree}
        onCreate={vi.fn(async () => undefined)}
        onDelete={vi.fn(async () => undefined)}
        onStudy={onStudy}
        onToggle={onToggle}
      />,
    )

    await user.click(screen.getByLabelText('Collapse IELTS'))
    expect(onToggle).toHaveBeenCalledWith('root-1')
    expect(onStudy).not.toHaveBeenCalled()

    await user.click(screen.getByRole('button', { name: 'IELTS' }))
    expect(onStudy).toHaveBeenCalledWith('root-1', 'IELTS')
  })

  it('supports inline root and child folder creation plus delete action', async () => {
    const user = userEvent.setup()
    const onCreate = vi.fn(async () => undefined)
    const onDelete = vi.fn(async () => undefined)

    render(
      <FolderTreeView
        expanded={{ 'root-1': true }}
        nodes={baseTree}
        onCreate={onCreate}
        onDelete={onDelete}
        onStudy={vi.fn()}
        onToggle={vi.fn()}
      />,
    )

    await user.click(screen.getByRole('button', { name: '+ New Folder' }))
    await user.type(screen.getByLabelText('New root folder name'), 'Grammar')
    await user.click(screen.getByRole('button', { name: 'Save' }))

    await waitFor(() => expect(onCreate).toHaveBeenCalledWith('Grammar', null))

    await user.click(screen.getByLabelText('Create child folder in IELTS'))
    await user.type(screen.getByLabelText('New folder name'), 'Task 1')
    await user.click(screen.getAllByRole('button', { name: 'Save' })[0])

    await waitFor(() => expect(onCreate).toHaveBeenCalledWith('Task 1', 'root-1'))

    await user.click(screen.getByLabelText('Delete IELTS'))
    await waitFor(() => expect(onDelete).toHaveBeenCalledWith('root-1', 'IELTS'))
  })
})


