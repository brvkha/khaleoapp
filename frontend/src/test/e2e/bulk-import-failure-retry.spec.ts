import { test } from '@playwright/test'

test.describe('bulk import failure retry', () => {
  test.skip(true, 'requires deterministic chunk failure injection in e2e environment')
})


