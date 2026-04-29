import { requestJson } from '../../../../services/apiClient'
import type {
  ListeningExercise,
  ListeningLesson,
  ListeningSentence,
  ListeningTopic,
  ReorderSentencesRequest,
  SentenceImportResponse,
} from '../../../listening/types/listeningApi'

export async function listTopics(): Promise<ListeningTopic[]> {
  return await requestJson<ListeningTopic[]>('/api/v1/admin/listening/topics')
}

export async function getTopics(): Promise<ListeningTopic[]> {
  return await listTopics()
}

export async function getTopic(topicId: string): Promise<ListeningTopic> {
  return await requestJson<ListeningTopic>(`/api/v1/admin/listening/topics/${topicId}`)
}

export async function createTopic(payload: ListeningTopic): Promise<ListeningTopic> {
  return await requestJson<ListeningTopic>('/api/v1/admin/listening/topics', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function updateTopic(topicId: string, payload: ListeningTopic): Promise<ListeningTopic> {
  return await requestJson<ListeningTopic>(`/api/v1/admin/listening/topics/${topicId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteTopic(topicId: string): Promise<void> {
  await requestJson(`/api/v1/admin/listening/topics/${topicId}`, { method: 'DELETE' })
}

export async function listExercises(topicId?: string): Promise<ListeningExercise[]> {
  const url = topicId ? `/api/v1/admin/listening/exercises?topicId=${topicId}` : '/api/v1/admin/listening/exercises'
  return await requestJson<ListeningExercise[]>(url)
}

export async function getExercisesByTopic(topicId: string): Promise<ListeningExercise[]> {
  return await listExercises(topicId)
}

export async function createExercise(topicId: string, payload: ListeningExercise): Promise<ListeningExercise> {
  return await requestJson<ListeningExercise>(`/api/v1/admin/listening/exercises`, {
    method: 'POST',
    body: JSON.stringify({ ...payload, topicId }),
  })
}

export async function updateExercise(exerciseId: string, payload: ListeningExercise): Promise<ListeningExercise> {
  return await requestJson<ListeningExercise>(`/api/v1/admin/listening/exercises/${exerciseId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteExercise(exerciseId: string): Promise<void> {
  await requestJson(`/api/v1/admin/listening/exercises/${exerciseId}`, { method: 'DELETE' })
}

export async function listLessons(exerciseId?: string): Promise<ListeningLesson[]> {
  const url = exerciseId ? `/api/v1/admin/listening/lessons?exerciseId=${exerciseId}` : '/api/v1/admin/listening/lessons'
  return await requestJson<ListeningLesson[]>(url)
}

export async function getLessonsByExercise(exerciseId: string): Promise<ListeningLesson[]> {
  return await listLessons(exerciseId)
}

export async function createLesson(exerciseId: string, payload: ListeningLesson): Promise<ListeningLesson> {
  return await requestJson<ListeningLesson>(`/api/v1/admin/listening/lessons`, {
    method: 'POST',
    body: JSON.stringify({ ...payload, exerciseId }),
  })
}

export async function updateLesson(lessonId: string, payload: ListeningLesson): Promise<ListeningLesson> {
  return await requestJson<ListeningLesson>(`/api/v1/admin/listening/lessons/${lessonId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteLesson(lessonId: string): Promise<void> {
  await requestJson(`/api/v1/admin/listening/lessons/${lessonId}`, { method: 'DELETE' })
}

export async function listSentences(lessonId?: string): Promise<ListeningSentence[]> {
  const url = lessonId ? `/api/v1/admin/listening/sentences?lessonId=${lessonId}` : '/api/v1/admin/listening/sentences'
  return await requestJson<ListeningSentence[]>(url)
}

export async function getSentencesByLesson(lessonId: string): Promise<ListeningSentence[]> {
  return await listSentences(lessonId)
}

export async function createSentence(lessonId: string, payload: ListeningSentence): Promise<ListeningSentence> {
  return await requestJson<ListeningSentence>(`/api/v1/admin/listening/sentences`, {
    method: 'POST',
    body: JSON.stringify({ ...payload, lessonId }),
  })
}

export async function updateSentence(sentenceId: string, payload: ListeningSentence): Promise<ListeningSentence> {
  return await requestJson<ListeningSentence>(`/api/v1/admin/listening/sentences/${sentenceId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteSentence(sentenceId: string): Promise<void> {
  await requestJson(`/api/v1/admin/listening/sentences/${sentenceId}`, { method: 'DELETE' })
}

export async function reorderSentences(lessonId: string, payload: ReorderSentencesRequest): Promise<void> {
  await requestJson(`/api/v1/admin/listening/lessons/${lessonId}/sentences/reorder`, {
    method: 'POST', // Backend explicitly marked as POST with ReorderSentencesRequest body
    body: JSON.stringify(payload),
  })
}

export async function reorderSentencesGlobally(payload: ReorderSentencesRequest): Promise<void> {
  await requestJson(`/api/v1/admin/listening/sentences/reorder`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function importSentences(lessonId: string, rows: ListeningSentence[]): Promise<SentenceImportResponse> {
  return await requestJson<SentenceImportResponse>(`/api/v1/admin/listening/sentences/import`, {
    method: 'POST',
    body: JSON.stringify({ lessonId, rows }),
  })
}
