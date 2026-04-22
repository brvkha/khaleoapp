import type { ListeningSentence } from '../../../listening/types/listeningApi'

type Props = {
  items: ListeningSentence[]
  onReorder: (nextItems: ListeningSentence[]) => void
}

function moveItem(items: ListeningSentence[], fromIndex: number, toIndex: number): ListeningSentence[] {
  const next = [...items]
  const [item] = next.splice(fromIndex, 1)
  next.splice(toIndex, 0, item)
  return next.map((sentence, index) => ({ ...sentence, orderIndex: index + 1 }))
}

export function SentenceReorderList({ items, onReorder }: Props) {
  return (
    <div className="space-y-2">
      {items.map((sentence, index) => (
        <div
          className="flex items-start gap-2 rounded-lg border border-slate-200 bg-white p-3"
          draggable
          key={sentence.id ?? `${sentence.orderIndex ?? index}`}
          onDragStart={(event) => {
            event.dataTransfer.setData('text/plain', String(index))
          }}
          onDragOver={(event) => event.preventDefault()}
          onDrop={(event) => {
            event.preventDefault()
            const fromIndex = Number(event.dataTransfer.getData('text/plain'))
            if (Number.isNaN(fromIndex) || fromIndex === index) {
              return
            }
            onReorder(moveItem(items, fromIndex, index))
          }}
        >
          <div className="min-w-10 rounded-full bg-slate-900 px-3 py-1 text-center text-sm font-semibold text-white">
            {sentence.orderIndex ?? index + 1}
          </div>
          <div className="min-w-0 flex-1">
            <p className="font-medium text-slate-900">{sentence.transcript}</p>
            <p className="text-sm text-slate-500">
              {sentence.translation ?? 'Bản dịch đang được cập nhật...'}
            </p>
          </div>
          <div className="flex shrink-0 flex-col gap-1">
            <button
              className="rounded border border-slate-300 px-2 py-1 text-xs disabled:opacity-40"
              disabled={index === 0}
              onClick={() => onReorder(moveItem(items, index, index - 1))}
              type="button"
            >
              ↑
            </button>
            <button
              className="rounded border border-slate-300 px-2 py-1 text-xs disabled:opacity-40"
              disabled={index === items.length - 1}
              onClick={() => onReorder(moveItem(items, index, index + 1))}
              type="button"
            >
              ↓
            </button>
          </div>
        </div>
      ))}
    </div>
  )
}

