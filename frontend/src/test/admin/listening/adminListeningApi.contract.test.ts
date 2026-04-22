import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as adminListeningApi from '../../../features/admin/listening/services/adminListeningApi';

/**
 * Contract tests for Admin Listening API client.
 * Validates request/response shapes and client behavior.
 */
describe('Admin Listening API Contract Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Topic CRUD', () => {
    it('should fetch all topics with correct response shape', async () => {
      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify([
              {
                id: 'topic-1',
                name: 'IELTS Listening',
                slug: 'ielts-listening',
                description: 'IELTS materials',
                status: 'draft',
                createdAt: '2026-04-21T00:00:00Z',
                updatedAt: '2026-04-21T00:00:00Z',
              },
            ]),
            { status: 200 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.getTopics();

      expect(response).toHaveLength(1);
      expect(response[0]).toHaveProperty('id');
      expect(response[0]).toHaveProperty('name');
      expect(response[0]).toHaveProperty('slug');
      expect(response[0]).toHaveProperty('status');
    });

    it('should create topic with required fields', async () => {
      const payload = {
        name: 'New Topic',
        slug: 'new-topic',
        description: 'Description',
        status: 'draft' as const,
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify({
              id: 'new-id',
              ...payload,
              createdAt: '2026-04-21T00:00:00Z',
              updatedAt: '2026-04-21T00:00:00Z',
            }),
            { status: 201 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.createTopic(payload);

      expect(response).toHaveProperty('id');
      expect(response.name).toBe(payload.name);
      expect(response.slug).toBe(payload.slug);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/admin/listening/topics'),
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
        })
      );
    });

    it('should get topic by ID', async () => {
      const topicId = 'topic-123';
      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify({
              id: topicId,
              name: 'Test Topic',
              slug: 'test-topic',
              status: 'draft',
            }),
            { status: 200 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.getTopic(topicId);

      expect(response.id).toBe(topicId);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/topics/${topicId}`)
      );
    });

    it('should update topic', async () => {
      const topicId = 'topic-123';
      const updatePayload = {
        name: 'Updated',
        slug: 'updated',
        status: 'published' as const,
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify({ id: topicId, ...updatePayload }), {
            status: 200,
          })
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.updateTopic(topicId, updatePayload);

      expect(response.name).toBe(updatePayload.name);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/topics/${topicId}`),
        expect.objectContaining({
          method: 'PUT',
        })
      );
    });

    it('should delete topic', async () => {
      const topicId = 'topic-123';
      const mockFetch = vi.fn(() =>
        Promise.resolve(new Response('', { status: 204 }))
      );
      global.fetch = mockFetch;

      await adminListeningApi.deleteTopic(topicId);

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/topics/${topicId}`),
        expect.objectContaining({
          method: 'DELETE',
        })
      );
    });
  });

  describe('Exercise CRUD', () => {
    it('should create exercise under topic', async () => {
      const topicId = 'topic-123';
      const payload = {
        name: 'Cambridge Test 20',
        slug: 'cambridge-20',
        status: 'draft' as const,
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify({
              id: 'ex-1',
              topicId,
              ...payload,
            }),
            { status: 201 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.createExercise(topicId, payload);

      expect(response.topicId).toBe(topicId);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/topics/${topicId}/exercises`),
        expect.objectContaining({ method: 'POST' })
      );
    });

    it('should get exercises for topic', async () => {
      const topicId = 'topic-123';
      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify([
              { id: 'ex-1', name: 'Exercise 1', topicId },
              { id: 'ex-2', name: 'Exercise 2', topicId },
            ]),
            { status: 200 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.getExercisesByTopic(topicId);

      expect(response).toHaveLength(2);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/topics/${topicId}/exercises`)
      );
    });

    it('should update exercise', async () => {
      const exerciseId = 'ex-123';
      const updatePayload = {
        name: 'Updated Exercise',
        slug: 'updated',
        status: 'published' as const,
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify({ id: exerciseId, ...updatePayload }), {
            status: 200,
          })
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.updateExercise(exerciseId, updatePayload);

      expect(response.name).toBe(updatePayload.name);
    });

    it('should delete exercise', async () => {
      const exerciseId = 'ex-123';
      const mockFetch = vi.fn(() =>
        Promise.resolve(new Response('', { status: 204 }))
      );
      global.fetch = mockFetch;

      await adminListeningApi.deleteExercise(exerciseId);

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/exercises/${exerciseId}`),
        expect.objectContaining({ method: 'DELETE' })
      );
    });
  });

  describe('Lesson CRUD', () => {
    it('should create lesson under exercise', async () => {
      const exerciseId = 'ex-123';
      const payload = {
        name: 'Test 1 - Part 1',
        slug: 'test-1-part-1',
        status: 'draft' as const,
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify({
              id: 'lesson-1',
              exerciseId,
              ...payload,
            }),
            { status: 201 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.createLesson(exerciseId, payload);

      expect(response.exerciseId).toBe(exerciseId);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/exercises/${exerciseId}/lessons`),
        expect.objectContaining({ method: 'POST' })
      );
    });

    it('should get lessons for exercise', async () => {
      const exerciseId = 'ex-123';
      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify([
              { id: 'lesson-1', name: 'Lesson 1', exerciseId },
            ]),
            { status: 200 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.getLessonsByExercise(exerciseId);

      expect(response).toHaveLength(1);
    });

    it('should update lesson', async () => {
      const lessonId = 'lesson-123';
      const updatePayload = {
        name: 'Updated Lesson',
        slug: 'updated',
        status: 'published' as const,
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify({ id: lessonId, ...updatePayload }), {
            status: 200,
          })
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.updateLesson(lessonId, updatePayload);

      expect(response.name).toBe(updatePayload.name);
    });

    it('should delete lesson', async () => {
      const lessonId = 'lesson-123';
      const mockFetch = vi.fn(() =>
        Promise.resolve(new Response('', { status: 204 }))
      );
      global.fetch = mockFetch;

      await adminListeningApi.deleteLesson(lessonId);

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/lessons/${lessonId}`),
        expect.objectContaining({ method: 'DELETE' })
      );
    });
  });

  describe('Sentence CRUD', () => {
    it('should create sentence under lesson', async () => {
      const lessonId = 'lesson-123';
      const payload = {
        transcript: 'The cat sat on the mat',
        translation: 'Con mèo ngồi trên thảm',
        aliasesJson: '["the cat sat"]',
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify({
              id: 'sent-1',
              lessonId,
              orderIndex: 0,
              ...payload,
            }),
            { status: 201 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.createSentence(lessonId, payload);

      expect(response.lessonId).toBe(lessonId);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/lessons/${lessonId}/sentences`),
        expect.objectContaining({ method: 'POST' })
      );
    });

    it('should get sentences for lesson', async () => {
      const lessonId = 'lesson-123';
      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify([
              {
                id: 'sent-1',
                transcript: 'Sentence 1',
                orderIndex: 0,
                lessonId,
              },
              {
                id: 'sent-2',
                transcript: 'Sentence 2',
                orderIndex: 1,
                lessonId,
              },
            ]),
            { status: 200 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.getSentencesByLesson(lessonId);

      expect(response).toHaveLength(2);
      expect(response[0].orderIndex).toBe(0);
      expect(response[1].orderIndex).toBe(1);
    });

    it('should update sentence', async () => {
      const sentenceId = 'sent-123';
      const updatePayload = {
        transcript: 'Updated transcript',
        translation: 'Updated translation',
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify({ id: sentenceId, ...updatePayload }), {
            status: 200,
          })
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.updateSentence(sentenceId, updatePayload);

      expect(response.transcript).toBe(updatePayload.transcript);
    });

    it('should delete sentence', async () => {
      const sentenceId = 'sent-123';
      const mockFetch = vi.fn(() =>
        Promise.resolve(new Response('', { status: 204 }))
      );
      global.fetch = mockFetch;

      await adminListeningApi.deleteSentence(sentenceId);

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(`/api/v1/admin/listening/sentences/${sentenceId}`),
        expect.objectContaining({ method: 'DELETE' })
      );
    });
  });

  describe('Sentence Reorder', () => {
    it('should reorder sentences and return persisted order', async () => {
      const lessonId = 'lesson-123';
      const sentenceIds = ['sent-3', 'sent-1', 'sent-2'];

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify({
              lessonId,
              sentenceIds,
              message: 'Reordered successfully',
            }),
            { status: 200 }
          )
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.reorderSentences(lessonId, sentenceIds);

      expect(response.sentenceIds).toEqual(sentenceIds);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(
          `/api/v1/admin/listening/lessons/${lessonId}/sentences/reorder`
        ),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ sentenceIds }),
        })
      );
    });
  });

  describe('Sentence JSON Import', () => {
    it('should import sentences and return partial success response', async () => {
      const lessonId = 'lesson-123';
      const importRows = [
        { transcript: 'Sentence 1', translation: 'Translation 1' },
        { transcript: 'Sentence 2', translation: null },
        { transcript: '', translation: 'Will fail' },
      ];

      const mockResponse = {
        successCount: 2,
        failedCount: 1,
        errors: [
          {
            rowIndex: 2,
            message: 'Transcript is required',
          },
        ],
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify(mockResponse), { status: 200 })
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.importSentences(lessonId, importRows);

      expect(response.successCount).toBe(2);
      expect(response.failedCount).toBe(1);
      expect(response.errors).toHaveLength(1);
      expect(response.errors[0].rowIndex).toBe(2);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining(
          `/api/v1/admin/listening/lessons/${lessonId}/sentences/import-json`
        ),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(importRows),
        })
      );
    });

    it('should handle import with all valid rows', async () => {
      const lessonId = 'lesson-123';
      const importRows = [
        { transcript: 'Valid 1', translation: 'Translation 1' },
        { transcript: 'Valid 2', translation: null },
      ];

      const mockResponse = {
        successCount: 2,
        failedCount: 0,
        errors: [],
      };

      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify(mockResponse), { status: 200 })
        )
      );
      global.fetch = mockFetch;

      const response = await adminListeningApi.importSentences(lessonId, importRows);

      expect(response.successCount).toBe(2);
      expect(response.failedCount).toBe(0);
      expect(response.errors).toHaveLength(0);
    });
  });

  describe('Error Handling', () => {
    it('should handle 404 not found', async () => {
      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify({ message: 'Not found' }), {
            status: 404,
          })
        )
      );
      global.fetch = mockFetch;

      await expect(adminListeningApi.getTopic('invalid-id')).rejects.toThrow();
    });

    it('should handle 400 bad request', async () => {
      const payload = { name: '', slug: '' };
      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(
            JSON.stringify({
              message: 'Validation failed',
              errors: { name: 'Name is required' },
            }),
            { status: 400 }
          )
        )
      );
      global.fetch = mockFetch;

      await expect(
        adminListeningApi.createTopic(payload as any)
      ).rejects.toThrow();
    });

    it('should handle 500 server error', async () => {
      const mockFetch = vi.fn(() =>
        Promise.resolve(
          new Response(JSON.stringify({ message: 'Internal server error' }), {
            status: 500,
          })
        )
      );
      global.fetch = mockFetch;

      await expect(adminListeningApi.getTopics()).rejects.toThrow();
    });
  });
});
