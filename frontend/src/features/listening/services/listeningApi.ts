import axios from 'axios';
import type { UUID } from '../types';

/**
 * T042 - Learner Listening API client
 * Handles workspace queries and progress updates
 */

const API_BASE = '/api/v1/listening';

// ========== TYPES ==========

export interface SentenceWithProgress {
  id: UUID;
  orderIndex: number;
  transcript: string;
  translation: string | null;
  aliasesJson: string | null;
  mediaUrl: string | null;
  startTime: number | null;
  endTime: number | null;
  isCompleted: boolean;
}

export interface TopicListResponse {
  id: UUID;
  name: string;
  slug: string;
  description: string | null;
  status: string;
}

export interface ExerciseListResponse {
  id: UUID;
  topicId: UUID;
  name: string;
  slug: string;
  status: string;
}

export interface LessonListResponse {
  id: UUID;
  exerciseId: UUID;
  name: string;
  slug: string;
  status: string;
}

export interface LessonWorkspace {
  lessonId: UUID;
  name: string;
  slug: string;
  mediaUrl: string | null;
  mediaType: 'audio' | 'video' | null;
  sentences: SentenceWithProgress[];
  progressPercent: number;
  completedCount: number;
  totalCount: number;
}

export interface ProgressUpdateRequest {
  actionType: 'correct_check' | 'skip';
}

export interface ProgressUpdateResponse {
  progressId: UUID;
  sentenceId: UUID;
  completed: boolean;
  completionSource: 'correct_check' | 'skip';
  lessonProgressPercent: number;
  lessonCompletedCount: number;
  lessonTotalCount: number;
}

export interface DictionaryEntry {
  ipa: string | null;
  ukAudioUrl: string | null;
  usAudioUrl: string | null;
  definitions: string[];
}

export interface DictionaryLookupResponse {
  term: string;
  entries: DictionaryEntry[];
}

export interface DictionaryLookupFailureResponse {
  code: 'PROVIDER_UNAVAILABLE';
  message: string;
}

// ========== CATALOGUE QUERIES ==========

export async function getPublishedTopics(): Promise<TopicListResponse[]> {
  const { data } = await axios.get(`${API_BASE}/topics`);
  return data;
}

export async function getExercisesByTopicSlug(topicSlug: string): Promise<ExerciseListResponse[]> {
  const { data } = await axios.get(
    `${API_BASE}/topics/${topicSlug}/exercises`
  );
  return data;
}

export async function getLessonsByTopicAndExerciseSlug(
  topicSlug: string,
  exerciseSlug: string
): Promise<LessonListResponse[]> {
  const { data } = await axios.get(
    `${API_BASE}/topics/${topicSlug}/exercises/${exerciseSlug}/lessons`
  );
  return data;
}

// ========== WORKSPACE & PROGRESS ==========

export async function getLessonWorkspace(lessonId: UUID): Promise<LessonWorkspace> {
  const { data } = await axios.get(
    `${API_BASE}/lessons/${lessonId}/workspace`
  );
  return data;
}

export async function markSentenceComplete(
  sentenceId: UUID,
  actionType: 'correct_check' | 'skip'
): Promise<ProgressUpdateResponse> {
  const { data } = await axios.post(
    `${API_BASE}/progress/sentences/${sentenceId}`,
    { actionType } as ProgressUpdateRequest
  );
  return data;
}

export async function getSentenceProgress(sentenceId: UUID) {
  const { data } = await axios.get(
    `${API_BASE}/progress/sentences/${sentenceId}`
  );
  return data;
}

// ========== MEDIA ACCESS ==========

export async function requestMediaAccess(mediaUrl: string) {
  const { data } = await axios.post(
    `${API_BASE}/media/access`,
    { mediaUrl }
  );
  return data as { url: string; expiresAt: string };
}

export async function fetchMediaAsBlob(mediaUrl: string): Promise<Blob> {
  const access = await requestMediaAccess(mediaUrl);
  const response = await axios.get(access.url, {
    responseType: 'blob'
  });
  return response.data;
}

// ========== DICTIONARY ==========

export async function lookupDictionary(
  term: string
): Promise<DictionaryLookupResponse | DictionaryLookupFailureResponse> {
  try {
    const { data } = await axios.get(
      `${API_BASE}/dictionary`,
      { params: { term } }
    );
    return data;
  } catch {
    // Return graceful fallback on API failure
    return {
      code: 'PROVIDER_UNAVAILABLE',
      message: `Dictionary service unavailable for "${term}". Continue your practice!`
    };
  }
}
