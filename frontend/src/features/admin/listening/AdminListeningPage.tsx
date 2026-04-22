import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNotificationStore } from '../../../store/notificationStore'
import type {
  ListeningExercise,
  ListeningLesson,
  ListeningSentence,
  ListeningTopic,
} from '../../listening/types/listeningApi'
import {
  createExercise,
  createLesson,
  createSentence,
  createTopic,
  deleteExercise,
  deleteLesson,
  deleteSentence,
  deleteTopic,
  importSentences,
  listExercises,
  listLessons,
  listSentences,
  listTopics,
  reorderSentences,
  updateExercise,
  updateLesson,
  updateSentence,
  updateTopic,
} from './services/adminListeningApi'
import { SentenceJsonImportModal } from './components/SentenceJsonImportModal'
import { SentenceReorderList } from './components/SentenceReorderList'

const EMPTY_TOPIC: ListeningTopic = { name: '', slug: '', description: '', status: 'draft' }
const EMPTY_EXERCISE: ListeningExercise = { name: '', slug: '', orderIndex: 1, status: 'draft' }
const EMPTY_LESSON: ListeningLesson = { name: '', slug: '', orderIndex: 1, mediaUrl: '', mediaType: 'audio', status: 'draft' }
const EMPTY_SENTENCE: ListeningSentence = {
  transcript: '',
  translation: '',
  aliasesJson: '[]',
  mediaUrl: '',
  startTime: null,
  endTime: null,
  orderIndex: 1,
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T
}

export function AdminListeningPage() {
  const pushSuccess = useNotificationStore((state) => state.pushSuccess)
  const pushError = useNotificationStore((state) => state.pushError)

  const [topics, setTopics] = useState<ListeningTopic[]>([])
  const [selectedTopicId, setSelectedTopicId] = useState<string | null>(null)
  const [exercises, setExercises] = useState<ListeningExercise[]>([])
  const [selectedExerciseId, setSelectedExerciseId] = useState<string | null>(null)
  const [lessons, setLessons] = useState<ListeningLesson[]>([])
  const [selectedLessonId, setSelectedLessonId] = useState<string | null>(null)
  const [sentences, setSentences] = useState<ListeningSentence[]>([])
  const [importOpen, setImportOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const [topicForm, setTopicForm] = useState<ListeningTopic>(clone(EMPTY_TOPIC))
  const [exerciseForm, setExerciseForm] = useState<ListeningExercise>(clone(EMPTY_EXERCISE))
  const [lessonForm, setLessonForm] = useState<ListeningLesson>(clone(EMPTY_LESSON))
  const [sentenceForm, setSentenceForm] = useState<ListeningSentence>(clone(EMPTY_SENTENCE))

  const selectedTopic = useMemo(() => topics.find((item) => item.id === selectedTopicId) ?? null, [topics, selectedTopicId])
  const selectedExercise = useMemo(
    () => exercises.find((item) => item.id === selectedExerciseId) ?? null,
    [exercises, selectedExerciseId],
  )
  const selectedLesson = useMemo(
    () => lessons.find((item) => item.id === selectedLessonId) ?? null,
    [lessons, selectedLessonId],
  )

  const runWithBusy = async (action: () => Promise<void>) => {
    setBusy(true)
    try {
      await action()
    } finally {
      setBusy(false)
    }
  }

  const refreshTopics = async () => {
    const next = await listTopics()
    setTopics(next)
    if (next.length > 0 && !selectedTopicId) {
      setSelectedTopicId(next[0].id ?? null)
    }
  }

  const refreshExercises = async (topicId: string) => {
    const next = await listExercises(topicId)
    setExercises(next)
    setSelectedExerciseId(next[0]?.id ?? null)
  }

  const refreshLessons = async (exerciseId: string) => {
    const next = await listLessons(exerciseId)
    setLessons(next)
    setSelectedLessonId(next[0]?.id ?? null)
  }

  const refreshSentences = async (lessonId: string) => {
    const next = await listSentences(lessonId)
    setSentences(next)
  }

  useEffect(() => {
    void refreshTopics().catch((error) => pushError(error instanceof Error ? error.message : 'Failed to load topics.'))
  }, [])

  useEffect(() => {
    if (!selectedTopicId) {
      setExercises([])
      setLessons([])
      setSentences([])
      return
    }
    void refreshExercises(selectedTopicId).catch((error) => pushError(error instanceof Error ? error.message : 'Failed to load exercises.'))
  }, [selectedTopicId])

  useEffect(() => {
    if (!selectedExerciseId) {
      setLessons([])
      setSentences([])
      return
    }
    void refreshLessons(selectedExerciseId).catch((error) => pushError(error instanceof Error ? error.message : 'Failed to load lessons.'))
  }, [selectedExerciseId])

  useEffect(() => {
    if (!selectedLessonId) {
      setSentences([])
      return
    }
    void refreshSentences(selectedLessonId).catch((error) => pushError(error instanceof Error ? error.message : 'Failed to load sentences.'))
  }, [selectedLessonId])

  return (
    <section className="space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Listening CMS</h1>
          <p className="text-sm text-slate-600">Manage topics, exercises, lessons, and sentences in drill-down order.</p>
          <p className="mt-1 text-xs text-slate-500">
            Current selection: {selectedTopic?.name ?? '—'} / {selectedExercise?.name ?? '—'} / {selectedLesson?.name ?? '—'}
          </p>
        </div>
        <button
          className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white"
          onClick={() => setSelectedTopicId(null)}
          type="button"
        >
          New topic
        </button>
      </div>

      <div className="grid gap-4 lg:grid-cols-4">
        <Panel title="Topics">
          <EntityList
            activeId={selectedTopicId}
            items={topics}
            label={(item) => item.name}
            onSelect={(item) => {
              setSelectedTopicId(item.id ?? null)
              setTopicForm(item)
            }}
          />
          <EntityForm
            disabled={busy}
            fields={[
              { key: 'name', label: 'Name', value: topicForm.name },
              { key: 'slug', label: 'Slug', value: topicForm.slug },
              { key: 'description', label: 'Description', value: topicForm.description ?? '' },
            ]}
            onChange={(key, value) => setTopicForm((current) => ({ ...current, [key]: value }))}
            onDelete={async () => {
              const topicId = topicForm.id
              if (!topicId) return
              await runWithBusy(async () => {
                await deleteTopic(topicId)
                pushSuccess('Topic deleted.')
                setTopicForm(clone(EMPTY_TOPIC))
                setSelectedTopicId(null)
                await refreshTopics()
              })
            }}
            onSave={async () => {
              await runWithBusy(async () => {
                const saved = topicForm.id ? await updateTopic(topicForm.id, topicForm) : await createTopic(topicForm)
                pushSuccess(`Topic ${topicForm.id ? 'updated' : 'created'}.`)
                setTopicForm(saved)
                await refreshTopics()
                setSelectedTopicId(saved.id ?? null)
              })
            }}
            title="Topic editor"
          />
        </Panel>

        <Panel title="Exercises">
          <EntityList
            activeId={selectedExerciseId}
            items={exercises}
            label={(item) => `${item.orderIndex ?? 0}. ${item.name}`}
            onSelect={(item) => {
              setSelectedExerciseId(item.id ?? null)
              setExerciseForm(item)
            }}
          />
          <EntityForm
            disabled={busy || !selectedTopicId}
            fields={[
              { key: 'name', label: 'Name', value: exerciseForm.name },
              { key: 'slug', label: 'Slug', value: exerciseForm.slug },
              { key: 'orderIndex', label: 'Order', value: String(exerciseForm.orderIndex ?? '') },
            ]}
            onChange={(key, value) =>
              setExerciseForm((current) => ({
                ...current,
                [key]: key === 'orderIndex' ? (value ? Number(value) : null) : value,
                topicId: selectedTopicId ?? current.topicId ?? null,
              }))
            }
            onDelete={async () => {
              const exerciseId = exerciseForm.id
              if (!exerciseId) return
              await runWithBusy(async () => {
                await deleteExercise(exerciseId)
                pushSuccess('Exercise deleted.')
                setExerciseForm(clone(EMPTY_EXERCISE))
                await refreshExercises(selectedTopicId ?? '')
              })
            }}
            onSave={async () => {
              if (!selectedTopicId) {
                throw new Error('Select a topic first.')
              }
              await runWithBusy(async () => {
                const payload = { ...exerciseForm, topicId: selectedTopicId }
                const saved = exerciseForm.id ? await updateExercise(exerciseForm.id, payload) : await createExercise(selectedTopicId, payload)
                pushSuccess(`Exercise ${exerciseForm.id ? 'updated' : 'created'}.`)
                setExerciseForm(saved)
                await refreshExercises(selectedTopicId)
              })
            }}
            title="Exercise editor"
          />
        </Panel>

        <Panel title="Lessons">
          <EntityList
            activeId={selectedLessonId}
            items={lessons}
            label={(item) => `${item.orderIndex ?? 0}. ${item.name}`}
            onSelect={(item) => {
              setSelectedLessonId(item.id ?? null)
              setLessonForm(item)
            }}
          />
          <EntityForm
            disabled={busy || !selectedExerciseId}
            fields={[
              { key: 'name', label: 'Name', value: lessonForm.name },
              { key: 'slug', label: 'Slug', value: lessonForm.slug },
              { key: 'mediaUrl', label: 'Media URL', value: lessonForm.mediaUrl ?? '' },
            ]}
            onChange={(key, value) =>
              setLessonForm((current) => ({
                ...current,
                [key]: value,
                exerciseId: selectedExerciseId ?? current.exerciseId ?? null,
              }))
            }
            onDelete={async () => {
              const lessonId = lessonForm.id
              if (!lessonId) return
              await runWithBusy(async () => {
                await deleteLesson(lessonId)
                pushSuccess('Lesson deleted.')
                setLessonForm(clone(EMPTY_LESSON))
                await refreshLessons(selectedExerciseId ?? '')
              })
            }}
            onSave={async () => {
              if (!selectedExerciseId) {
                throw new Error('Select an exercise first.')
              }
              await runWithBusy(async () => {
                const payload = { ...lessonForm, exerciseId: selectedExerciseId }
                const saved = lessonForm.id ? await updateLesson(lessonForm.id, payload) : await createLesson(selectedExerciseId, payload)
                pushSuccess(`Lesson ${lessonForm.id ? 'updated' : 'created'}.`)
                setLessonForm(saved)
                await refreshLessons(selectedExerciseId)
              })
            }}
            title="Lesson editor"
          />
        </Panel>

        <Panel title="Sentences">
          {selectedLessonId ? (
            <>
              <SentenceReorderList
                items={sentences}
                onReorder={async (nextItems) => {
                  if (!selectedLessonId) return
                  await runWithBusy(async () => {
                    setSentences(nextItems)
                    await reorderSentences(selectedLessonId, { sentenceIds: nextItems.map((item) => item.id ?? '').filter(Boolean) })
                    pushSuccess('Sentence order updated.')
                  })
                }}
              />
              <button
                className="mt-3 rounded border border-slate-300 px-3 py-2 text-sm"
                onClick={() => setImportOpen(true)}
                type="button"
              >
                Import JSON
              </button>
              <EntityForm
                disabled={busy}
                fields={[
                  { key: 'transcript', label: 'Transcript', value: sentenceForm.transcript },
                  { key: 'translation', label: 'Translation', value: sentenceForm.translation ?? '' },
                  { key: 'aliasesJson', label: 'Aliases JSON', value: sentenceForm.aliasesJson ?? '[]' },
                ]}
                onChange={(key, value) =>
                  setSentenceForm((current) => ({
                    ...current,
                    [key]: value,
                    lessonId: selectedLessonId,
                  }))
                }
                onDelete={async () => {
                  const sentenceId = sentenceForm.id
                  if (!sentenceId) return
                  await runWithBusy(async () => {
                    await deleteSentence(sentenceId)
                    pushSuccess('Sentence deleted.')
                    setSentenceForm(clone(EMPTY_SENTENCE))
                    await refreshSentences(selectedLessonId)
                  })
                }}
                onSave={async () => {
                  if (!selectedLessonId) {
                    throw new Error('Select a lesson first.')
                  }
                  await runWithBusy(async () => {
                    const payload = { ...sentenceForm, lessonId: selectedLessonId }
                    const saved = sentenceForm.id ? await updateSentence(sentenceForm.id, payload) : await createSentence(selectedLessonId, payload)
                    pushSuccess(`Sentence ${sentenceForm.id ? 'updated' : 'created'}.`)
                    setSentenceForm(saved)
                    await refreshSentences(selectedLessonId)
                  })
                }}
                title="Sentence editor"
              />
            </>
          ) : (
            <p className="text-sm text-slate-500">Select a lesson to manage sentences.</p>
          )}
        </Panel>
      </div>

      <SentenceJsonImportModal
        onClose={() => setImportOpen(false)}
        onImport={async (rows) => {
          if (!selectedLessonId) {
            throw new Error('Select a lesson first.')
          }
          const result = await importSentences(selectedLessonId, rows)
          pushSuccess(`Saved ${result.successCount}/${result.successCount + result.failedCount} rows.`)
          await refreshSentences(selectedLessonId)
          return result
        }}
        open={importOpen}
      />
    </section>
  )
}

type PanelProps = {
  title: string
  children: ReactNode
}

function Panel({ title, children }: PanelProps) {
  return (
    <section className="space-y-3 rounded-xl border border-slate-200 bg-slate-50 p-3">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">{title}</h2>
      {children}
    </section>
  )
}

type EntityListProps<T extends { id?: string }> = {
  items: T[]
  activeId: string | null
  label: (item: T) => string
  onSelect: (item: T) => void
}

function EntityList<T extends { id?: string }>({ items, activeId, label, onSelect }: EntityListProps<T>) {
  if (items.length === 0) {
    return <p className="text-sm text-slate-500">No items yet.</p>
  }

  return (
    <div className="space-y-2">
      {items.map((item) => (
        <button
          className={`block w-full rounded-lg border px-3 py-2 text-left text-sm ${
            item.id && item.id === activeId ? 'border-slate-900 bg-slate-900 text-white' : 'border-slate-200 bg-white'
          }`}
          key={item.id ?? label(item)}
          onClick={() => onSelect(item)}
          type="button"
        >
          {label(item)}
        </button>
      ))}
    </div>
  )
}

type Field = {
  key: string
  label: string
  value: string
}

type EntityFormProps = {
  title: string
  fields: Field[]
  disabled?: boolean
  onChange: (key: string, value: string) => void
  onSave: () => Promise<void>
  onDelete: () => Promise<void>
}

function EntityForm({ title, fields, disabled, onChange, onSave, onDelete }: EntityFormProps) {
  return (
    <div className="space-y-2 rounded-lg border border-slate-200 bg-white p-3">
      <h3 className="text-sm font-semibold text-slate-800">{title}</h3>
      {fields.map((field) => (
        <label className="block space-y-1 text-sm" key={field.key}>
          <span className="text-slate-500">{field.label}</span>
          <input
            className="w-full rounded border border-slate-300 px-3 py-2"
            disabled={disabled}
            onChange={(event) => onChange(field.key, event.target.value)}
            value={field.value}
          />
        </label>
      ))}
      <div className="flex gap-2 pt-1">
        <button
          className="rounded bg-slate-900 px-3 py-2 text-sm font-medium text-white disabled:opacity-50"
          disabled={disabled}
          onClick={() => void onSave()}
          type="button"
        >
          Save
        </button>
        <button
          className="rounded border border-rose-300 px-3 py-2 text-sm text-rose-700 disabled:opacity-50"
          disabled={disabled}
          onClick={() => void onDelete()}
          type="button"
        >
          Delete
        </button>
      </div>
    </div>
  )
}




