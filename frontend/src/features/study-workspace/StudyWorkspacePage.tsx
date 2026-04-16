import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { FolderTreeView } from '../../components/FolderTreeView'
import { DeckStudyModal, type DeckInfo } from '../../components/DeckStudyModal'
import { useFolderStore } from '../../store/folderStore'

export function StudyWorkspacePage() {
  const navigate = useNavigate()
  const nodes = useFolderStore((state) => state.nodes)
  const expanded = useFolderStore((state) => state.expanded)
  const loading = useFolderStore((state) => state.loading)
  const loadError = useFolderStore((state) => state.error)
  const loadTree = useFolderStore((state) => state.loadTree)
  const toggleExpanded = useFolderStore((state) => state.toggleExpanded)
  const createNode = useFolderStore((state) => state.createNode)
  const deleteNode = useFolderStore((state) => state.deleteNode)
  const getBreadcrumb = useFolderStore((state) => state.getBreadcrumb)
  const [actionError, setActionError] = useState('')
  const [selectedDeck, setSelectedDeck] = useState<DeckInfo | null>(null)

  useEffect(() => {
    void loadTree()
  }, [loadTree])

  const handleStudyClick = (folderId: string, folderName: string) => {
    const node = nodes.find((n) => n.id === folderId)
    if (!node) return

    setSelectedDeck({
      id: folderId,
      name: folderName,
      newCards: node.newCards,
      learningCards: node.learningCards,
      masteredCards: node.masteredCards,
    })
  }

  const handleConfirmStudy = () => {
    if (!selectedDeck) return

    const breadcrumb = getBreadcrumb(selectedDeck.id)
      .map((item) => item.name)
      .join(' > ')
    navigate(`/flashcard/study/session/${selectedDeck.id}`, {
      state: {
        deckName: selectedDeck.name,
        breadcrumb,
      },
    })
    setSelectedDeck(null)
  }

  return (
    <section>
      <div className="mb-4">
        <h1 className="text-2xl font-semibold">Study Workspace</h1>
        <p className="mt-1 text-sm text-slate-600">
          Click a folder name to start study mode with that folder and all descendants.
        </p>
      </div>

      {loadError ? <p className="mb-3 text-sm text-rose-600">{loadError}</p> : null}
      {actionError ? <p className="mb-3 text-sm text-rose-600">{actionError}</p> : null}
      {loading ? <p className="mb-3 text-sm text-slate-500">Loading folders...</p> : null}

      <FolderTreeView
        expanded={expanded}
        nodes={nodes}
        onCreate={async (name, parentId) => {
          try {
            setActionError('')
            await createNode(name, parentId)
          } catch (error) {
            setActionError(error instanceof Error ? error.message : 'Failed to create folder')
          }
        }}
        onDelete={async (folderId, folderName) => {
          if (!window.confirm(`Delete folder "${folderName}" and its descendants?`)) {
            return
          }
          try {
            setActionError('')
            await deleteNode(folderId)
          } catch (error) {
            setActionError(error instanceof Error ? error.message : 'Failed to delete folder')
          }
        }}
        onStudy={handleStudyClick}
        onToggle={toggleExpanded}
      />

      <DeckStudyModal
        deck={selectedDeck}
        isOpen={selectedDeck !== null}
        onClose={() => setSelectedDeck(null)}
        onStudy={handleConfirmStudy}
      />
    </section>
  )
}
