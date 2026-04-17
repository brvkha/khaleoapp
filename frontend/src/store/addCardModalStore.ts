import { create } from 'zustand'
import type { BulkImportSeparator, ParsedBulkCandidate } from '../features/cards/types/bulkImport'
import type { BulkImportRunState } from '../features/cards/types/bulkImportOrchestration'

const LAST_MODE_KEY = 'add-card-modal:last-mode'

type AddCardMode = 'single' | 'bulk'

type AddCardModalState = {
  mode: AddCardMode
  bulkRawText: string
  separator: BulkImportSeparator
  parsedCandidates: ParsedBulkCandidate[]
  runState: BulkImportRunState | null
  retryFromChunkIndex: number | null
  setMode: (mode: AddCardMode) => void
  setBulkRawText: (value: string) => void
  setSeparator: (separator: BulkImportSeparator) => void
  setParsedCandidates: (value: ParsedBulkCandidate[]) => void
  setRunState: (value: BulkImportRunState | null) => void
  setRetryFromChunkIndex: (value: number | null) => void
  resetBulk: () => void
}

function loadMode(): AddCardMode {
  if (typeof window === 'undefined') {
    return 'single'
  }
  const mode = window.localStorage.getItem(LAST_MODE_KEY)
  return mode === 'bulk' ? 'bulk' : 'single'
}

export const useAddCardModalStore = create<AddCardModalState>((set) => ({
  mode: loadMode(),
  bulkRawText: '',
  separator: 'tab',
  parsedCandidates: [],
  runState: null,
  retryFromChunkIndex: null,
  setMode: (mode) => {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(LAST_MODE_KEY, mode)
    }
    set({ mode })
  },
  setBulkRawText: (bulkRawText) => set({ bulkRawText }),
  setSeparator: (separator) => set({ separator }),
  setParsedCandidates: (parsedCandidates) => set({ parsedCandidates }),
  setRunState: (runState) => set({ runState }),
  setRetryFromChunkIndex: (retryFromChunkIndex) => set({ retryFromChunkIndex }),
  resetBulk: () =>
    set({
      bulkRawText: '',
      parsedCandidates: [],
      runState: null,
      retryFromChunkIndex: null,
    }),
}))

