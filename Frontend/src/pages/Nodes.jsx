import React, { useEffect, useState } from 'react'
import {
  Network,
  Zap,
  Battery,
  CircleCheck,
  Search,
  ListFilter,
  MapPin,
  Pencil,
  Plus,
  Trash,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { getNodes, createNode, updateNode, deactivateNode } from '../api/api'
import { getErrorMessage } from '../utils/errors'
import Modal from '../components/ui/Modal'
import { EmptyState, Spinner, ConfirmDialog } from '../components/ui/Widgets'
import { useToast } from '../components/ui/Toast'

const EMPTY_FORM = {
  name: '',
  latitude: '',
  longitude: '',
  capacityKWh: '',
  totalBatterySlots: '',
  availableBatterySlots: '',
  operatingSchedule: 'Mon-Sun 06:00-20:00',
}

const fmtCap = (value) =>
  new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 }).format(Number(value) || 0)

export default function Nodes() {
  const { user } = useAuth()
  const toast = useToast()
  const [nodes, setNodes] = useState([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [confirmDeactivate, setConfirmDeactivate] = useState(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadNodes()
  }, [])

  async function loadNodes() {
    setLoading(true)
    try {
      const res = await getNodes()
      setNodes(res.data)
    } catch (err) {
      toast.error('Could not load nodes', getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  function openCreate() {
    setEditing(null)
    setForm(EMPTY_FORM)
    setFormOpen(true)
  }

  function openEdit(node) {
    setEditing(node)
    setForm({
      name: node.name,
      latitude: node.latitude,
      longitude: node.longitude,
      capacityKWh: node.capacityKWh,
      totalBatterySlots: node.totalBatterySlots,
      availableBatterySlots: node.availableBatterySlots,
      operatingSchedule: node.operatingSchedule?.join(', ') || '',
    })
    setFormOpen(true)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    const scheduleList = form.operatingSchedule
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean)

    try {
      if (editing) {
        await updateNode(editing.id, {
          name: form.name,
          capacityKWh: Number(form.capacityKWh),
          totalBatterySlots: Number(form.totalBatterySlots),
          availableBatterySlots: Number(form.availableBatterySlots),
          operatingSchedule: scheduleList,
        })
        toast.success('Node updated', `${form.name} was updated.`)
      } else {
        await createNode({
          name: form.name,
          latitude: Number(form.latitude),
          longitude: Number(form.longitude),
          capacityKWh: Number(form.capacityKWh),
          totalBatterySlots: Number(form.totalBatterySlots),
          operatingSchedule: scheduleList,
        })
        toast.success('Node registered', `${form.name} is now online.`)
      }
      setFormOpen(false)
      await loadNodes()
    } catch (err) {
      toast.error('Save failed', getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDeactivate(node) {
    try {
      await deactivateNode(node.id)
      toast.success('Node deactivated', `${node.name} was taken offline.`)
      await loadNodes()
    } catch (err) {
      toast.error('Could not deactivate node', getErrorMessage(err))
    }
  }

  const q = search.trim().toLowerCase()
  const filtered = nodes.filter(
    (n) =>
      (statusFilter === 'all' || n.isActive) &&
      (!q ||
        n.name?.toLowerCase().includes(q) ||
        n.operatingSchedule?.join(' ')?.toLowerCase().includes(q) ||
        String(n.capacityKWh).includes(q)),
  )

  const totalCapacity = nodes.reduce((sum, node) => sum + (Number(node.capacityKWh) || 0), 0)
  const availableBatterySlots = nodes.reduce((sum, node) => sum + (Number(node.availableBatterySlots) || 0), 0)
  const totalBatterySlots = nodes.reduce((sum, node) => sum + (Number(node.totalBatterySlots) || 0), 0)
  const activeNodes = nodes.filter((node) => node.isActive).length
  const batteryPct = totalBatterySlots ? (availableBatterySlots / totalBatterySlots) * 100 : 0
  const statusPct = nodes.length ? (activeNodes / nodes.length) * 100 : 0

return (
    <div className="nodes-dash">
      <div className="nodes-container">
        <div className="nodes-header">
          <div className="nodes-title">
            <h1>Microgrid Nodes</h1>
            <p>Solar grid hubs — location, capacity and battery storage slots.</p>
          </div>
          {user.role === 'Backoffice' ? (
            <button className="btn btn-solar" onClick={openCreate}>
              <Plus size={16} /> Register Node
            </button>
          ) : (
            <span className="cell-muted" style={{ fontSize: 13 }}>
              You can update battery slots on each node below.
            </span>
          )}
        </div>

        <div className="nodes-cards">
          <div className="node-card">
            <div className="node-card-icon icon-royal">
              <Network size={20} />
            </div>
            <div className="node-card-value">{nodes.length}</div>
            <div className="node-card-label">Total Nodes</div>
            <div className="node-card-sub">Active monitoring hubs</div>
            <div className="node-card-bar">
              <div className="track">
                <span className="fill fill-blue" style={{ width: `${Math.max(statusPct, 4)}%` }} />
              </div>
            </div>
          </div>

          <div className="node-card">
            <div className="node-card-icon icon-amber">
              <Zap size={20} />
            </div>
            <div className="node-card-value">{fmtCap(totalCapacity)} kWh</div>
            <div className="node-card-label">Total Capacity</div>
            <div className="node-card-sub">Across all nodes</div>
            <div className="node-card-bar">
              <div className="track">
                <span className="fill fill-amber" style={{ width: '76%' }} />
              </div>
            </div>
          </div>

          <div className="node-card">
            <div className="node-card-icon icon-teal">
              <Battery size={20} />
            </div>
            <div className="node-card-value">
              {fmtCap(availableBatterySlots)} / {fmtCap(totalBatterySlots)}
            </div>
            <div className="node-card-label">Available Battery Slots</div>
            <div className="node-card-sub">{Math.round(batteryPct)}% available</div>
            <div className="node-card-bar">
              <div className="track">
                <span className="fill fill-teal" style={{ width: `${Math.max(batteryPct, 4)}%` }} />
              </div>
            </div>
          </div>

          <div className="node-card">
            <div className="node-card-icon icon-emerald">
              <CircleCheck size={20} />
            </div>
            <div className="node-card-value">
              {activeNodes} / {nodes.length || 0}
            </div>
            <div className="node-card-label">Operational Status</div>
            <div className="node-card-sub">
              {activeNodes === nodes.length && nodes.length > 0 ? 'All nodes active' : 'Some nodes offline'}
            </div>
            <div className="node-card-bar">
              <div className="track">
                <span className="fill fill-green" style={{ width: `${Math.max(statusPct, 4)}%` }} />
              </div>
            </div>
          </div>
        </div>

      <div className="nodes-panel">
        <div className="nodes-toolbar">
          <div className="nodes-search">
            <Search size={17} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search nodes..."
            />
          </div>
          <span className="nodes-count">
            {filtered.length} of {nodes.length}
          </span>
          <button
            className={`nodes-filter${statusFilter === 'active' ? ' is-on' : ''}`}
            title={statusFilter === 'active' ? 'Showing active nodes — click to show all' : 'Filter nodes'}
            onClick={() => setStatusFilter(statusFilter === 'active' ? 'all' : 'active')}
          >
            <ListFilter size={17} />
          </button>
        </div>

        {loading ? (
          <div className="loading-block">
            <Spinner size={20} /> Loading nodes...
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<Network size={26} />}
            title={nodes.length === 0 ? 'No microgrid nodes registered' : 'No matching nodes'}
            hint="Register solar grid hubs to begin accepting energy bookings."
          />
        ) : (
          <div className="nodes-table-wrap">
            <table className="nodes-table">
              <thead>
                <tr>
                  <th>Node</th>
                  <th>Location</th>
                  <th>Capacity</th>
                  <th>Battery slots</th>
                  <th>Status</th>
                  <th style={{ width: 96 }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((n) => {
                  const slotsPct = n.totalBatterySlots
                    ? Math.round((n.availableBatterySlots / n.totalBatterySlots) * 100)
                    : 0
                  return (
                    <tr key={n.id}>
                      <td data-label="Node">
                        <div className="node-id">
                          <div>
                            <div className="node-name">{n.name}</div>
                            <div className="node-sched">{n.operatingSchedule?.join(' · ') || 'No schedule set'}</div>
                          </div>
                        </div>
                      </td>
                      <td data-label="Location">
                        <span className="node-loc">
                          <MapPin size={14} />
                          {n.latitude?.toFixed(4)}, {n.longitude?.toFixed(4)}
                        </span>
                      </td>
                      <td data-label="Capacity">
                        <span className="node-cap">
                          <Zap size={14} />
                          {n.capacityKWh} kWh
                        </span>
                      </td>
                      <td data-label="Battery slots">
                        <div className="node-slots">
                          <div className="node-slots-num">
                            <Battery size={14} />
                            {n.availableBatterySlots} <span className="node-slots-total">/ {n.totalBatterySlots}</span>
                          </div>
                          <div className="track">
                            <span
                              className={`fill fill-teal${slotsPct === 0 ? ' off' : ''}`}
                              style={{ width: `${Math.min(Math.max(slotsPct, 0), 100)}%` }}
                            />
                          </div>
                        </div>
                      </td>
                      <td data-label="Status">
                        <span className={`pill ${n.isActive ? 'pill-on' : 'pill-off'}`}>
                          {n.isActive ? 'Active' : 'Deactivated'}
                        </span>
                      </td>
                      <td data-label="Actions">
                        <div className="node-actions">
                          <button className="node-action-btn" title="Edit node" onClick={() => openEdit(n)}>
                            <Pencil size={15} />
                          </button>
                          {n.isActive && user.role === 'Backoffice' && (
                            <button
                              className="node-action-btn danger"
                              title="Deactivate node"
                              onClick={() => setConfirmDeactivate(n)}
                            >
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

      <Modal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editing ? 'Edit microgrid node' : 'Register new microgrid node'}
        subtitle={
          editing
            ? user.role === 'Backoffice'
              ? 'Update capacity, battery slots and the operating schedule.'
              : 'Grid operators can update battery slot availability and schedules.'
            : 'Backoffice only — capture GPS location, capacity and battery storage.'
        }
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setFormOpen(false)} disabled={saving}>
              Cancel
            </button>
            <button type="submit" form="node-form" className="btn btn-solar" disabled={saving}>
              {saving ? <Spinner size={15} /> : editing ? 'Save changes' : 'Register node'}
            </button>
          </>
        }
      >
        <form id="node-form" onSubmit={handleSubmit}>
          <div className="row g-3">
            <div className="col-md-8">
              <label className="form-label-sm">Node name</label>
              <input
                className="form-control"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
                autoFocus
              />
            </div>
            <div className="col-md-4">
              <label className="form-label-sm">Capacity (kWh)</label>
              <input
                type="number"
                step="any"
                className="form-control"
                value={form.capacityKWh}
                onChange={(e) => setForm({ ...form, capacityKWh: e.target.value })}
                required
              />
            </div>

            {!editing && (
              <>
                <div className="col-md-6">
                  <label className="form-label-sm">
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                      <MapPin size={12} /> Latitude
                    </span>
                  </label>
                  <input
                    type="number"
                    step="any"
                    className="form-control"
                    value={form.latitude}
                    onChange={(e) => setForm({ ...form, latitude: e.target.value })}
                    required
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label-sm">Longitude</label>
                  <input
                    type="number"
                    step="any"
                    className="form-control"
                    value={form.longitude}
                    onChange={(e) => setForm({ ...form, longitude: e.target.value })}
                    required
                  />
                </div>
              </>
            )}

            <div className="col-md-6">
              <label className="form-label-sm">Total battery slots</label>
              <input
                type="number"
                className="form-control"
                value={form.totalBatterySlots}
                onChange={(e) => setForm({ ...form, totalBatterySlots: e.target.value })}
                required
              />
            </div>
            {editing && (
              <div className="col-md-6">
                <label className="form-label-sm">Available battery slots</label>
                <input
                  type="number"
                  className="form-control"
                  value={form.availableBatterySlots}
                  onChange={(e) => setForm({ ...form, availableBatterySlots: e.target.value })}
                  required
                />
              </div>
            )}
            <div className="col-12">
              <label className="form-label-sm">Operating schedule (comma separated)</label>
              <input
                className="form-control"
                value={form.operatingSchedule}
                onChange={(e) => setForm({ ...form, operatingSchedule: e.target.value })}
                placeholder="e.g. Mon-Fri 06:00-20:00, Sat-Sun 08:00-18:00"
              />
            </div>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirmDeactivate}
        title="Deactivate node"
        message={`"${confirmDeactivate?.name}" will be taken offline. Deactivation is blocked if it has active energy reservations.`}
        confirmText="Deactivate"
        onCancel={() => setConfirmDeactivate(null)}
        onConfirm={() => (confirmDeactivate ? handleDeactivate(confirmDeactivate) : Promise.resolve())}
      />
      </div>
    </div>
  )
}