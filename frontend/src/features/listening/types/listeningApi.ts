export type ListeningStatus = 'draft' | 'published' | 'archived'

export type ListeningTopic = {
  id?: string
  name: string
  slug: string
  description?: string | null
  status?: ListeningStatus | string
  createdAt?: string
  updatedAt?: string
}

export type ListeningExercise = {
  id?: string
  topicId?: string | null
  name: string
  slug: string
  orderIndex?: number | null
  status?: ListeningStatus | string
  createdAt?: string
  updatedAt?: string
}

export type ListeningLesson = {
  id?: string
  exerciseId?: string | null
  name: string
  slug: string
  orderIndex?: number | null
  mediaUrl?: string | null
  mediaType?: string | null
  status?: ListeningStatus | string
  createdAt?: string
  updatedAt?: string
}

export type ListeningSentence = {
  id?: string
  lessonId?: string | null
  orderIndex?: number | null
  transcript: string
  translation?: string | null
  aliasesJson?: string | null
  mediaUrl?: string | null
  startTime?: number | null
  endTime?: number | null
  createdAt?: string
  updatedAt?: string
}

export type SentenceImportError = {
  row: number
  message: string
}

export type SentenceImportResponse = {
  successCount: number
  failedCount: number
  errors: SentenceImportError[]
}

export type ReorderSentencesRequest = {
  sentenceIds: string[]
}

export type MediaAccessRequest = {
  mediaUrl: string
}

export type MediaAccessResponse = {
  url: string
  expiresAt: string
}

