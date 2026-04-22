import { useCallback, useEffect, useMemo, useState } from 'react'
import { AdminListeningPage } from '../admin/listening/AdminListeningPage'
import { useAuthStore } from '../../store/authStore'
import { DictationTab } from './components/DictationTab'
import { DictionaryPopover } from './components/DictionaryPopover'
import { FullTranscriptTab } from './components/FullTranscriptTab'
import { ListeningProgressHeader } from './components/ListeningProgressHeader'
import { ListeningSettingsModal } from './components/ListeningSettingsModal'
import { useListeningAudioPlayer } from './hooks/useListeningAudioPlayer'
import {
  getExercisesByTopicSlug,
  getLessonsByTopicAndExerciseSlug,
  getLessonWorkspace,
  getPublishedTopics,
  requestMediaAccess,
} from './services/listeningApi'
import { useListeningSettingsStore } from '../../store/listeningSettingsStore'
import type {
  ExerciseListResponse,
  LessonListResponse,
  LessonWorkspace,
  SentenceWithProgress,
  TopicListResponse,
} from './services/listeningApi'

type DictationSessionViewState = {
  currentIndex: number
  totalCount: number
  progressPercent: number
  completedCount: number
  currentSentence: SentenceWithProgress | null
}

const EMPTY_SESSION_STATE: DictationSessionViewState = {
  currentIndex: 0,
  totalCount: 0,
  progressPercent: 0,
  completedCount: 0,
  currentSentence: null,
}

export function ListeningPage() {
  const currentUser = useAuthStore((state) => state.currentUser)

  if (currentUser?.role === 'ADMIN') {
    return <AdminListeningPage />
  }

  return <LearnerListeningWorkspace />
}

function LearnerListeningWorkspace() {
  const [activeTab, setActiveTab] = useState<'dictation' | 'transcript'>('dictation')
  const [isSettingsOpen, setIsSettingsOpen] = useState(false)
  const [selectedDictionaryWord, setSelectedDictionaryWord] = useState<string | null>(null)
  const [topics, setTopics] = useState<TopicListResponse[]>([])
  const [topicSlug, setTopicSlug] = useState('')
  const [exercises, setExercises] = useState<ExerciseListResponse[]>([])
  const [exerciseSlug, setExerciseSlug] = useState('')
  const [lessons, setLessons] = useState<LessonListResponse[]>([])
  const [selectedLessonId, setSelectedLessonId] = useState('')
  const [lessonWorkspace, setLessonWorkspace] = useState<LessonWorkspace | null>(null)
  const [sessionState, setSessionState] = useState<DictationSessionViewState>(EMPTY_SESSION_STATE)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const audioPlayer = useListeningAudioPlayer()
  const replayKey = useListeningSettingsStore((state) => state.replayKey)
  const playPauseKey = useListeningSettingsStore((state) => state.playPauseKey)
  const transcriptAutoScroll = useListeningSettingsStore((state) => state.transcriptAutoScroll)
  const transcriptLoop = useListeningSettingsStore((state) => state.transcriptLoop)

  useEffect(() => {
    let active = true
    const loadTopics = async () => {
      try {
        setError(null)
        const next = await getPublishedTopics()
        if (!active) {
          return
        }
        setTopics(next)
        setTopicSlug(next[0]?.slug ?? '')
      } catch (loadError) {
        if (active) {
          setError(loadError instanceof Error ? loadError.message : 'Failed to load listening topics.')
        }
      }
    }

    void loadTopics()

    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    let active = true
    const loadExercises = async () => {
      if (!topicSlug) {
        setExercises([])
        setExerciseSlug('')
        setLessons([])
        setSelectedLessonId('')
        setLessonWorkspace(null)
        return
      }

      try {
        setError(null)
        const next = await getExercisesByTopicSlug(topicSlug)
        if (!active) {
          return
        }
        setExercises(next)
        setExerciseSlug(next[0]?.slug ?? '')
      } catch (loadError) {
        if (active) {
          setError(loadError instanceof Error ? loadError.message : 'Failed to load exercises.')
        }
      }
    }

    void loadExercises()

    return () => {
      active = false
    }
  }, [topicSlug])

  useEffect(() => {
    let active = true
    const loadLessons = async () => {
      if (!topicSlug || !exerciseSlug) {
        setLessons([])
        setSelectedLessonId('')
        setLessonWorkspace(null)
        return
      }

      try {
        setError(null)
        const next = await getLessonsByTopicAndExerciseSlug(topicSlug, exerciseSlug)
        if (!active) {
          return
        }
        setLessons(next)
        setSelectedLessonId(next[0]?.id ?? '')
      } catch (loadError) {
        if (active) {
          setError(loadError instanceof Error ? loadError.message : 'Failed to load lessons.')
        }
      }
    }

    void loadLessons()

    return () => {
      active = false
    }
  }, [topicSlug, exerciseSlug])

  useEffect(() => {
    let active = true
    const loadWorkspace = async () => {
      if (!selectedLessonId) {
        setLessonWorkspace(null)
        setSessionState(EMPTY_SESSION_STATE)
        return
      }

      try {
        setIsLoading(true)
        setError(null)
        const next = await getLessonWorkspace(selectedLessonId)
        if (!active) {
          return
        }
        setLessonWorkspace(next)
        setSessionState({
          currentIndex: 0,
          totalCount: next.totalCount,
          progressPercent: next.progressPercent,
          completedCount: next.completedCount,
          currentSentence: next.sentences[0] ?? null,
        })
      } catch (loadError) {
        if (active) {
          setLessonWorkspace(null)
          setError(loadError instanceof Error ? loadError.message : 'Failed to load lesson workspace.')
        }
      } finally {
        if (active) {
          setIsLoading(false)
        }
      }
    }

    void loadWorkspace()

    return () => {
      active = false
    }
  }, [selectedLessonId])

  const selectedTopic = useMemo(
    () => topics.find((topic) => topic.slug === topicSlug) ?? null,
    [topics, topicSlug],
  )
  const selectedExercise = useMemo(
    () => exercises.find((exercise) => exercise.slug === exerciseSlug) ?? null,
    [exercises, exerciseSlug],
  )
  const selectedLesson = useMemo(
    () => lessons.find((lesson) => lesson.id === selectedLessonId) ?? null,
    [lessons, selectedLessonId],
  )

  const currentSentenceMediaUrl = sessionState.currentSentence?.mediaUrl ?? lessonWorkspace?.mediaUrl ?? null

  const playFromMediaUrl = useCallback(async (mediaUrl: string) => {
    setError(null)
    const access = await requestMediaAccess(mediaUrl)
    await audioPlayer.playBlobUrl(access.url)
  }, [audioPlayer])

  const playCurrentSentence = useCallback(async () => {
    if (!currentSentenceMediaUrl) {
      setError('No media URL is available for this lesson.')
      return
    }

    try {
      await playFromMediaUrl(currentSentenceMediaUrl)
    } catch (playbackError) {
      setError(playbackError instanceof Error ? playbackError.message : 'Failed to start playback.')
    }
  }, [currentSentenceMediaUrl, playFromMediaUrl])

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      const pressedKey = event.key
      if (replayKey && pressedKey === replayKey) {
        event.preventDefault()
        void playCurrentSentence()
      }

      if (playPauseKey && pressedKey === playPauseKey) {
        event.preventDefault()
        const element = audioPlayer.audioRef.current
        if (!element) {
          return
        }
        if (element.paused) {
          void element.play()
        } else {
          element.pause()
        }
      }
    }

    window.addEventListener('keydown', onKeyDown)
    return () => {
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [audioPlayer.audioRef, playCurrentSentence, playPauseKey, replayKey])

  return (
    <section className="space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Listening Workspace</h1>
          <p className="text-sm text-slate-600">
            Select a topic, exercise, and lesson to practice strict dictation with short-lived media playback.
          </p>
          <p className="mt-1 text-xs text-slate-500">
            Current selection: {selectedTopic?.name ?? '—'} / {selectedExercise?.name ?? '—'} / {selectedLesson?.name ?? '—'}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button className="rounded border border-slate-300 px-3 py-2 text-sm" onClick={() => setIsSettingsOpen(true)} type="button">
            Settings
          </button>
        </div>

        <div className="w-full max-w-sm rounded-xl border border-slate-200 bg-slate-50 p-3">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Playback</p>
          <button
            className="mt-2 rounded bg-slate-900 px-3 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-50"
            disabled={!currentSentenceMediaUrl || isLoading}
            onClick={() => void playCurrentSentence()}
            type="button"
          >
            Play current sentence
          </button>
          <p className="mt-2 text-xs text-slate-500">
            Audio is loaded through a short-lived access URL and played from a blob URL.
          </p>
          <audio
            ref={audioPlayer.setAudioElement}
            className="mt-3 w-full"
            controls
            onEnded={() => {
              if (activeTab === 'transcript' && transcriptLoop) {
                void playCurrentSentence()
              }
            }}
          />
        </div>
      </div>

      {error && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {error}
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <SelectCard
          label="Topic"
          onChange={(value) => {
            setTopicSlug(value)
            setExerciseSlug('')
            setSelectedLessonId('')
            setLessonWorkspace(null)
            setSessionState(EMPTY_SESSION_STATE)
          }}
          options={topics.map((topic) => ({ value: topic.slug, label: topic.name }))}
          value={topicSlug}
        />
        <SelectCard
          label="Exercise"
          onChange={(value) => {
            setExerciseSlug(value)
            setSelectedLessonId('')
            setLessonWorkspace(null)
            setSessionState(EMPTY_SESSION_STATE)
          }}
          options={exercises.map((exercise) => ({ value: exercise.slug, label: exercise.name }))}
          value={exerciseSlug}
          disabled={!topicSlug}
        />
        <SelectCard
          label="Lesson"
          onChange={(value) => {
            setSelectedLessonId(value)
            setLessonWorkspace(null)
            setSessionState(EMPTY_SESSION_STATE)
          }}
          options={lessons.map((lesson) => ({ value: lesson.id, label: lesson.name }))}
          value={selectedLessonId}
          disabled={!exerciseSlug}
        />
      </div>

      {!lessonWorkspace ? (
        <div className="rounded-xl border border-dashed border-slate-200 bg-slate-50 p-6 text-sm text-slate-600">
          {isLoading ? 'Loading lesson workspace…' : 'Choose a lesson to start dictation practice.'}
        </div>
      ) : (
        <div className="space-y-4">
          <ListeningProgressHeader
            currentIndex={sessionState.currentIndex}
            lessonName={lessonWorkspace.name}
            progressPercent={sessionState.progressPercent}
            totalCount={sessionState.totalCount}
          />

          <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <span>
                Completed {sessionState.completedCount}/{sessionState.totalCount} sentences
              </span>
              <span>
                Media: {lessonWorkspace.mediaType ?? 'sentence media'}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2 border-b border-slate-200 pb-2">
            <button
              className={`rounded px-3 py-1.5 text-sm ${activeTab === 'dictation' ? 'bg-slate-900 text-white' : 'border border-slate-300'}`}
              onClick={() => setActiveTab('dictation')}
              type="button"
            >
              Dictation
            </button>
            <button
              className={`rounded px-3 py-1.5 text-sm ${activeTab === 'transcript' ? 'bg-slate-900 text-white' : 'border border-slate-300'}`}
              onClick={() => setActiveTab('transcript')}
              type="button"
            >
              Full Transcript
            </button>
          </div>

          {activeTab === 'dictation' ? (
            <DictationTab
              key={lessonWorkspace.lessonId}
              onProgressUpdate={(percent) => {
                setSessionState((current) => ({ ...current, progressPercent: percent }))
              }}
              onStateChange={(state) => {
                setSessionState(state)
              }}
              sentences={lessonWorkspace.sentences}
            />
          ) : (
            <div className="grid gap-4 lg:grid-cols-[2fr,1fr]">
              <FullTranscriptTab
                currentSentenceId={sessionState.currentSentence?.id ?? null}
                onReplaySentence={(sentence) => {
                  setSessionState((current) => ({ ...current, currentSentence: sentence }))
                  const mediaUrl = sentence.mediaUrl ?? lessonWorkspace.mediaUrl
                  if (!mediaUrl) {
                    setError('No media URL is available for this sentence.')
                    return
                  }
                  void playFromMediaUrl(mediaUrl).catch((playbackError) => {
                    setError(playbackError instanceof Error ? playbackError.message : 'Failed to start playback.')
                  })
                }}
                onSelectSentence={(sentence, index) => {
                  setSessionState((current) => ({ ...current, currentIndex: index, currentSentence: sentence }))
                }}
                onWordClick={(word) => {
                  if (word) {
                    setSelectedDictionaryWord(word)
                  }
                }}
                sentences={lessonWorkspace.sentences}
                transcriptAutoScroll={transcriptAutoScroll}
                transcriptLoop={transcriptLoop}
              />
              <DictionaryPopover onClose={() => setSelectedDictionaryWord(null)} term={selectedDictionaryWord} />
            </div>
          )}
        </div>
      )}

      <ListeningSettingsModal onClose={() => setIsSettingsOpen(false)} open={isSettingsOpen} />
    </section>
  )
}

type SelectCardProps = {
  label: string
  value: string
  disabled?: boolean
  options: Array<{ value: string; label: string }>
  onChange: (value: string) => void
}

function SelectCard({ label, value, disabled, options, onChange }: SelectCardProps) {
  return (
    <label className="space-y-2 rounded-xl border border-slate-200 bg-slate-50 p-3 text-sm">
      <span className="block text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</span>
      <select
        className="w-full rounded border border-slate-300 bg-white px-3 py-2"
        disabled={disabled || options.length === 0}
        onChange={(event) => onChange(event.target.value)}
        value={value}
      >
        <option value="">Select {label.toLowerCase()}</option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  )
}

