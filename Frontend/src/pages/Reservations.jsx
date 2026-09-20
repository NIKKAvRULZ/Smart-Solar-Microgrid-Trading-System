import React, { useEffect, useMemo, useState } from 'react'
import {
  CalendarDays,
  Clock3,
  MapPin,
  Eye,
  Pencil,
  Check,
  Trash,
  Search,
  ListFilter,
  Plus,
  QrCode,
  User,
} from 'lucide-react'
import {
  getReservations,
  getProsumers,
  getNodes,
  createReservation,
  updateReservation,
  cancelReservation,
  approveReservation,
  completeReservation,
} from '../api/api'
import { getErrorMessage } from '../utils/errors'
import Modal from '../components/ui/Modal'
import { EmptyState, Spinner, ConfirmDialog } from '../components/ui/Widgets'
import { useToast } from '../components/ui/Toast'

const STATUS_CLASS = {
  Pending: 'res-pill-amber',
  Approved: 'res-pill-green',
  Completed: 'res-pill-blue',
  Cancelled: 'res-pill-red',
}

const STATUS_BY_INDEX = { 0: 'Pending', 1: 'Approved', 2: 'Completed', 3: 'Cancelled' }

const STATUS_ORDER = ['Pending', 'Approved', 'Completed', 'Cancelled']

function normalizeStatus(status) {
  if (typeof status === 'string') return status
  return STATUS_BY_INDEX[status] || String(status ?? '')
}

function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  return d.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })
}

function formatTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' })
}

function timeFromNow(value) {
  if (!value) return ''
  const diffMs = new Date(value).getTime() - Date.now()
  const mins = Math.round(diffMs / 60000)
  if (mins <= 0) return 'now'
  if (mins < 60) return `in ${mins}m`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `in ${hrs}h ${mins % 60}m`
  const days = Math.floor(hrs / 24)
  return `in ${days}d ${hrs % 24}h`
}

function formatDuration(mins) {
  if (!mins) return '—'
  const h = Math.floor(mins / 60)
  const m = mins % 60
  if (h && m) return `${h}h ${m}m`
  if (h) return `${h}h`
  return `${m}m`
}

const EMPTY_FORM = { prosumerNic: '', nodeId: '', scheduledDateTime: '', durationMinutes: 60 }

export default function Reservations() {
  const toast = useToast()
  const [reservations, setReservations] = useState([])
  const [prosumers, setProsumers] = useState([])
  const [nodes, setNodes] = useState([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('All')
  const [filterOpen, setFilterOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [createOpen, setCreateOpen] = useState(false)
  const [editReservation, setEditReservation] = useState(null)
  const [detailReservation, setDetailReservation] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [editForm, setEditForm] = useState({ scheduledDateTime: '', durationMinutes: 60 })
  const [confirmCancel, setConfirmCancel] = useState(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadAll()
  }, [])

  async function loadAll() {
    setLoading(true)
    try {
      const [resRes, prosumerRes, nodeRes] = await Promise.all([getReservations(), getProsumers(), getNodes()])
      setReservations(resRes.data)
      setProsumers(prosumerRes.data)
      setNodes(nodeRes.data)
    } catch (err) {
      toast.error('Could not load reservations', getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const activeProsumers = useMemo(() => prosumers.filter((p) => p.isActive), [prosumers])
  const activeNodes = useMemo(() => nodes.filter((n) => n.isActive), [nodes])

  const prosumerMap = useMemo(() => {
    const m = {}
    prosumers.forEach((p) => (m[p.nic] = p))
    return m
  }, [prosumers])

  const nodeMap = useMemo(() => {
    const m = {}
    nodes.forEach((n) => (m[n.id] = n))
    return m
  }, [nodes])

  function prosumerOf(r) {
    return r ? prosumerMap[r.prosumerNic] : undefined
  }

  function nodeOf(r) {
    return r ? nodeMap[r.nodeId] : undefined
  }

  function nodeName(nodeId) {
    return nodeMap[nodeId]?.name || nodeId
  }

  function openCreate() {
    setForm(EMPTY_FORM)
    setCreateOpen(true)
  }

  function openEdit(r) {
    setEditReservation(r)
    setEditForm({
      scheduledDateTime: new Date(r.scheduledDateTime).toISOString().slice(0, 16),
      durationMinutes: r.durationMinutes,
    })
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await createReservation({
        ...form,
        scheduledDateTime: new Date(form.scheduledDateTime).toISOString(),
        durationMinutes: Number(form.durationMinutes),
      })
      toast.success('Reservation created', 'The energy slot has been booked.')
      setCreateOpen(false)
      await loadAll()
    } catch (err) {
      toast.error('Could not create reservation', getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleUpdate(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await updateReservation(editReservation.id, {
        scheduledDateTime: new Date(editForm.scheduledDateTime).toISOString(),
        durationMinutes: Number(editForm.durationMinutes),
      })
      toast.success('Reservation updated', 'The booking was rescheduled.')
      setEditReservation(null)
      await loadAll()
    } catch (err) {
      toast.error('Could not update reservation', getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleAction(actionFn, r, successMsg) {
    try {
      await actionFn(r.id)
      toast.success(successMsg)
      await loadAll()
    } catch (err) {
      toast.error('Action failed', getErrorMessage(err))
    }
  }

  const maxDateTime = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16)
  const minDateTime = new Date(Date.now() + 5 * 60 * 1000).toISOString().slice(0, 16)

  const counts = useMemo(() => {
    const c = { All: reservations.length }
    STATUS_ORDER.forEach((s) => (c[s] = reservations.filter((r) => normalizeStatus(r.status) === s).length))
    return c
  }, [reservations])

  const q = search.trim().toLowerCase()
  const filtered = useMemo(
    () =>
      reservations.filter((r) => {
        const status = normalizeStatus(r.status)
        if (statusFilter !== 'All' && status !== statusFilter) return false
        const prosumer = prosumerOf(r)
        const node = nodeOf(r)
        const haystack = [
          r.prosumerNic,
          prosumer?.fullName,
          prosumer?.email,
          node?.name,
          status,
          formatDate(r.scheduledDateTime),
          r.qrCode,
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()
        return !q || haystack.includes(q)
      }),
    [reservations, statusFilter, q, prosumers, nodes],
  )

  const nodeOptions = activeNodes.map((n) => (
    <option key={n.id} value={n.id}>
      {n.name} ({n.availableBatterySlots} slots free)
    </option>
  ))
  const prosumerOptions = activeProsumers.map((p) => (
    <option key={p.nic} value={p.nic}>
      {p.fullName} ({p.nic})
    </option>
  ))

  function DetailRow({ label, children }) {
    return (
      <li>
        <div className="dt">
          <div className="dl">{label}</div>
        </div>
        <div className="dv">{children}</div>
      </li>
    )
  }

  return (
    <div className="res-dash">
      <div className="res-container">
        <div className="res-header">
          <div className="res-title">
            <h1>Reservations</h1>
            <p>Power trading bookings across all microgrid nodes.</p>
          </div>
          <button className="btn btn-solar" onClick={openCreate}>
            <Plus size={16} /> New Reservation
          </button>
        </div>

        <div className="res-panel">
          <div className="res-filters">
            {['All', ...STATUS_ORDER].map((s) => (
              <button
                key={s}
                className={`res-filter res-filter-${s.toLowerCase()}${statusFilter === s ? ' selected' : ''}`}
                onClick={() => setStatusFilter(s)}
              >
                {s}
                <span className="res-badge">{counts[s] || 0}</span>
              </button>
            ))}
          </div>

        <div className="res-toolbar">
          <div className="res-search">
            <Search size={17} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by prosumer, node, stat"
            />
          </div>
          <span className="res-count">
            {filtered.length} of {reservations.length}
          </span>
          <div className="res-filter-wrap">
            <button
              className={`res-filter-btn${filterOpen ? ' is-on' : ''}`}
              title="Filter by status"
              onClick={() => setFilterOpen(!filterOpen)}
            >
              <ListFilter size={16} />
            </button>
            {filterOpen && (
              <div className="res-filter-menu">
                {['All', ...STATUS_ORDER].map((s) => (
                  <button
                    key={s}
                    className={statusFilter === s ? 'active' : ''}
                    onClick={() => {
                      setStatusFilter(s)
                      setFilterOpen(false)
                    }}
                  >
                    <span>{s}</span>
                    {statusFilter === s && <Check size={13} />}
                  </button>
                ))}
              </div>
            )}
          </div>
          <button
            className="res-clear"
            onClick={() => {
              setStatusFilter('All')
              setSearch('')
            }}
          >
            Clear filters
          </button>
        </div>

        {loading ? (
          <div className="loading-block">
            <Spinner size={20} /> Loading reservations...
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<CalendarDays size={26} />}
            title={reservations.length === 0 ? 'No reservations yet' : 'No matching reservations'}
            hint="Create a booking on a prosumer's behalf, or wait for bookings from the mobile app."
          />
        ) : (
          <div className="res-table-wrap">
            <table className="res-table">
              <thead>
                <tr>
                  <th>Prosumer</th>
                  <th>Microgrid node</th>
                  <th>Scheduled</th>
                  <th>Duration</th>
                  <th>Status</th>
                  <th style={{ width: 168 }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((r) => {
                  const status = normalizeStatus(r.status)
                  const prosumer = prosumerOf(r)
                  const node = nodeOf(r)
                  return (
                    <tr key={r.id}>
                      <td data-label="Prosumer">
                        <div className="res-strong">{prosumer?.fullName || r.prosumerNic}</div>
                        <div className="res-muted">
                          NIC · {r.prosumerNic}
                          {prosumer?.phone ? ` · ${prosumer.phone}` : ''}
                        </div>
                      </td>
                      <td data-label="Microgrid node">
                        <div className="res-strong res-node">{nodeName(r.nodeId)}</div>
                        {node && (
                          <div className="res-muted res-coords">
                            <MapPin size={12} />
                            {node.latitude?.toFixed(3)}, {node.longitude?.toFixed(3)}
                          </div>
                        )}
                      </td>
                      <td data-label="Scheduled">
                        <div className="res-strong res-date">
                          <CalendarDays size={14} /> {formatDate(r.scheduledDateTime)}
                        </div>
                        <div className="res-muted res-time">
                          <Clock3 size={13} /> {formatTime(r.scheduledDateTime)} · {timeFromNow(r.scheduledDateTime)}
                        </div>
                      </td>
                      <td data-label="Duration">
                        <span className="res-duration">{formatDuration(r.durationMinutes)}</span>
                      </td>
                      <td data-label="Status">
                        <span className={`res-pill ${STATUS_CLASS[status] || 'res-pill-blue'}`}>
                          <span className="dot" /> {status}
                        </span>
                        {r.qrCode && status === 'Approved' && (
                          <div className="res-muted res-dispatch">
                            <QrCode size={11} /> {r.qrCode}
                          </div>
                        )}
                      </td>
                      <td data-label="Actions">
                        <div className="res-actions">
                          <button className="res-action-btn" title="View details" onClick={() => setDetailReservation(r)}>
                            <Eye size={15} />
                          </button>
                          {(status === 'Pending' || status === 'Approved') && (
                            <button className="res-action-btn" title="Reschedule" onClick={() => openEdit(r)}>
                              <Pencil size={15} />
                            </button>
                          )}
                          {status === 'Pending' && (
                            <button
                              className="res-action-btn success"
                              title="Approve"
                              onClick={() => handleAction(approveReservation, r, 'Reservation approved.')}
                            >
                              <Check size={15} />
                            </button>
                          )}
                          {status === 'Approved' && (
                            <button
                              className="res-action-btn success"
                              title="Complete"
                              onClick={() => handleAction(completeReservation, r, 'Marked as completed.')}
                            >
                              <Check size={15} />
                            </button>
                          )}
                          {(status === 'Pending' || status === 'Approved') && (
                            <button className="res-action-btn danger" title="Cancel" onClick={() => setConfirmCancel(r)}>
                              <Trash size={15} />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ---- Details modal ---- */}
      <Modal
        open={!!detailReservation}
        onClose={() => setDetailReservation(null)}
        size="md"
        title="Reservation details"
        subtitle={detailReservation ? `Booking #${detailReservation.id}` : undefined}
        footer={
          detailReservation &&
          (() => {
            const r = detailReservation
            const status = normalizeStatus(r.status)
            return (
              <>
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => setDetailReservation(null)}
                >
                  Close
                </button>
                {(status === 'Pending' || status === 'Approved') && (
                  <>
                    <button
                      type="button"
                      className="btn btn-outline-grid"
                      onClick={() => {
                        openEdit(r)
                        setDetailReservation(null)
                      }}
                    >
                      <Pencil size={15} /> Reschedule
                    </button>
                    <button
                      type="button"
                      className="btn btn-danger"
                      onClick={() => {
                        setConfirmCancel(r)
                        setDetailReservation(null)
                      }}
                    >
                      <Trash size={15} /> Cancel
                    </button>
                  </>
                )}
              </>
            )
          })()
        }
      >
        {detailReservation &&
          (() => {
            const r = detailReservation
            const status = normalizeStatus(r.status)
            const prosumer = prosumerOf(r)
            const node = nodeOf(r)
            return (
              <>
                <div className="mb-3">
                  <span className={`res-pill res-pill-lg ${STATUS_CLASS[status] || 'res-pill-blue'}`}>
                    <span className="dot" /> {status}
                  </span>
                </div>

                {r.qrCode && (
                  <div className="mb-3">
                    <div className="qr-token">
                      <QrCode size={16} /> {r.qrCode}
                    </div>
                    <div style={{ fontSize: 11.5, color: 'var(--ink-faint)', marginTop: 5 }}>
                      Share this dispatch code with the grid operator to complete the energy transfer.
                    </div>
                  </div>
                )}

                <ul className="detail-list">
                  <DetailRow label="Prosumer">
                    <div className="cell-sub">
                      <User size={14} style={{ color: 'var(--ink-faint)' }} />
                      <span className="cell-strong">{prosumer?.fullName || 'Unknown prosumer'}</span>
                    </div>
                    <div className="cell-muted" style={{ fontSize: 12.5 }}>
                      NIC · {r.prosumerNic}
                      {prosumer?.email ? `  ·  ${prosumer.email}` : ''}
                      {prosumer?.phone ? `  ·  ${prosumer.phone}` : ''}
                    </div>
                    {prosumer?.address && (
                      <div className="cell-muted" style={{ fontSize: 12.5 }}>
                        {prosumer.address}
                      </div>
                    )}
                    <div className="cell-muted" style={{ fontSize: 12 }}>
                      {prosumer?.isActive ? 'Account active' : 'Account deactivated'}
                    </div>
                  </DetailRow>

                  <DetailRow label="Microgrid node">
                    <div className="cell-strong">{node?.name || r.nodeId}</div>
                    {node && (
                      <div className="cell-muted" style={{ fontSize: 12.5 }}>
                        <MapPin size={12} style={{ verticalAlign: '-1px' }} /> {node.latitude}, {node.longitude} ·{' '}
                        {node.capacityKWh} kWh · {node.availableBatterySlots}/{node.totalBatterySlots} slots
                      </div>
                    )}
                  </DetailRow>

                  <DetailRow label="Scheduled">
                    <div className="cell-sub">
                      <CalendarDays size={14} style={{ color: 'var(--ink-faint)' }} />
                      <span style={{ fontSize: 14, fontWeight: 600 }}>
                        {formatDate(r.scheduledDateTime)} · {formatTime(r.scheduledDateTime)}
                      </span>
                      <span className="cell-muted" style={{ fontSize: 12.5 }}>
                        ({timeFromNow(r.scheduledDateTime)})
                      </span>
                    </div>
                  </DetailRow>

                  <DetailRow label="Duration">
                    <span style={{ fontSize: 14 }}>{formatDuration(r.durationMinutes)} ({r.durationMinutes} minutes)</span>
                  </DetailRow>

                  <DetailRow label="History">
                    <div className="cell-muted" style={{ fontSize: 12.5 }}>
                      Created {new Date(r.createdAt).toLocaleString()}
                      {r.updatedAt ? `  ·  last updated ${new Date(r.updatedAt).toLocaleString()}` : ''}
                    </div>
                  </DetailRow>
                </ul>
              </>
            )
          })()}
      </Modal>

      {/* ---- Create modal ---- */}
      <Modal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        title="Create reservation"
        subtitle="Must be scheduled within the next 7 days."
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setCreateOpen(false)} disabled={saving}>
              Cancel
            </button>
            <button type="submit" form="create-form" className="btn btn-solar" disabled={saving}>
              {saving ? <Spinner size={15} /> : 'Create reservation'}
            </button>
          </>
        }
      >
        <form id="create-form" onSubmit={handleCreate}>
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label-sm">Prosumer (NIC)</label>
              <select
                className="form-select"
                value={form.prosumerNic}
                onChange={(e) => setForm({ ...form, prosumerNic: e.target.value })}
                required
              >
                <option value="">Select prosumer...</option>
                {prosumerOptions}
              </select>
            </div>
            <div className="col-md-6">
              <label className="form-label-sm">Microgrid node</label>
              <select
                className="form-select"
                value={form.nodeId}
                onChange={(e) => setForm({ ...form, nodeId: e.target.value })}
                required
              >
                <option value="">Select node...</option>
                {nodeOptions}
              </select>
            </div>
            <div className="col-md-7">
              <label className="form-label-sm">Date &amp; time</label>
              <input
                type="datetime-local"
                className="form-control"
                min={minDateTime}
                max={maxDateTime}
                value={form.scheduledDateTime}
                onChange={(e) => setForm({ ...form, scheduledDateTime: e.target.value })}
                required
              />
            </div>
            <div className="col-md-5">
              <label className="form-label-sm">Duration (min)</label>
              <input
                type="number"
                className="form-control"
                value={form.durationMinutes}
                onChange={(e) => setForm({ ...form, durationMinutes: e.target.value })}
                min="15"
                step="15"
                required
              />
            </div>
          </div>
        </form>
      </Modal>

      {/* ---- Reschedule modal ---- */}
      <Modal
        open={!!editReservation}
        onClose={() => setEditReservation(null)}
        title="Reschedule reservation"
        subtitle={
          editReservation
            ? `Booking for ${prosumerOf(editReservation)?.fullName || editReservation.prosumerNic} · ${nodeName(editReservation.nodeId)}`
            : undefined
        }
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setEditReservation(null)} disabled={saving}>
              Cancel
            </button>
            <button type="submit" form="edit-form" className="btn btn-solar" disabled={saving}>
              {saving ? <Spinner size={15} /> : 'Save changes'}
            </button>
          </>
        }
      >
        <form id="edit-form" onSubmit={handleUpdate}>
          <div className="row g-3">
            <div className="col-md-7">
              <label className="form-label-sm">New date &amp; time</label>
              <input
                type="datetime-local"
                className="form-control"
                min={minDateTime}
                max={maxDateTime}
                value={editForm.scheduledDateTime}
                onChange={(e) => setEditForm({ ...editForm, scheduledDateTime: e.target.value })}
                required
              />
            </div>
            <div className="col-md-5">
              <label className="form-label-sm">Duration (min)</label>
              <input
                type="number"
                className="form-control"
                value={editForm.durationMinutes}
                onChange={(e) => setEditForm({ ...editForm, durationMinutes: e.target.value })}
                min="15"
                step="15"
                required
              />
            </div>
          </div>
          <div className="mt-3" style={{ fontSize: 12.5, color: 'var(--ink-soft)' }}>
            Reschedules require at least 12 hours&apos; notice before the current scheduled time.
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirmCancel}
        title="Cancel reservation"
        message={`The booking for ${prosumerOf(confirmCancel)?.fullName || confirmCancel?.prosumerNic} will be cancelled and its battery slot released. Cancellations require at least 12 hours' notice before the scheduled time.`}
        confirmText="Cancel reservation"
        onCancel={() => setConfirmCancel(null)}
        onConfirm={() => (confirmCancel ? handleAction(cancelReservation, confirmCancel, 'Reservation cancelled.') : Promise.resolve())}
      />
      </div>
    </div>
  )
}