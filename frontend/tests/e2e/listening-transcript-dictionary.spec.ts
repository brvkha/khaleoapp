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
  description: 'Listening practice for transcript, dictionary, and settings coverage.',
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

function buildDictionarySuccessResponse(term: string) {
  return {
    term,
    entries: [
      {
        ipa: '/həˈloʊ/',
        ukAudioUrl: null,
        usAudioUrl: null,
        definitions: ['a greeting used to start a conversation'],
      },
    ],
  }
}

test.describe('listening transcript dictionary and settings', () => {
  test('learner can use transcript, dictionary, and settings in one flow', async ({ page }) => {
    await page.addInitScript((session) => {
      localStorage.setItem('khaleo-auth-session', JSON.stringify(session))

      Object.defineProperty(HTMLMediaElement.prototype, 'play', {
        configurable: true,
        value: () => Promise.resolve(),
      })
      Object.defineProperty(HTMLMediaElement.prototype, 'pause', {
        configurable: true,
        value: () => undefined,
      })
    }, authenticatedSession)

    await page.route('**/api/v1/listening/**', async (route) => {
      const request = route.request()
      const url = new URL(request.url())
      const { pathname, searchParams } = url

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

      if (request.method() === 'POST' && pathname === '/api/v1/listening/media/access') {
        await route.fulfill({
          json: {
            url: '/mock-media/lesson-1.mp3',
            expiresAt: '2026-04-22T00:10:00.000Z',
          },
        })
        return
      }

      if (request.method() === 'GET' && pathname === '/api/v1/listening/dictionary') {
        const term = searchParams.get('term')
        if (term === 'Hello') {
          await route.fulfill({ json: buildDictionarySuccessResponse(term) })
          return
        }

        if (term === 'world') {
          await route.fulfill({ status: 503, body: 'dictionary provider unavailable' })
          return
        }
      }

      await route.continue()
    })

    await page.route('**/mock-media/**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'audio/mpeg',
        body: 'FAKEAUDIO',
      })
    })

    await page.goto('/listening')

    await expect(page.getByRole('heading', { name: 'Listening Workspace' })).toBeVisible()
    await expect(page.getByText('Current selection: IELTS Listening / Cambridge 20 / Part 1: City Sounds')).toBeVisible()

    await page.getByRole('button', { name: 'Full Transcript' }).click()
    await expect(page.getByText('Auto-scroll enabled')).toBeVisible()

    await page.getByRole('button', { name: 'Play' }).first().click()
    await expect(page.getByRole('button', { name: 'Hello' })).toBeVisible()

    await page.getByRole('button', { name: 'Hello' }).click()
    await expect(page.getByRole('heading', { name: 'Dictionary: Hello' })).toBeVisible()
    await expect(page.getByText('a greeting used to start a conversation')).toBeVisible()

    await page.getByRole('button', { name: 'Close' }).click()

    await page.getByRole('button', { name: 'world' }).click()
    await expect(page.getByText('Dictionary service unavailable for "world". Continue your practice!')).toBeVisible()

    await page.getByRole('button', { name: 'Settings' }).click()
    const replayKeyInput = page.getByLabel('Replay key')
    await expect(replayKeyInput).toHaveValue('Control')
    await replayKeyInput.fill('Alt')
    await page.getByRole('button', { name: 'Done' }).click()

    await page.reload()

    await page.getByRole('button', { name: 'Settings' }).click()
    await expect(page.getByLabel('Replay key')).toHaveValue('Alt')
  })
})



