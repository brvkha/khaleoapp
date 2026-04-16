import { useEffect, useState } from 'react'

export interface DeckInfo {
  id: string
  name: string
  newCards: number
  learningCards: number
  masteredCards: number
}

interface DeckStudyModalProps {
  deck: DeckInfo | null
  isOpen: boolean
  onClose: () => void
  onStudy: () => void
}

export function DeckStudyModal({ deck, isOpen, onClose, onStudy }: DeckStudyModalProps) {
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    setIsVisible(isOpen)
  }, [isOpen])

  if (!deck || !isVisible) {
    return null
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-lg">
        <h2 className="truncate text-xl font-semibold text-slate-900">{deck.name}</h2>

        <div className="mt-6 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-slate-600">Mới:</span>
            <span className="text-lg font-semibold text-blue-600">{deck.newCards}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-slate-600">Đang học:</span>
            <span className="text-lg font-semibold text-orange-600">{deck.learningCards}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-slate-600">Cần ôn:</span>
            <span className="text-lg font-semibold text-green-600">{deck.masteredCards}</span>
          </div>
        </div>

        <div className="mt-8 flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Hủy
          </button>
          <button
            onClick={onStudy}
            className="flex-1 rounded-md bg-blue-500 px-4 py-2 text-sm font-medium text-white hover:bg-blue-600"
          >
            Học Bây giờ
          </button>
        </div>
      </div>
    </div>
  )
}

