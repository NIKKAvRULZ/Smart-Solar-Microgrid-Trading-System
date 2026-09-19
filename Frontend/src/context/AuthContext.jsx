// -----------------------------------------------------------------------------
// File: AuthContext.jsx
// Purpose: Global auth state (current user, role, JWT) shared across the
// app via React Context. Persists to localStorage so a page refresh keeps
// the session alive until the token expires.
// -----------------------------------------------------------------------------
import React, { createContext, useContext, useState } from 'react'
import { login as loginApi } from '../api/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('ssm_user')
    return stored ? JSON.parse(stored) : null
  })

  // Inline comment: calls the /auth/login endpoint, stores the JWT + user
  // profile, and updates context state so protected routes unlock.
  async function login(username, password) {
    const response = await loginApi(username, password)
    const { token, username: uname, fullName, role } = response.data
    localStorage.setItem('ssm_token', token)
    const userInfo = { username: uname, fullName, role }
    localStorage.setItem('ssm_user', JSON.stringify(userInfo))
    setUser(userInfo)
    return userInfo
  }

  function logout() {
    localStorage.removeItem('ssm_token')
    localStorage.removeItem('ssm_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
