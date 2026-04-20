import { requestJson } from './apiClient'
import type { BulkChunkApiResponse } from '../features/cards/types/bulkImportOrchestration'
import type { ParsedBulkCandidate } from '../features/cards/types/bulkImport'

export type BulkRequestFailure = {
  type: 'NETWORK' | 'HTTP'
  message: string
  status?: number
}

export async function bulkCreateCardsChunk(deckId: string, cards: ParsedBulkCandidate[]): Promise<BulkChunkApiResponse> {
  try {
    return await requestJson<BulkChunkApiResponse>(`/api/v1/decks/${deckId}/cards/bulk`, {
      method: 'POST',
      body: JSON.stringify({
        cards: cards.map((card) => ({
          line: card.line,
          frontContent: card.frontContent,
          backContent: card.backContent,
        })),
      }),
    })
  } catch (error) {
    const failure = classifyBulkError(error)
    throw new Error(`${failure.type}:${failure.message}`)
  }
}

export function classifyBulkError(error: unknown): BulkRequestFailure {
  if (error instanceof Error) {
    const statusMatch = error.message.match(/HTTP\s(\d{3})/)
    if (statusMatch) {
      return {
        type: 'HTTP',
        status: Number(statusMatch[1]),
        message: error.message,
      }
    }
    return {
      type: 'NETWORK',
      message: error.message,
    }
  }
  return {
    type: 'NETWORK',
    message: 'Unknown bulk request failure',
  }
}

