import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { DictionaryPopover } from '../../features/listening/components/DictionaryPopover'

const lookupDictionaryMock = vi.fn()

vi.mock('../../features/listening/services/listeningApi', async () => {
  const actual = await vi.importActual('../../features/listening/services/listeningApi')
  return {
    ...actual,
    lookupDictionary: (...args: unknown[]) => lookupDictionaryMock(...args),
  }
})

describe('dictionary popover', () => {
  beforeEach(() => {
    lookupDictionaryMock.mockReset()
  })

  it('shows dictionary entry when lookup succeeds', async () => {
    lookupDictionaryMock.mockResolvedValue({
      term: 'practice',
      entries: [
        {
          ipa: '/prak.tis/',
          ukAudioUrl: null,
          usAudioUrl: null,
          definitions: ['to do regularly'],
        },
      ],
    })

    render(<DictionaryPopover onClose={() => {}} term="practice" />)

    await waitFor(() => {
      expect(lookupDictionaryMock).toHaveBeenCalledWith('practice')
    })
    expect(await screen.findByText('IPA: /prak.tis/')).not.toBeNull()
    expect(screen.getByText('to do regularly')).not.toBeNull()
  })

  it('shows graceful fallback message when provider is unavailable', async () => {
    lookupDictionaryMock.mockResolvedValue({
      code: 'PROVIDER_UNAVAILABLE',
      message: 'Dictionary service is temporarily unavailable.',
    })

    const onClose = vi.fn()
    const user = userEvent.setup()
    render(<DictionaryPopover onClose={onClose} term="unstable" />)

    expect(await screen.findByText('Dictionary service is temporarily unavailable.')).not.toBeNull()

    await user.click(screen.getByRole('button', { name: 'Close' }))
    expect(onClose).toHaveBeenCalledTimes(1)
  })
})

