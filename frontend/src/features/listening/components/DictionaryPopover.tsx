import { useEffect, useState } from 'react'
import {
  lookupDictionary,
  type DictionaryLookupFailureResponse,
  type DictionaryLookupResponse,
} from '../services/listeningApi'

type DictionaryPopoverProps = {
  term: string | null
  onClose: () => void
}

export function DictionaryPopover({ term, onClose }: DictionaryPopoverProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [data, setData] = useState<DictionaryLookupResponse | null>(null)
  const [failure, setFailure] = useState<DictionaryLookupFailureResponse | null>(null)

  useEffect(() => {
    let active = true

    const load = async () => {
      if (!term) {
        setData(null)
        setFailure(null)
        return
      }

      setIsLoading(true)
      setFailure(null)
      try {
        const result = await lookupDictionary(term)
        if (!active) {
          return
        }

        if ('entries' in result) {
          setData(result)
          setFailure(null)
        } else {
          setData(null)
          setFailure(result)
        }
      } finally {
        if (active) {
          setIsLoading(false)
        }
      }
    }

    void load()
    return () => {
      active = false
    }
  }, [term])

  if (!term) {
    return null
  }

  return (
    <aside className="rounded-xl border border-slate-200 bg-white p-4 text-sm shadow-sm">
      <div className="mb-3 flex items-center justify-between gap-2">
        <h3 className="text-base font-semibold text-slate-900">Dictionary: {term}</h3>
        <button className="rounded border border-slate-300 px-2 py-1 text-xs" onClick={onClose} type="button">
          Close
        </button>
      </div>

      {isLoading ? <p className="text-slate-500">Looking up term...</p> : null}

      {!isLoading && failure ? (
        <div className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-amber-800">
          {failure.message}
        </div>
      ) : null}

      {!isLoading && data && data.entries.length === 0 ? (
        <p className="text-slate-500">No dictionary entries found.</p>
      ) : null}

      {!isLoading && data && data.entries.length > 0 ? (
        <div className="space-y-3">
          {data.entries.map((entry, index) => (
            <article className="rounded-lg border border-slate-200 bg-slate-50 p-3" key={`${entry.ipa ?? 'entry'}-${index}`}>
              <p className="text-xs text-slate-500">IPA: {entry.ipa ?? 'N/A'}</p>
              {entry.definitions.length > 0 ? (
                <ul className="mt-2 list-disc space-y-1 pl-5 text-slate-700">
                  {entry.definitions.map((definition, defIndex) => (
                    <li key={`${index}-${defIndex}`}>{definition}</li>
                  ))}
                </ul>
              ) : (
                <p className="mt-2 text-slate-500">No definitions available.</p>
              )}
            </article>
          ))}
        </div>
      ) : null}
    </aside>
  )
}

