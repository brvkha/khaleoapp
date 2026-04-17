import { create } from 'zustand'
import type { User } from '../types'
import { getClaimsFromAccessToken, loginWithPassword, logoutWithRefreshToken, registerWithPassword } from '../services/authApi'
import { persistAuthSession, readAuthSession } from '../services/authSession'

type AuthState = {
  currentUser: User | null
  login: (identifier: string, password: string) => Promise<void>
  register: (username: string, email: string | null, password: string) => Promise<void>
  logout: () => Promise<void>
  bootstrap: () => void
  banUser: (username: string) => void
}

const initialUser = readAuthSession()?.currentUser ?? null

export const useAuthStore = create<AuthState>((set) => ({
  currentUser: initialUser,
  login: async (identifier, password) => {
    const loginResponse = await loginWithPassword(identifier, password)
    const claims = getClaimsFromAccessToken(loginResponse.accessToken)
    const user: User = {
      id: claims.userId,
      username: claims.username,
      email: claims.email,
      role: claims.role,
      verified: true,
      banned: false,
    }

    persistAuthSession({
      currentUser: user,
      accessToken: loginResponse.accessToken,
      refreshToken: loginResponse.refreshToken,
    })

    set({ currentUser: user })
  },
  register: async (username, email, password) => {
    await registerWithPassword(username, email, password)
  },
  logout: async () => {
    const session = readAuthSession()
    if (session?.refreshToken) {
      try {
        await logoutWithRefreshToken(session.refreshToken)
      } catch {
        // Local logout should still proceed if remote token revocation fails.
      }
    }

    persistAuthSession(null)
    set({ currentUser: null })
  },
  bootstrap: () => {
    set({ currentUser: readAuthSession()?.currentUser ?? null })
  },
  banUser: (username) => {
    set((state) => {
      if (!state.currentUser || state.currentUser.username !== username) {
        return state
      }
      const next = { ...state.currentUser, banned: true }
      const session = readAuthSession()
      if (session) {
        persistAuthSession({
          ...session,
          currentUser: next,
        })
      }
      return { currentUser: next }
    })
  },
}))
