import React, { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { getNodes, createNode, updateNode, deactivateNode } from '../api/api'
import { getErrorMessage } from '../utils/errors'
import Modal from '../components/ui/Modal'
import {
  PageHeader,
  SearchInput,
  EmptyState,
  Badge,
  Spinner,
  ConfirmDialog,
} from '../components/ui/Widgets'
import { IconPlus, IconEdit, IconTrash, IconNode, IconMapPin, IconBattery, IconZap } from '../components/ui/Icons'
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

export default function Nodes() {
  const { user } = useAuth()
  const toast = useToast()
  const [nodes, setNodes] = useState([])
  const [search, setSearch] = useState('')
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
      !q ||
      n.name?.toLowerCase().includes(q) ||
      n.operatingSchedule?.join(' ')?.toLowerCase().includes(q) ||
      String(n.capacityKWh).includes(q),
  )

  return (
    <>
      <PageHeader
        title="Microgrid Nodes"
        subtitle="Solar grid hubs — location, capacity and battery storage slots."
        actions={
          user.role === 'Backoffice' ? (
            <button className="btn btn-solar" onClick={openCreate}>
              <IconPlus size={16} /> Register Node
            </button>
          ) : (
            <span className="cell-muted" style={{ fontSize: 13 }}>
              You can update battery slots on each node below.
            </span>
          )
        }
      />

      <div className="panel">
        <div className="toolbar">
          <div className="toolbar-left">
            <SearchInput value={search} onChange={setSearch} placeholder="Search nodes..." />
            <span className="cell-muted" style={{ fontSize: 13 }}>
              {filtered.length} of {nodes.length}
            </span>
          </div>
        </div>

        {loading ? (
          <div className="loading-block">
            <Spinner size={20} /> Loading nodes...
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<IconNode size={26} />}
            title={nodes.length === 0 ? 'No microgrid nodes registered' : 'No matching nodes'}
            hint="Register solar grid hubs to begin accepting energy bookings."
          />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Node</th>
                  <th>Location</th>
                  <th>Capacity</th>
                  <th>Battery slots</th>
                  <th>Status</th>
                  <th style={{ width: 90 }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((n) => {
                  const pct = n.totalBatterySlots ? Math.round((n.availableBatterySlots / n.totalBatterySlots) * 100) : 0
                  return (
                    <tr key={n.id}>
                      <td>
                        <div className="cell-strong">{n.name}</div>
                        <div className="cell-muted" style={{ fontSize: 12 }}>
                          {n.operatingSchedule?.join(' · ')}
                        </div>
                      </td>
                      <td>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, color: 'var(--ink-soft)' }}>
                          <IconMapPin size={14} />
                          {n.latitude?.toFixed(4)}, {n.longitude?.toFixed(4)}
                        </span>
                      </td>
                      <td>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                          <IconZap size={14} style={{ color: 'var(--solar-amber-dark)' }} />
                          {n.capacityKWh} kWh
                        </span>
                      </td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 7 }}>
                          <IconBattery size={14} style={{ color: pct === 0 ? 'var(--alert-clay)' : 'var(--battery-green)' }} />
                          <span style={{ fontWeight: 600 }}>{n.availableBatterySlots}</span>
                          <span className="cell-muted">/ {n.totalBatterySlots}</span>
                        </div>
                        <div
                          style={{
                            height: 5,
                            width: 90,
                            background: 'var(--line)',
                            borderRadius: 4,
                            overflow: 'hidden',
                            marginTop: 4,
                          }}
                        >
                          <div
                            style={{
                              height: '100%',
                              width: `${pct}%`,
                              background: pct === 0 ? 'var(--alert-clay)' : 'var(--battery-green)',
                              borderRadius: 4,
                            }}
                          />
                        </div>
                      </td>
                      <td>
                        <Badge tone={n.isActive ? 'active' : 'inactive'}>
                          {n.isActive ? 'Active' : 'Deactivated'}
                        </Badge>
                      </td>
                      <td>
                        <button className="icon-action" title="Edit" onClick={() => openEdit(n)}>
                          <IconEdit size={16} />
                        </button>
                        {n.isActive && user.role === 'Backoffice' && (
                          <button
                            className="icon-action warn"
                            title="Deactivate"
                            onClick={() => setConfirmDeactivate(n)}
                          >
                            <IconTrash size={16} />
                          </button>
                        )}
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
                      <IconMapPin size={12} /> Latitude
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
    </>
  )
}