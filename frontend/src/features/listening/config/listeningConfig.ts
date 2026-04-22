export const LISTENING_SPEED_OPTIONS = [0.25, 0.5, 0.75, 1, 1.25, 1.5, 2, 3] as const

export const LISTENING_DEFAULT_SHORTCUTS = {
  replayKey: 'Control',
  playPauseKey: '`',
} as const

export const LISTENING_DEFAULT_SETTINGS = {
  autoPlayOnNext: true,
  autoReplayCount: 'none' as const,
  replayIntervalSeconds: 1,
  transcriptAutoScroll: true,
  transcriptLoop: false,
} as const

export const LISTENING_ALLOWED_MEDIA_TYPES = ['audio', 'video'] as const

