import { useCallback, useMemo, useRef, useState } from 'react'

export function useListeningAudioPlayer() {
  const audioRef = useRef<HTMLAudioElement | null>(null)
  const [currentObjectUrl, setCurrentObjectUrl] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const releaseObjectUrl = useCallback(() => {
    if (currentObjectUrl) {
      URL.revokeObjectURL(currentObjectUrl)
      setCurrentObjectUrl(null)
    }
  }, [currentObjectUrl])

  const setAudioElement = useCallback((element: HTMLAudioElement | null) => {
    audioRef.current = element
  }, [])

  const playBlobUrl = useCallback(
    async (url: string) => {
      setIsLoading(true)
      setError(null)
      try {
        releaseObjectUrl()
        const response = await fetch(url)
        if (!response.ok) {
          throw new Error(`Media request failed with status ${response.status}`)
        }
        const blob = await response.blob()
        const objectUrl = URL.createObjectURL(blob)
        setCurrentObjectUrl(objectUrl)
        if (audioRef.current) {
          audioRef.current.src = objectUrl
          await audioRef.current.play()
        }
      } catch (playbackError) {
        setError(playbackError instanceof Error ? playbackError.message : 'Failed to load media.')
        throw playbackError
      } finally {
        setIsLoading(false)
      }
    },
    [releaseObjectUrl],
  )

  const playerState = useMemo(
    () => ({
      audioRef,
      currentObjectUrl,
      isLoading,
      error,
      setAudioElement,
      playBlobUrl,
      releaseObjectUrl,
    }),
    [currentObjectUrl, error, isLoading, playBlobUrl, releaseObjectUrl, setAudioElement],
  )

  return playerState
}

