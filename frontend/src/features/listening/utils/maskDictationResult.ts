type MaskedDictationResult = {
  typedTokens: string[]
  expectedTokens: string[]
  revealedTokens: string[]
  maskedTokens: string[]
}

function stripToken(token: string): string {
  return token.replace(/[‚,.、。！：；）（，？„“‘’"?;:'\]\[}{!&()\-—+=\s…]/g, '').toLowerCase()
}

export function maskDictationResult(expectedText: string, answerText: string): MaskedDictationResult {
  const expectedTokens = expectedText.trim().split(/\s+/).filter(Boolean)
  const typedTokens = answerText.trim().split(/\s+/).filter(Boolean)
  const revealedTokens: string[] = []
  const maskedTokens: string[] = []

  const maxLength = expectedTokens.length
  let mismatchFound = false

  for (let index = 0; index < maxLength; index += 1) {
    const expectedToken = expectedTokens[index]
    const typedToken = typedTokens[index]

    if (mismatchFound) {
      maskedTokens.push('***')
      continue
    }

    if (!typedToken) {
      maskedTokens.push('***')
      mismatchFound = true
      continue
    }

    if (stripToken(expectedToken) === stripToken(typedToken)) {
      revealedTokens.push(expectedToken)
      maskedTokens.push(expectedToken)
      continue
    }

    maskedTokens.push('***')
    mismatchFound = true
  }

  return { typedTokens, expectedTokens, revealedTokens, maskedTokens }
}

