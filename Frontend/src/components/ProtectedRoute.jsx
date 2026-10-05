// -----------------------------------------------------------------------------
// File: ProtectedRoute.jsx
// Purpose: Route guard that redirects to /login when unauthenticated, and
// optionally restricts a route to specific roles (e.g. Backoffice only).
// -----------------------------------------------------------------------------
import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { canAccessRoute } from '../utils/auth'

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (!canAccessRoute(user, allowedRoles)) {
    return <Navigate to="/dashboard" replace />
  }

  return children
}
