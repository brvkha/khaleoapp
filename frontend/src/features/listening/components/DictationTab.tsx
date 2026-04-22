import React, { useEffect } from 'react';
import { useDictationSession } from '../hooks/useDictationSession';
import type { SentenceWithProgress } from '../services/listeningApi';

/**
 * T045 - Dictation input panel with check/skip and prev/next controls
 */

interface DictationTabProps {
  sentences: SentenceWithProgress[];
  onProgressUpdate?: (percent: number) => void;
  onStateChange?: (state: {
    currentIndex: number;
    totalCount: number;
    progressPercent: number;
    completedCount: number;
    currentSentence: SentenceWithProgress | null;
  }) => void;
}

export const DictationTab: React.FC<DictationTabProps> = ({
  sentences,
  onProgressUpdate,
  onStateChange
}) => {
  const session = useDictationSession(sentences);

  useEffect(() => {
    onProgressUpdate?.(session.progressPercent);
  }, [session.progressPercent, onProgressUpdate]);

  useEffect(() => {
    onStateChange?.({
      currentIndex: session.currentIndex,
      totalCount: session.totalCount,
      progressPercent: session.progressPercent,
      completedCount: session.completedCount,
      currentSentence: session.currentSentence ?? null,
    });
  }, [
    onStateChange,
    session.currentIndex,
    session.currentSentence,
    session.totalCount,
    session.progressPercent,
    session.completedCount,
  ]);

  const current = session.currentSentence;
  if (!current) {
    return (
      <div className="p-4 text-center text-gray-500">
        No sentences available
      </div>
    );
  }

  return (
    <div className="space-y-4 p-6">
      {/* Sentence Counter & Translation */}
      <div className="border-b pb-4">
        <div className="flex justify-between items-center mb-2">
          <h3 className="font-semibold">
            Sentence {session.currentIndex + 1} of {session.totalCount}
          </h3>
          <span className="text-sm text-gray-600">
            {session.completedCount} completed
          </span>
        </div>

        {current.translation && (
          <div className="text-sm italic text-gray-700 mt-2">
            Translation: {current.translation}
          </div>
        )}
      </div>

      {/* Dictation Input Area */}
      <div>
        <label className="block text-sm font-medium mb-2">
          Type what you hear:
        </label>
        <textarea
          value={session.currentAnswer}
          onChange={(e) => session.setCurrentAnswer(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
              session.checkAnswer();
            } else if (e.key === 'Escape') {
              session.skipSentence();
            }
          }}
          className="w-full h-24 p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Type here... (Enter to check, Esc to skip)"
          disabled={session.isSkipped}
        />
      </div>

      {/* Check Result Display */}
      {session.checkResult && (
        <div
          className={`p-3 rounded-lg ${
            session.checkResult.correct
              ? 'bg-green-50 border border-green-300'
              : 'bg-red-50 border border-red-300'
          }`}
        >
          <p className="text-sm font-medium mb-1">
            {session.checkResult.correct ? '✓ Correct!' : '✗ Not quite right'}
          </p>
          <p className="text-sm text-gray-700">
            Your answer: <code className="font-mono">{session.checkResult.maskedDisplay}</code>
          </p>
        </div>
      )}

      {/* Skip Indicator */}
      {session.isSkipped && !session.checkResult && (
        <div className="p-3 rounded-lg bg-yellow-50 border border-yellow-300">
          <p className="text-sm text-gray-700">
            Skipped. Answer: <code className="font-mono">{current.transcript}</code>
          </p>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex gap-3">
        <button
          onClick={() => session.checkAnswer()}
          disabled={!session.currentAnswer.trim() || session.isSkipped}
          className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed"
        >
          Check (Enter)
        </button>
        <button
          onClick={() => session.skipSentence()}
          disabled={session.isSkipped}
          className="flex-1 px-4 py-2 bg-yellow-500 text-white rounded-lg hover:bg-yellow-600 disabled:bg-gray-300 disabled:cursor-not-allowed"
        >
          Skip (Esc)
        </button>
      </div>

      {/* Navigation */}
      <div className="flex gap-3 pt-4 border-t">
        <button
          onClick={() => session.goPrevious()}
          disabled={!session.canGoPrevious}
          className="flex-1 px-3 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          ← Previous
        </button>
        <button
          onClick={() => session.goNext()}
          disabled={!session.canGoNext}
          className="flex-1 px-3 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Next →
        </button>
      </div>
    </div>
  );
};
