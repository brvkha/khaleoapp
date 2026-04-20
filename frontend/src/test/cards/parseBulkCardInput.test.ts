import { describe, expect, it } from 'vitest'

import { parseBulkCardInput } from '../../features/cards/utils/parseBulkCardInput'

describe('parseBulkCardInput', () => {
  it('parses tab-separated rows and preserves empty middle back columns', () => {
    const raw = 'Apple\tQua tao\nBanana\tQua chuoi\t\tNhiet doi\r\nCar\tXe hoi\tPhuong tien'

    const result = parseBulkCardInput(raw, 'tab')

    expect(result.candidates).toEqual([
      { line: 1, frontContent: 'Apple', backContent: 'Qua tao' },
      { line: 2, frontContent: 'Banana', backContent: 'Qua chuoi\n\nNhiet doi' },
      { line: 3, frontContent: 'Car', backContent: 'Xe hoi\nPhuong tien' },
    ])
    expect(result.rejected).toEqual([])
  })

  it('rejects rows with too few columns or missing front/back content', () => {
    const raw = 'OnlyFront\n\tBack only\nFront only\t\nGood\tBack'

    const result = parseBulkCardInput(raw, 'tab')

    expect(result.candidates).toEqual([{ line: 4, frontContent: 'Good', backContent: 'Back' }])
    expect(result.rejected).toEqual([
      expect.objectContaining({ line: 1, code: 'ROW_TOO_FEW_COLUMNS' }),
      expect.objectContaining({ line: 2, code: 'FRONT_REQUIRED' }),
      expect.objectContaining({ line: 3, code: 'BACK_REQUIRED' }),
    ])
  })
})

