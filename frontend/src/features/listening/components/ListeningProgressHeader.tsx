import React from 'react';

/**
 * T046 - Progress header with sentence counter and progress bar
 */

interface ListeningProgressHeaderProps {
  currentIndex: number;
  totalCount: number;
  progressPercent: number;
  lessonName: string;
}

export const ListeningProgressHeader: React.FC<ListeningProgressHeaderProps> = ({
  currentIndex,
  totalCount,
  progressPercent,
  lessonName
}) => {
  return (
    <div className="bg-white border-b p-4">
      {/* Lesson Title */}
      <h1 className="text-2xl font-bold mb-4">{lessonName}</h1>

      {/* Sentence Counter */}
      <div className="mb-3">
        <div className="flex justify-between mb-1">
          <span className="text-sm font-medium text-gray-700">
            Sentence {currentIndex + 1} of {totalCount}
          </span>
          <span className="text-sm font-medium text-gray-700">
            {progressPercent}%
          </span>
        </div>

        {/* Progress Bar */}
        <div className="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
          <div
            className="h-full bg-green-500 transition-all duration-300 ease-out"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>

      {/* Info Text */}
      <p className="text-xs text-gray-600">
        Complete sentences by checking your answer or skipping
      </p>
    </div>
  );
};
