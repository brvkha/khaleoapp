import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { ListeningSettingsModal } from '../../features/listening/components/ListeningSettingsModal'
import { useListeningSettingsStore } from '../../store/listeningSettingsStore'

describe('listening settings modal', () => {
  beforeEach(() => {
    window.localStorage.clear()
    useListeningSettingsStore.getState().reset()
  })

  it('updates shortcut keys and persists settings to localStorage', async () => {
    const user = userEvent.setup()
    render(<ListeningSettingsModal onClose={() => {}} open />)

    const replayKeyInput = screen.getByLabelText('Replay key')
    const playPauseKeyInput = screen.getByLabelText('Play/Pause key')

    await user.clear(replayKeyInput)
    await user.type(replayKeyInput, 'r')
    await user.clear(playPauseKeyInput)
    await user.type(playPauseKeyInput, 'p')

    expect(useListeningSettingsStore.getState().replayKey).toBe('r')
    expect(useListeningSettingsStore.getState().playPauseKey).toBe('p')

    const persisted = JSON.parse(window.localStorage.getItem('listening-settings') ?? '{}')
    expect(persisted.replayKey).toBe('r')
    expect(persisted.playPauseKey).toBe('p')
  })

  it('supports transcript toggle overrides', async () => {
    const user = userEvent.setup()
    render(<ListeningSettingsModal onClose={() => {}} open />)

    await user.click(screen.getByLabelText('Transcript auto-scroll'))
    await user.click(screen.getByLabelText('Loop current transcript sentence on playback end'))

    expect(useListeningSettingsStore.getState().transcriptAutoScroll).toBe(false)
    expect(useListeningSettingsStore.getState().transcriptLoop).toBe(true)
  })
})

