import React, { createContext, useContext, useState, useCallback, useRef } from 'react'
import { IconCheckCircle, IconAlert, IconX, IconCheck } from './Icons'

const ToastContext = createContext(null)

let idCounter = 0

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const push = useCallback((type, title, description, timeout = 4000) => {
    const id = ++idCounter
    setToasts((prev) => [...prev, { id, type, title, description }])
    if (timeout > 0) {
      setTimeout(() => dismiss(id), timeout)
    }
    return id
  }, [dismiss])

  const toast = useRef({
    success: (title, description) => push('success', title, description),
    error: (title, description) => push('error', title, description),
    info: (title, description) => push('info', title, description),
  }).current

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="toast-stack" role="status" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast-item toast-${t.type}`}>
            <span className="toast-icon">
              {t.type === 'success' && <IconCheckCircle size={18} />}
              {t.type === 'error' && <IconAlert size={18} />}
              {t.type === 'info' && <IconCheck size={18} />}
            </span>
            <div className="toast-body">
              <div className="toast-title">{t.title}</div>
              {t.description && <div className="toast-desc">{t.description}</div>}
            </div>
            <button className="toast-close" onClick={() => dismiss(t.id)} aria-label="Dismiss">
              <IconX size={14} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast must be used within a ToastProvider')
  return ctx
}