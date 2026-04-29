import { useEffect, useState } from 'react'
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
type TabType = 'topics' | 'exercises' | 'lessons' | 'sentences'
export function AdminListeningPage() {
  const pushSuccess = useNotificationStore((state) => state.pushSuccess)
  const pushError = useNotificationStore((state) => state.pushError)
  const [activeTab, setActiveTab] = useState<TabType>('topics')
  const [topics, setTopics] = useState<ListeningTopic[]>([])
  const [exercises, setExercises] = useState<ListeningExercise[]>([])
  const [lessons, setLessons] = useState<ListeningLesson[]>([])
  const [sentences, setSentences] = useState<ListeningSentence[]>([])
  // Filters for tabs
  const [filterTopicId, setFilterTopicId] = useState<string>('')
  const [filterExerciseId, setFilterExerciseId] = useState<string>('')
  const [filterLessonId, setFilterLessonId] = useState<string>('')
  const [topicForm, setTopicForm] = useState<ListeningTopic | null>(null)
  const [exerciseForm, setExerciseForm] = useState<ListeningExercise | null>(null)
  const [lessonForm, setLessonForm] = useState<ListeningLesson | null>(null)
  const [sentenceForm, setSentenceForm] = useState<ListeningSentence | null>(null)
  const [importOpen, setImportOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const runWithBusy = async (action: () => Promise<void>) => {
    setBusy(true)
    try {
      await action()
    } finally {
      setBusy(false)
    }
  }
  const loadData = async () => {
    await runWithBusy(async () => {
      try {
        const [tops, exes, less, sents] = await Promise.all([
           listTopics(),
           listExercises(filterTopicId || undefined),
           listLessons(filterExerciseId || undefined),
           listSentences(filterLessonId || undefined),
        ])
        setTopics(tops)
        setExercises(exes)
        setLessons(less)
        setSentences(sents)
      } catch (err: any) {
        pushError(err.message || 'Failed to load data')
      }
    })
  }
  useEffect(() => {
    void loadData()
  }, [filterTopicId, filterExerciseId, filterLessonId])
  return (
    <section className={`space-y-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm ${busy ? 'opacity-70 pointer-events-none' : ''}`}>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Listening CMS</h1>
          <p className="text-sm text-slate-600">Manage listening content through flat entity tabs.</p>
        </div>
      </div>
      <div className="flex border-b border-slate-200">
        {[
          { id: 'topics', label: 'Topics' },
          { id: 'exercises', label: 'Exercises' },
          { id: 'lessons', label: 'Lessons' },
          { id: 'sentences', label: 'Sentences' },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as TabType)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
              activeTab === tab.id ? 'border-slate-900 text-slate-900' : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <div className="pt-4">
        {activeTab === 'topics' && (
          <div className="space-y-4">
            <div className="flex justify-end">
               <button className="bg-slate-900 text-white px-3 py-2 text-sm rounded" onClick={() => setTopicForm(clone(EMPTY_TOPIC))}>Create Topic</button>
            </div>
            {topicForm ? (
               <EntityForm
                 title="Topic Form"
                 fields={[
                   { key: 'name', label: 'Name', value: topicForm.name },
                   { key: 'slug', label: 'Slug', value: topicForm.slug },
                   { key: 'description', label: 'Description', value: topicForm.description ?? '' },
                 ]}
                 onChange={(key, value) => setTopicForm({ ...topicForm, [key]: value })}
                 onSave={async () => {
                   await runWithBusy(async () => {
                     if (topicForm.id) await updateTopic(topicForm.id, topicForm)
                     else await createTopic(topicForm)
                     pushSuccess('Saved topic')
                     setTopicForm(null)
                     await loadData()
                   })
                 }}
                 onCancel={() => setTopicForm(null)}
               />
            ) : (
              <table className="w-full text-left text-sm border">
                <thead className="bg-slate-50 border-b"><tr><th className="p-2">Name</th><th className="p-2">Slug</th><th className="p-2">Status</th><th className="p-2 w-24">Actions</th></tr></thead>
                <tbody>
                  {topics.map(t => (
                    <tr key={t.id} className="border-b">
                      <td className="p-2">{t.name}</td><td className="p-2">{t.slug}</td><td className="p-2">{t.status}</td>
                      <td className="p-2 space-x-2 flex">
                        <button className="text-blue-600 hover:underline" onClick={() => setTopicForm(t)}>Edit</button>
                        <button className="text-rose-600 hover:underline" onClick={async () => {
                           if(t.id) await deleteTopic(t.id); await loadData(); pushSuccess('Deleted');
                        }}>Del</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
        {activeTab === 'exercises' && (
          <div className="space-y-4">
            <div className="flex justify-between">
               <select className="border border-slate-300 rounded px-2 py-1 text-sm" value={filterTopicId} onChange={(e) => setFilterTopicId(e.target.value)}>
                 <option value="">All Topics</option>
                 {topics.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
               </select>
               <button className="bg-slate-900 text-white px-3 py-2 text-sm rounded" onClick={() => setExerciseForm(clone(EMPTY_EXERCISE))}>Create Exercise</button>
            </div>
            {exerciseForm ? (
               <EntityForm
                 title="Exercise Form"
                 fields={[
                   { key: 'topicId', label: 'Topic ID', value: exerciseForm.topicId || filterTopicId },
                   { key: 'name', label: 'Name', value: exerciseForm.name },
                   { key: 'slug', label: 'Slug', value: exerciseForm.slug },
                 ]}
                 onChange={(key, value) => setExerciseForm({ ...exerciseForm, [key]: value })}
                 onSave={async () => {
                   await runWithBusy(async () => {
                     const tId = exerciseForm.topicId || filterTopicId
                     if(!tId) throw new Error("Topic ID required")
                     if (exerciseForm.id) await updateExercise(exerciseForm.id, {...exerciseForm, topicId: tId})
                     else await createExercise(tId, exerciseForm)
                     pushSuccess('Saved exercise')
                     setExerciseForm(null)
                     await loadData()
                   })
                 }}
                 onCancel={() => setExerciseForm(null)}
               />
            ) : (
              <table className="w-full text-left text-sm border">
                <thead className="bg-slate-50 border-b"><tr><th className="p-2">Name</th><th className="p-2">Slug</th><th className="p-2">Order</th><th className="p-2 w-24">Actions</th></tr></thead>
                <tbody>
                  {exercises.map(t => (
                    <tr key={t.id} className="border-b">
                      <td className="p-2">{t.name}</td><td className="p-2">{t.slug}</td><td className="p-2">{t.orderIndex}</td>
                      <td className="p-2 space-x-2 flex">
                        <button className="text-blue-600 hover:underline" onClick={() => setExerciseForm(t)}>Edit</button>
                        <button className="text-rose-600 hover:underline" onClick={async () => {
                           if(t.id) await deleteExercise(t.id); await loadData(); pushSuccess('Deleted');
                        }}>Del</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
        {activeTab === 'lessons' && (
          <div className="space-y-4">
            <div className="flex justify-between">
               <select className="border border-slate-300 rounded px-2 py-1 text-sm" value={filterExerciseId} onChange={(e) => setFilterExerciseId(e.target.value)}>
                 <option value="">All Exercises</option>
                 {exercises.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
               </select>
               <button className="bg-slate-900 text-white px-3 py-2 text-sm rounded" onClick={() => setLessonForm(clone(EMPTY_LESSON))}>Create Lesson</button>
            </div>
            {lessonForm ? (
               <EntityForm
                 title="Lesson Form"
                 fields={[
                   { key: 'exerciseId', label: 'Exercise ID', value: lessonForm.exerciseId || filterExerciseId },
                   { key: 'name', label: 'Name', value: lessonForm.name },
                   { key: 'slug', label: 'Slug', value: lessonForm.slug },
                   { key: 'mediaUrl', label: 'Media URL', value: lessonForm.mediaUrl ?? '' },
                 ]}
                 onChange={(key, value) => setLessonForm({ ...lessonForm, [key]: value })}
                 onSave={async () => {
                   await runWithBusy(async () => {
                     const eId = lessonForm.exerciseId || filterExerciseId
                     if(!eId) throw new Error("Exercise ID required")
                     if (lessonForm.id) await updateLesson(lessonForm.id, {...lessonForm, exerciseId: eId})
                     else await createLesson(eId, lessonForm)
                     pushSuccess('Saved lesson')
                     setLessonForm(null)
                     await loadData()
                   })
                 }}
                 onCancel={() => setLessonForm(null)}
               />
            ) : (
              <table className="w-full text-left text-sm border">
                <thead className="bg-slate-50 border-b"><tr><th className="p-2">Name</th><th className="p-2">Slug</th><th className="p-2">Order</th><th className="p-2 w-24">Actions</th></tr></thead>
                <tbody>
                  {lessons.map(t => (
                    <tr key={t.id} className="border-b">
                      <td className="p-2">{t.name}</td><td className="p-2">{t.slug}</td><td className="p-2">{t.orderIndex}</td>
                      <td className="p-2 space-x-2 flex">
                        <button className="text-blue-600 hover:underline" onClick={() => setLessonForm(t)}>Edit</button>
                        <button className="text-rose-600 hover:underline" onClick={async () => {
                           if(t.id) await deleteLesson(t.id); await loadData(); pushSuccess('Deleted');
                        }}>Del</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
        {activeTab === 'sentences' && (
          <div className="space-y-4">
            <div className="flex justify-between items-center">
               <select className="border border-slate-300 rounded px-2 py-1 text-sm" value={filterLessonId} onChange={(e) => setFilterLessonId(e.target.value)}>
                 <option value="">All Lessons</option>
                 {lessons.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
               </select>
               <div className="space-x-2">
                 <button className="border border-slate-300 px-3 py-2 text-sm rounded bg-slate-50" onClick={() => setImportOpen(true)}>Import JSON</button>
                 <button className="bg-slate-900 text-white px-3 py-2 text-sm rounded" onClick={() => setSentenceForm(clone(EMPTY_SENTENCE))}>Create Sentence</button>
               </div>
            </div>
            {sentenceForm ? (
               <EntityForm
                 title="Sentence Form"
                 fields={[
                   { key: 'lessonId', label: 'Lesson ID', value: sentenceForm.lessonId || filterLessonId },
                   { key: 'transcript', label: 'Transcript', value: sentenceForm.transcript },
                   { key: 'translation', label: 'Translation', value: sentenceForm.translation ?? '' },
                 ]}
                 onChange={(key, value) => setSentenceForm({ ...sentenceForm, [key]: value })}
                 onSave={async () => {
                   await runWithBusy(async () => {
                     const lId = sentenceForm.lessonId || filterLessonId
                     if(!lId) throw new Error("Lesson ID required")
                     if (sentenceForm.id) await updateSentence(sentenceForm.id, {...sentenceForm, lessonId: lId})
                     else await createSentence(lId, sentenceForm)
                     pushSuccess('Saved sentence')
                     setSentenceForm(null)
                     await loadData()
                   })
                 }}
                 onCancel={() => setSentenceForm(null)}
               />
            ) : (
              <div className="flex gap-4 items-start">
                 <div className="flex-1 overflow-x-auto">
                    <table className="w-full text-left text-sm border">
                      <thead className="bg-slate-50 border-b"><tr><th className="p-2">Order</th><th className="p-2">Transcript</th><th className="p-2">Translation</th><th className="p-2 w-24">Actions</th></tr></thead>
                      <tbody>
                        {sentences.map(t => (
                          <tr key={t.id} className="border-b">
                            <td className="p-2">{t.orderIndex}</td><td className="p-2 truncate max-w-xs">{t.transcript}</td><td className="p-2 truncate max-w-xs">{t.translation}</td>
                            <td className="p-2 space-x-2 flex">
                              <button className="text-blue-600 hover:underline" onClick={() => setSentenceForm(t)}>Edit</button>
                              <button className="text-rose-600 hover:underline" onClick={async () => {
                                 if(t.id) await deleteSentence(t.id); await loadData(); pushSuccess('Deleted');
                              }}>Del</button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                 </div>
                 {filterLessonId && (
                   <div className="w-[300px] border p-3 rounded space-y-3 bg-slate-50">
                     <h3 className="font-semibold text-sm">Reorder View</h3>
                     <p className="text-xs text-slate-500">Drag to reorder the selected lesson</p>
                     <SentenceReorderList
                       items={sentences}
                       onReorder={async (nextItems) => {
                         setSentences(nextItems)
                         await reorderSentences(filterLessonId, { sentenceIds: nextItems.map(x => x.id!).filter(Boolean) })
                         pushSuccess('Reordered successfully')
                       }}
                     />
                   </div>
                 )}
              </div>
            )}
            <SentenceJsonImportModal
              open={importOpen}
              onClose={() => setImportOpen(false)}
              onImport={async (rows) => {
                if(!filterLessonId) throw new Error("Please select a lesson first in the dropdown to import sentences.")
                const result = await importSentences(filterLessonId, rows)
                pushSuccess(`Imported rows: ${result.successCount}`)
                await loadData()
                return result
              }}
            />
          </div>
        )}
      </div>
    </section>
  )
}
type EntityFormProps = {
  title: string
  fields: { key: string; label: string; value: string }[]
  onChange: (key: string, value: string) => void
  onSave: () => Promise<void>
  onCancel: () => void
}
function EntityForm({ title, fields, onChange, onSave, onCancel }: EntityFormProps) {
  return (
    <div className="space-y-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
      <h3 className="font-semibold text-slate-800">{title}</h3>
      <div className="grid gap-4 sm:grid-cols-2">
        {fields.map((f) => (
          <label key={f.key} className="block text-sm">
            <span className="text-slate-600 block mb-1">{f.label}</span>
            <input
              className="w-full border-slate-300 rounded"
              value={f.value}
              onChange={(e) => onChange(f.key, e.target.value)}
            />
          </label>
        ))}
      </div>
      <div className="flex gap-2 justify-end">
        <button className="px-3 py-2 border rounded border-slate-300" onClick={onCancel}>Cancel</button>
        <button className="bg-slate-900 text-white px-3 py-2 rounded" onClick={() => void onSave()}>Save</button>
      </div>
    </div>
  )
}
