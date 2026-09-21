import React, { useEffect, useState } from 'react'
import Modal from './Modal'
import { IconSearch, IconSpinner, IconAlert } from './Icons'

export function Spinner({ size, label }) {
  return (
    <span className="inline-spinner">
      <IconSpinner size={size} />
      {label && <span style={{ marginLeft: 8 }}>{label}</span>}
    </span>
  )
}

export function PageHeader({ title, subtitle, actions, className = '' }) {
  return (
    <div className={`page-header ${className}`}>
      <div>
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {actions && <div className="page-header-actions">{actions}</div>}
    </div>
  )
}

function useCountUp(target, duration = 900) {
  const [value, setValue] = useState(0)

  useEffect(() => {
    if (typeof target !== 'number') {
      setValue(target)
      return
    }
    let raf
    const start = performance.now()
    const tick = (now) => {
      const t = Math.min((now - start) / duration, 1)
      const eased = 1 - Math.pow(1 - t, 3)
      setValue(Math.round(target * eased))
      if (t < 1) raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(raf)
  }, [target, duration])

  return value
}

export function StatCard({ icon, label, value, sub, accent = 'navy' }) {
  const display = useCountUp(value)
  return (
    <div className={`stat-card accent-${accent}`}>
      <div className="stat-top">
        <span className="stat-icon">{icon}</span>
        {sub && <span className="stat-sub">{sub}</span>}
      </div>
      <div className="stat-value">{display}</div>
      <div className="stat-label">{label}</div>
    </div>
  )
}

export function Badge({ children, tone = 'neutral' }) {
  return <span className={`badge-status badge-${tone}`}>{children}</span>
}

export function EmptyState({ icon, title, hint }) {
  return (
    <div className="empty-state">
      {icon && <div className="empty-icon">{icon}</div>}
      <div className="empty-title">{title}</div>
      {hint && <div className="empty-hint">{hint}</div>}
    </div>
  )
}

export function SearchInput({ value, onChange, placeholder }) {
  return (
    <div className="search-box">
      <IconSearch size={16} />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder || 'Search...'}
      />
    </div>
  )
}

export function ConfirmDialog({ open, title, message, confirmText = 'Confirm', tone = 'danger', onConfirm, onCancel }) {
  const [busy, setBusy] = useState(false)

  async function handleConfirm() {
    setBusy(true)
    try {
      await onConfirm()
    } finally {
      setBusy(false)
      onCancel()
    }
  }

  return (
    <Modal
      open={open}
      onClose={onCancel}
      title={title}
      size="sm"
      footer={
        <>
          <button className="btn btn-ghost" onClick={onCancel} disabled={busy}>
            Cancel
          </button>
          <button className={`btn ${tone === 'danger' ? 'btn-danger' : 'btn-solar'}`} onClick={handleConfirm} disabled={busy}>
            {busy ? <Spinner size={15} /> : confirmText}
          </button>
        </>
      }
    >
      <div className="confirm-body">
        <span className="confirm-icon">
          <IconAlert size={22} />
        </span>
        <p>{message}</p>
      </div>
    </Modal>
  )
}