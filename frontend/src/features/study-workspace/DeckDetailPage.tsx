import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useFolderStore } from '../../store/folderStore'

export function DeckDetailPage() {
  const navigate = useNavigate()
  const { deckId } = useParams<{ deckId: string }>()
  const nodes = useFolderStore((state) => state.nodes)
  const getBreadcrumb = useFolderStore((state) => state.getBreadcrumb)

  // Tìm deck trong tree
  const findNodeById = (id: string, nodeList: typeof nodes): typeof nodes[0] | null => {
    for (const node of nodeList) {
      if (node.id === id) return node
      if (node.children.length > 0) {
        const found = findNodeById(id, node.children)
        if (found) return found
      }
    }
    return null
  }

  const currentDeck = deckId ? findNodeById(deckId, nodes) : null
  const breadcrumb = deckId ? getBreadcrumb(deckId) : []

  useEffect(() => {
    if (deckId && !currentDeck) {
      navigate('/flashcard/study')
    }
  }, [deckId, currentDeck, navigate])

  if (!currentDeck) {
    return (
      <div className="flex items-center justify-center p-8">
        <p className="text-slate-500">Deck not found</p>
      </div>
    )
  }

  return (
    <section className="space-y-4">
      <div className="flex items-center gap-2">
        <button
          onClick={() => navigate('/flashcard/study')}
          className="rounded px-3 py-1 text-sm font-medium text-slate-600 hover:bg-slate-100"
        >
          ← Back
        </button>
      </div>

      <div className="mb-2">
        <p className="text-sm text-slate-500">
          {breadcrumb.map((item, idx) => (
            <span key={item.id}>
              {idx > 0 && ' > '}
              {item.name}
            </span>
          ))}
        </p>
      </div>

      <div className="rounded border border-slate-200 bg-white p-2">
        <div className="space-y-4 p-4">
          <h1 className="truncate text-2xl font-semibold text-slate-900">{currentDeck.name}</h1>

          <div className="grid grid-cols-3 gap-4">
            <div className="rounded-lg bg-blue-50 p-4">
              <p className="text-sm font-medium text-slate-600">Mới</p>
              <p className="mt-2 text-2xl font-bold text-blue-600">{currentDeck.newCards}</p>
            </div>
            <div className="rounded-lg bg-orange-50 p-4">
              <p className="text-sm font-medium text-slate-600">Đang học</p>
              <p className="mt-2 text-2xl font-bold text-orange-600">{currentDeck.learningCards}</p>
            </div>
            <div className="rounded-lg bg-green-50 p-4">
              <p className="text-sm font-medium text-slate-600">Cần ôn</p>
              <p className="mt-2 text-2xl font-bold text-green-600">{currentDeck.masteredCards}</p>
            </div>
          </div>

          <div className="border-t border-slate-200 pt-4">
            <p className="text-sm text-slate-600">
              Tổng card: <span className="font-semibold text-slate-900">{currentDeck.totalCards}</span>
            </p>
          </div>

          <button
            onClick={() =>
              navigate(`/flashcard/study/session/${currentDeck.id}`, {
                state: {
                  deckName: currentDeck.name,
                  breadcrumb: breadcrumb.map((item) => item.name).join(' > '),
                },
              })
            }
            className="w-full rounded-lg bg-blue-500 px-4 py-3 font-medium text-white hover:bg-blue-600"
          >
            Học Bây giờ
          </button>
        </div>
      </div>
    </section>
  )
}

