import { create } from 'zustand'
import { LISTENING_DEFAULT_SETTINGS, LISTENING_DEFAULT_SHORTCUTS } from '../features/listening/config/listeningConfig'

const STORAGE_KEY = 'listening-settings'

type AutoReplayCount = 'none' | 1 | 2 | 'infinite'

type ListeningSettingsState = {
  replayKey: string
  playPauseKey: string
  autoPlayOnNext: boolean
  autoReplayCount: AutoReplayCount
  replayIntervalSeconds: number
  transcriptAutoScroll: boolean
  transcriptLoop: boolean
  setReplayKey: (value: string) => void
  setPlayPauseKey: (value: string) => void
  setAutoPlayOnNext: (value: boolean) => void
  setAutoReplayCount: (value: AutoReplayCount) => void
  setReplayIntervalSeconds: (value: number) => void
  setTranscriptAutoScroll: (value: boolean) => void
  setTranscriptLoop: (value: boolean) => void
  reset: () => void
}

function loadState() {
  if (typeof window === 'undefined') {
    return null
  }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return null
    }
    return JSON.parse(raw) as Partial<ListeningSettingsState>
  } catch {
    return null
  }
}

function persistState(state: Partial<ListeningSettingsState>) {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      replayKey: state.replayKey,
      playPauseKey: state.playPauseKey,
      autoPlayOnNext: state.autoPlayOnNext,
      autoReplayCount: state.autoReplayCount,
      replayIntervalSeconds: state.replayIntervalSeconds,
      transcriptAutoScroll: state.transcriptAutoScroll,
      transcriptLoop: state.transcriptLoop,
    }),
  )
}

const initialState = loadState() ?? {}

export const useListeningSettingsStore = create<ListeningSettingsState>((set, get) => ({
  replayKey: initialState.replayKey ?? LISTENING_DEFAULT_SHORTCUTS.replayKey,
  playPauseKey: initialState.playPauseKey ?? LISTENING_DEFAULT_SHORTCUTS.playPauseKey,
  autoPlayOnNext: initialState.autoPlayOnNext ?? LISTENING_DEFAULT_SETTINGS.autoPlayOnNext,
  autoReplayCount: initialState.autoReplayCount ?? LISTENING_DEFAULT_SETTINGS.autoReplayCount,
  replayIntervalSeconds: initialState.replayIntervalSeconds ?? LISTENING_DEFAULT_SETTINGS.replayIntervalSeconds,
  transcriptAutoScroll: initialState.transcriptAutoScroll ?? LISTENING_DEFAULT_SETTINGS.transcriptAutoScroll,
  transcriptLoop: initialState.transcriptLoop ?? LISTENING_DEFAULT_SETTINGS.transcriptLoop,
  setReplayKey: (value) => {
    const next = { ...get(), replayKey: value }
    persistState(next)
    set(next)
  },
  setPlayPauseKey: (value) => {
    const next = { ...get(), playPauseKey: value }
    persistState(next)
    set(next)
  },
  setAutoPlayOnNext: (value) => {
    const next = { ...get(), autoPlayOnNext: value }
    persistState(next)
    set(next)
  },
  setAutoReplayCount: (value) => {
    const next = { ...get(), autoReplayCount: value }
    persistState(next)
    set(next)
  },
  setReplayIntervalSeconds: (value) => {
    const next = { ...get(), replayIntervalSeconds: value }
    persistState(next)
    set(next)
  },
  setTranscriptAutoScroll: (value) => {
    const next = { ...get(), transcriptAutoScroll: value }
    persistState(next)
    set(next)
  },
  setTranscriptLoop: (value) => {
    const next = { ...get(), transcriptLoop: value }
    persistState(next)
    set(next)
  },
  reset: () => {
    const next = {
      replayKey: LISTENING_DEFAULT_SHORTCUTS.replayKey,
      playPauseKey: LISTENING_DEFAULT_SHORTCUTS.playPauseKey,
      autoPlayOnNext: LISTENING_DEFAULT_SETTINGS.autoPlayOnNext,
      autoReplayCount: LISTENING_DEFAULT_SETTINGS.autoReplayCount,
      replayIntervalSeconds: LISTENING_DEFAULT_SETTINGS.replayIntervalSeconds,
      transcriptAutoScroll: LISTENING_DEFAULT_SETTINGS.transcriptAutoScroll,
      transcriptLoop: LISTENING_DEFAULT_SETTINGS.transcriptLoop,
    }
    persistState(next)
    set(next)
  },
}))


