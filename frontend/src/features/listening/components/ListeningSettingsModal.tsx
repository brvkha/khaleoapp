import { useListeningSettingsStore } from '../../../store/listeningSettingsStore'

type ListeningSettingsModalProps = {
  open: boolean
  onClose: () => void
}

const AUTO_REPLAY_OPTIONS: Array<'none' | 1 | 2 | 'infinite'> = ['none', 1, 2, 'infinite']

export function ListeningSettingsModal({ open, onClose }: ListeningSettingsModalProps) {
  const replayKey = useListeningSettingsStore((state) => state.replayKey)
  const playPauseKey = useListeningSettingsStore((state) => state.playPauseKey)
  const autoPlayOnNext = useListeningSettingsStore((state) => state.autoPlayOnNext)
  const autoReplayCount = useListeningSettingsStore((state) => state.autoReplayCount)
  const replayIntervalSeconds = useListeningSettingsStore((state) => state.replayIntervalSeconds)
  const transcriptAutoScroll = useListeningSettingsStore((state) => state.transcriptAutoScroll)
  const transcriptLoop = useListeningSettingsStore((state) => state.transcriptLoop)

  const setReplayKey = useListeningSettingsStore((state) => state.setReplayKey)
  const setPlayPauseKey = useListeningSettingsStore((state) => state.setPlayPauseKey)
  const setAutoPlayOnNext = useListeningSettingsStore((state) => state.setAutoPlayOnNext)
  const setAutoReplayCount = useListeningSettingsStore((state) => state.setAutoReplayCount)
  const setReplayIntervalSeconds = useListeningSettingsStore((state) => state.setReplayIntervalSeconds)
  const setTranscriptAutoScroll = useListeningSettingsStore((state) => state.setTranscriptAutoScroll)
  const setTranscriptLoop = useListeningSettingsStore((state) => state.setTranscriptLoop)
  const reset = useListeningSettingsStore((state) => state.reset)

  if (!open) {
    return null
  }

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-900/30 p-4">
      <div className="w-full max-w-xl rounded-2xl border border-slate-200 bg-white p-5 shadow-xl">
        <div className="mb-4 flex items-center justify-between gap-2">
          <h2 className="text-lg font-semibold text-slate-900">Listening Settings</h2>
          <button className="rounded border border-slate-300 px-2 py-1 text-sm" onClick={onClose} type="button">
            Close
          </button>
        </div>

        <div className="grid gap-3 md:grid-cols-2">
          <label className="space-y-1 text-sm">
            <span className="text-slate-600">Replay key</span>
            <input className="w-full rounded border border-slate-300 px-3 py-2" onChange={(e) => setReplayKey(e.target.value)} value={replayKey} />
          </label>

          <label className="space-y-1 text-sm">
            <span className="text-slate-600">Play/Pause key</span>
            <input
              className="w-full rounded border border-slate-300 px-3 py-2"
              onChange={(e) => setPlayPauseKey(e.target.value)}
              value={playPauseKey}
            />
          </label>

          <label className="space-y-1 text-sm">
            <span className="text-slate-600">Auto replay count</span>
            <select
              className="w-full rounded border border-slate-300 px-3 py-2"
              onChange={(e) => {
                const value = e.target.value
                if (value === '1' || value === '2') {
                  setAutoReplayCount(Number(value) as 1 | 2)
                } else {
                  setAutoReplayCount(value as 'none' | 'infinite')
                }
              }}
              value={String(autoReplayCount)}
            >
              {AUTO_REPLAY_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </label>

          <label className="space-y-1 text-sm">
            <span className="text-slate-600">Replay interval (seconds)</span>
            <input
              className="w-full rounded border border-slate-300 px-3 py-2"
              min={0}
              onChange={(e) => setReplayIntervalSeconds(Number(e.target.value) || 0)}
              type="number"
              value={replayIntervalSeconds}
            />
          </label>

          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input checked={autoPlayOnNext} onChange={(e) => setAutoPlayOnNext(e.target.checked)} type="checkbox" />
            Auto-play next sentence
          </label>

          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input checked={transcriptAutoScroll} onChange={(e) => setTranscriptAutoScroll(e.target.checked)} type="checkbox" />
            Transcript auto-scroll
          </label>

          <label className="flex items-center gap-2 text-sm text-slate-700 md:col-span-2">
            <input checked={transcriptLoop} onChange={(e) => setTranscriptLoop(e.target.checked)} type="checkbox" />
            Loop current transcript sentence on playback end
          </label>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button className="rounded border border-slate-300 px-3 py-2 text-sm" onClick={reset} type="button">
            Reset defaults
          </button>
          <button className="rounded bg-slate-900 px-3 py-2 text-sm text-white" onClick={onClose} type="button">
            Done
          </button>
        </div>
      </div>
    </div>
  )
}

