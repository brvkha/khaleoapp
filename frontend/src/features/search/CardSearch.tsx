import type { Card } from '../../types'
import { PlainTextPreview } from '../cards/components/PlainTextPreview'

type CardSearchProps = {
  cards: Card[]
  query: string
}

export function CardSearch({ cards, query }: CardSearchProps) {
  const lowered = query.trim().toLowerCase()
  const filtered = lowered
    ? cards.filter((card) => {
        const haystack = `${card.front} ${card.back} ${card.tags.join(' ')}`.toLowerCase()
        return haystack.includes(lowered)
      })
    : cards

  return (
    <div className="mt-3">
      <p className="text-sm text-slate-600">Results: {filtered.length}</p>
      <ul className="mt-2 space-y-2">
        {filtered.map((card) => (
          <li className="rounded border border-slate-200 bg-white p-3" key={card.id}>
            <PlainTextPreview content={card.frontContent || card.front} lines={2} className="font-medium text-slate-900" />
            <PlainTextPreview content={card.backContent || card.back} lines={2} className="mt-1 text-sm text-slate-600" />
          </li>
        ))}
      </ul>
    </div>
  )
}
