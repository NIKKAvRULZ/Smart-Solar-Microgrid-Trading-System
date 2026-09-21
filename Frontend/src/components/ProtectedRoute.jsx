// -----------------------------------------------------------------------------
// File: ProtectedRoute.jsx
// Purpose: Route guard that redirects to /login when unauthenticated, and
// optionally restricts a route to specific roles (e.g. Backoffice only).
// -----------------------------------------------------------------------------
import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  // Inline comment: if allowedRoles is provided, block access for roles
  // outside that list (e.g. Grid Operators viewing User Management).
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />
  }

  return children
}
