import { useState, useCallback } from 'react';
import * as listeningApi from '../services/listeningApi';

/**
 * T043 - Hook for managing dictation session state
 * Tracks current sentence, answer, check results, skip, and navigation
 */

export interface DictationCheckResult {
  correct: boolean;
  maskedDisplay: string; // Shows green correct part + *** for errors
}

export function useDictationSession(
  sentences: listeningApi.SentenceWithProgress[]
) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [currentAnswer, setCurrentAnswer] = useState('');
  const [checkResult, setCheckResult] = useState<DictationCheckResult | null>(null);
  const [isSkipped, setIsSkipped] = useState(false);
  const [completedCount, setCompletedCount] = useState(() => sentences.filter((s) => s.isCompleted).length);

  const resetCurrentSentence = useCallback((nextIndex: number) => {
    setCurrentIndex(nextIndex);
    setCurrentAnswer('');
    setCheckResult(null);
    setIsSkipped(sentences[nextIndex]?.isCompleted ?? false);
  }, [sentences]);

  const getCurrentSentence = useCallback(() => {
    return sentences[currentIndex];
  }, [sentences, currentIndex]);

  const checkAnswer = useCallback(async () => {
    const sentence = getCurrentSentence();
    if (!sentence) return;

    const normalized = normalizeDictation(currentAnswer);
    const target = normalizeDictation(sentence.transcript);
    const aliasMatches = parseAcceptedAnswers(sentence.aliasesJson).some(
      (candidate) => normalizeDictation(candidate) === normalized,
    );
    const correct = normalized === target || aliasMatches;

    // Generate masked display (left-to-right word match)
    const maskedDisplay = generateMaskDisplay(currentAnswer, sentence.transcript, correct);
    
    setCheckResult({
      correct,
      maskedDisplay
    });

    if (correct) {
      // Mark as completed
      await listeningApi.markSentenceComplete(sentence.id, 'correct_check');
      setCompletedCount(prev => prev + 1);
      setIsSkipped(true);
    }
  }, [currentAnswer, getCurrentSentence]);

  const skipSentence = useCallback(async () => {
    const sentence = getCurrentSentence();
    if (!sentence) return;

    await listeningApi.markSentenceComplete(sentence.id, 'skip');
    setIsSkipped(true);
    setCompletedCount(prev => prev + 1);
  }, [getCurrentSentence]);

  const goNext = useCallback(() => {
    if (currentIndex < sentences.length - 1) {
      resetCurrentSentence(currentIndex + 1);
    }
  }, [currentIndex, resetCurrentSentence, sentences.length]);

  const goPrevious = useCallback(() => {
    if (currentIndex > 0) {
      resetCurrentSentence(currentIndex - 1);
    }
  }, [currentIndex, resetCurrentSentence]);

  const canGoNext = currentIndex < sentences.length - 1;
  const canGoPrevious = currentIndex > 0;
  const progressPercent = sentences.length === 0 ? 0 :
    Math.round((completedCount / sentences.length) * 100);

  return {
    // State
    currentIndex,
    currentAnswer,
    setCurrentAnswer,
    checkResult,
    isSkipped,
    currentSentence: getCurrentSentence(),
    // Actions
    checkAnswer,
    skipSentence,
    goNext,
    goPrevious,
    // Navigation
    canGoNext,
    canGoPrevious,
    // Progress
    completedCount,
    totalCount: sentences.length,
    progressPercent
  };
}

// ========== HELPERS ==========

export function normalizeDictation(text: string): string {
  if (!text) return '';
  return text
    .normalize('NFKC')
    .toLowerCase()
    .replace(/[\p{P}\p{S}]/gu, '')
    .replace(/\s+/g, ' ')
    .trim();
}

export function generateMaskDisplay(
  userInput: string,
  targetTranscript: string,
  isCorrect: boolean
): string {
  if (isCorrect) {
    return userInput; // Show full input if correct
  }

  // Token-by-token comparison (preserving punctuation)
  const userTokens = userInput.split(/\s+/);
  const targetTokens = targetTranscript.split(/\s+/);

  const result: string[] = [];
  for (let i = 0; i < targetTokens.length; i++) {
    if (i < userTokens.length) {
      const userToken = userTokens[i].toLowerCase().replace(/[\p{P}\p{S}]/gu, '');
      const targetToken = targetTokens[i].toLowerCase().replace(/[\p{P}\p{S}]/gu, '');

      if (userToken === targetToken) {
        result.push(userTokens[i]);
      } else {
        // First mismatch: mask this and all remaining
        result.push('***');
        break;
      }
    } else {
      // User hasn't typed this far
      result.push('***');
      break;
    }
  }

  return result.join(' ');
}

function parseAcceptedAnswers(aliasesJson: string | null): string[] {
  if (!aliasesJson) {
    return [];
  }

  try {
    const parsed = JSON.parse(aliasesJson);
    if (Array.isArray(parsed)) {
      return parsed
        .map((item) => {
          if (typeof item === 'string') {
            return item;
          }
          if (item && typeof item === 'object') {
            if (typeof item.text === 'string') {
              return item.text;
            }
            if (typeof item.value === 'string') {
              return item.value;
            }
          }
          return '';
        })
        .filter(Boolean);
    }

    if (parsed && typeof parsed === 'object') {
      return Object.values(parsed)
        .flatMap((value) => (typeof value === 'string' ? [value] : []));
    }
  } catch {
    return [];
  }

  return [];
}

