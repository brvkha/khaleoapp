import { expect, test } from '@playwright/test'

const authenticatedSession = {
  currentUser: {
    id: 'learner-001',
    username: 'learner-001',
    email: 'learner001@khaleo.app',
    role: 'USER',
    verified: true,
    banned: false,
  },
  accessToken: 'access-token',
  refreshToken: 'refresh-token',
}

const publishedTopic = {
  id: 'topic-1',
  name: 'IELTS Listening',
  slug: 'ielts-listening',
  description: 'Listening practice for dictation flow coverage.',
  status: 'published',
}

const publishedExercise = {
  id: 'exercise-1',
  topicId: publishedTopic.id,
  name: 'Cambridge 20',
  slug: 'cambridge-20',
  status: 'published',
}

const publishedLesson = {
  id: 'lesson-1',
  exerciseId: publishedExercise.id,
  name: 'Part 1: City Sounds',
  slug: 'part-1-city-sounds',
  status: 'published',
}

const lessonWorkspace = {
  lessonId: publishedLesson.id,
  name: publishedLesson.name,
  slug: publishedLesson.slug,
  mediaUrl: '/mock-media/lesson-1.mp3',
  mediaType: 'audio' as const,
  progressPercent: 0,
  completedCount: 0,
  totalCount: 2,
  sentences: [
    {
      id: 'sentence-1',
      orderIndex: 1,
      transcript: 'Hello world',
      translation: 'Xin chào thế giới',
      aliasesJson: null,
      mediaUrl: null,
      startTime: null,
      endTime: null,
      isCompleted: false,
    },
    {
      id: 'sentence-2',
      orderIndex: 2,
      transcript: 'Practice makes progress',
      translation: 'Luyện tập tạo nên tiến bộ',
      aliasesJson: null,
      mediaUrl: null,
      startTime: null,
      endTime: null,
      isCompleted: false,
    },
  ],
}

test.describe('listening dictation core', () => {
  test('learner can check and skip dictation in workspace smoke', async ({ page }) => {
    await page.addInitScript((session) => {
      localStorage.setItem('khaleo-auth-session', JSON.stringify(session))
    }, authenticatedSession)

    await page.route('**/api/v1/listening/**', async (route) => {
      const request = route.request()
      const url = new URL(request.url())
      const { pathname } = url

      if (request.method() === 'GET' && pathname === '/api/v1/listening/topics') {
        await route.fulfill({ json: [publishedTopic] })
        return
      }

      if (request.method() === 'GET' && pathname === '/api/v1/listening/topics/ielts-listening/exercises') {
        await route.fulfill({ json: [publishedExercise] })
        return
      }

      if (request.method() === 'GET' && pathname === '/api/v1/listening/topics/ielts-listening/exercises/cambridge-20/lessons') {
        await route.fulfill({ json: [publishedLesson] })
        return
      }

      if (request.method() === 'GET' && pathname === '/api/v1/listening/lessons/lesson-1/workspace') {
        await route.fulfill({ json: lessonWorkspace })
        return
      }

      if (request.method() === 'POST' && pathname.startsWith('/api/v1/listening/progress/sentences/')) {
        await route.fulfill({
          json: {
            progressId: 'progress-1',
            sentenceId: pathname.split('/').pop(),
            completed: true,
            completionSource: 'correct_check',
            lessonProgressPercent: pathname.endsWith('sentence-1') ? 50 : 100,
            lessonCompletedCount: pathname.endsWith('sentence-1') ? 1 : 2,
            lessonTotalCount: 2,
          },
        })
        return
      }

      await route.continue()
    })

    await page.goto('/listening')

    await expect(page.getByRole('heading', { name: 'Listening Workspace' })).toBeVisible()
    await expect(page.getByText('Current selection: IELTS Listening / Cambridge 20 / Part 1: City Sounds')).toBeVisible()

    const answerBox = page.getByRole('textbox')
    await expect(page.getByRole('heading', { name: 'Sentence 1 of 2' })).toBeVisible()
    await answerBox.fill('Hello world')
    await page.getByRole('button', { name: 'Check (Enter)' }).click()
    await expect(page.getByText('✓ Correct!')).toBeVisible()
    await expect(page.getByText('Your answer: Hello world')).toBeVisible()

    await page.getByRole('button', { name: 'Next →' }).click()
    await expect(page.getByRole('heading', { name: 'Sentence 2 of 2' })).toBeVisible()

    await page.getByRole('button', { name: 'Skip (Esc)' }).click()
    await expect(page.getByText('Skipped. Answer: Practice makes progress')).toBeVisible()
    await expect(page.getByText('Completed 2/2 sentences')).toBeVisible()
    await expect(page.getByText('100%')).toBeVisible()
  })
})



