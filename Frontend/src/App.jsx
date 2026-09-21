import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Prosumers from './pages/Prosumers'
import Nodes from './pages/Nodes'
import Reservations from './pages/Reservations'
import Users from './pages/Users'

function AuthenticatedLayout({ children, flush }) {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="main-wrap">
        <main className={`main-content ${flush ? 'main-content-flush' : ''}`}>{children}</main>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <AuthenticatedLayout flush>
              <Dashboard />
            </AuthenticatedLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/prosumers"
        element={
          <ProtectedRoute>
            <AuthenticatedLayout>
              <Prosumers />
            </AuthenticatedLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/nodes"
        element={
          <ProtectedRoute>
            <AuthenticatedLayout>
              <Nodes />
            </AuthenticatedLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/reservations"
        element={
          <ProtectedRoute allowedRoles={['GridOperator']}>
            <AuthenticatedLayout>
              <Reservations />
            </AuthenticatedLayout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/users"
        element={
          <ProtectedRoute allowedRoles={['Backoffice']}>
            <AuthenticatedLayout>
              <Users />
            </AuthenticatedLayout>
          </ProtectedRoute>
        }
      />

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}