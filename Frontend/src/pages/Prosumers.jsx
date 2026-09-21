import React, { useEffect, useState } from 'react'
import {
  Users,
  Zap,
  ChartColumn,
  Search,
  ListFilter,
  Pencil,
  Trash,
  Check,
  Key,
  Plus,
  ChevronDown,
  TrendingUp,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { getProsumers, createProsumer, updateProsumer, deactivateProsumer, reactivateProsumer } from '../api/api'
import { getErrorMessage } from '../utils/errors'
import Modal from '../components/ui/Modal'
import { EmptyState, Spinner, ConfirmDialog } from '../components/ui/Widgets'
import { useToast } from '../components/ui/Toast'

const EMPTY_FORM = { nic: '', fullName: '', email: '', phone: '', address: '', password: '' }

export default function Prosumers() {
  const { user } = useAuth()
  const toast = useToast()
  const [prosumers, setProsumers] = useState([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')
  const [filterOpen, setFilterOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editingNic, setEditingNic] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [confirmDeactivate, setConfirmDeactivate] = useState(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadProsumers()
  }, [])

  async function loadProsumers() {
    setLoading(true)
    try {
      const res = await getProsumers()
      setProsumers(res.data)
    } catch (err) {
      toast.error('Could not load prosumers', getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  function openCreate() {
    setEditingNic(null)
    setForm(EMPTY_FORM)
    setFormOpen(true)
  }

  function openEdit(p) {
    setEditingNic(p.nic)
    setForm({
      nic: p.nic,
      fullName: p.fullName,
      email: p.email,
      phone: p.phone,
      address: p.address,
      password: '',
    })
    setFormOpen(true)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    try {
      if (editingNic) {
        await updateProsumer(editingNic, {
          fullName: form.fullName,
          email: form.email,
          phone: form.phone,
          address: form.address,
        })
        toast.success('Prosumer updated', `${form.fullName}'s profile was updated.`)
      } else {
        await createProsumer(form)
        toast.success('Prosumer registered', `${form.fullName} (${form.nic}) can now use the mobile app.`)
      }
      setFormOpen(false)
      await loadProsumers()
    } catch (err) {
      toast.error('Save failed', getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDeactivate(p) {
    try {
      await deactivateProsumer(p.nic)
      toast.success('Prosumer deactivated', `${p.fullName} can no longer trade energy.`)
      await loadProsumers()
    } catch (err) {
      toast.error('Could not deactivate prosumer', getErrorMessage(err))
    }
  }

  async function handleReactivate(p) {
    try {
      await reactivateProsumer(p.nic)
      toast.success('Prosumer reactivated', `${p.fullName} can trade energy again.`)
      await loadProsumers()
    } catch (err) {
      toast.error('Could not reactivate prosumer', getErrorMessage(err))
    }
  }

  const q = search.trim().toLowerCase()
  const filtered = prosumers.filter(
    (p) =>
      (statusFilter === 'all' || (statusFilter === 'active') === !!p.isActive) &&
      (!q ||
        p.nic?.toLowerCase().includes(q) ||
        p.fullName?.toLowerCase().includes(q) ||
        p.email?.toLowerCase().includes(q) ||
        p.phone?.toLowerCase().includes(q)),
  )

  const activeCount = prosumers.filter((p) => p.isActive).length

  const FILTER_OPTIONS = [
    { value: 'all', label: 'All statuses' },
    { value: 'active', label: 'Active' },
    { value: 'deactivated', label: 'Deactivated' },
  ]

  return (
    <div className="pros-dash">
      <div className="pros-container">
        <div className="pros-header">
          <div className="pros-title">
            <h1>Prosumers</h1>
            <p>Property owners with solar arrays trading energy on the grid.</p>
          </div>
          <button className="btn btn-solar" onClick={openCreate}>
            <Plus size={16} /> New Prosumer
          </button>
        </div>

        <div className="pros-cards">
          <div className="pros-card">
            <div className="pros-card-body">
              <div className="pros-card-icon icon-blue">
                <Users size={20} />
              </div>
              <div className="pros-card-value">{prosumers.length}</div>
              <div className="pros-card-label">Total Prosumers</div>
              <div className="pros-card-sub">Registered property owners</div>
            </div>
            <div className="pros-users-illu" aria-hidden="true">
              <svg viewBox="0 0 76 48" fill="none">
                <g opacity="0.85">
                  <circle cx="24" cy="13" r="8" fill="#9CC3F5" />
                  <path d="M6 44c1.8-9 9.4-12.5 18-12.5S42 35 43.8 44H6Z" fill="#B8D4F8" />
                  <circle cx="54" cy="19" r="5.5" fill="#C8E0FA" />
                  <path d="M43 46.5c1.2-6.4 6.2-9 12-9s10.8 2.6 12 9H43Z" fill="#DCECFD" />
                </g>
              </svg>
            </div>
          </div>

          <div className="pros-card">
            <div className="pros-card-body">
              <div className="pros-card-icon icon-green">
                <Zap size={20} />
              </div>
              <div className="pros-card-value">{activeCount}</div>
              <div className="pros-card-label">Active Prosumers</div>
              <div className="pros-card-sub">Currently trading energy</div>
            </div>
            <div className="pros-ring" aria-hidden="true">
              <svg viewBox="0 0 64 64">
                <circle className="pros-ring-track" cx="32" cy="32" r="26" />
                <circle className="pros-ring-fill" cx="32" cy="32" r="26" />
              </svg>
              <span>{Math.round((prosumers.length ? activeCount / prosumers.length : 0) * 100)}%</span>
            </div>
          </div>

          <div className="pros-card">
            <div className="pros-card-body">
              <div className="pros-card-icon icon-amber">
                <ChartColumn size={20} />
              </div>
              <div className="pros-card-value">12.8 MWh</div>
              <div className="pros-card-label">Grid Contribution</div>
              <div className="pros-card-sub">Total energy supplied</div>
              <div className="pros-card-delta">
                <TrendingUp size={14} /> 12% vs. last month
              </div>
            </div>
            <div className="pros-bars" aria-hidden="true">
              <span style={{ height: '36%' }} />
              <span style={{ height: '54%' }} />
              <span style={{ height: '44%' }} />
              <span style={{ height: '70%' }} />
              <span style={{ height: '86%' }} />
            </div>
          </div>
        </div>

        <div className="pros-panel">
        <div className="pros-toolbar">
          <div className="pros-search">
            <Search size={17} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by NIC, name, email..."
            />
          </div>
          <span className="pros-count">
            {filtered.length} of {prosumers.length}
          </span>
          <div className="pros-filter-wrap">
            <button
              className={`pros-filter${filterOpen ? ' is-on' : ''}`}
              onClick={() => setFilterOpen(!filterOpen)}
            >
              <ListFilter size={16} />
              <span className="pros-filter-label">
                {FILTER_OPTIONS.find((o) => o.value === statusFilter)?.label}
              </span>
              <ChevronDown size={15} />
            </button>
            {filterOpen && (
              <div className="pros-filter-menu">
                {FILTER_OPTIONS.map((o) => (
                  <button
                    key={o.value}
                    className={statusFilter === o.value ? 'active' : ''}
                    onClick={() => {
                      setStatusFilter(o.value)
                      setFilterOpen(false)
                    }}
                  >
                    <span>{o.label}</span>
                    {statusFilter === o.value && <Check size={14} />}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        {loading ? (
          <div className="loading-block">
            <Spinner size={20} /> Loading prosumers...
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<Users size={26} />}
            title={prosumers.length === 0 ? 'No prosumers registered yet' : 'No matching prosumers'}
            hint="Register a prosumer with their NIC to grant mobile app access."
          />
        ) : (
          <div className="pros-table-wrap">
            <table className="pros-table">
              <thead>
                <tr>
                  <th>NIC</th>
                  <th>Name</th>
                  <th>Contact</th>
                  <th>Status</th>
                  <th style={{ width: 96 }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((p) => (
                  <tr key={p.nic}>
                    <td data-label="NIC">
                      <span className="pros-nic">{p.nic}</span>
                    </td>
                    <td data-label="Name">
                      <div className="pros-name">{p.fullName}</div>
                      <div className="pros-muted">{p.address}</div>
                    </td>
                    <td data-label="Contact">
                      <div className="pros-contact-main">{p.email}</div>
                      <div className="pros-muted">{p.phone}</div>
                    </td>
                    <td data-label="Status">
                      <span className={`pill ${p.isActive ? 'pill-on' : 'pill-off'}`}>
                        {p.isActive ? 'Active' : 'Deactivated'}
                      </span>
                    </td>
                    <td data-label="Actions">
                      <div className="pros-actions">
                        <button className="pros-action-btn" title="Edit prosumer" onClick={() => openEdit(p)}>
                          <Pencil size={15} />
                        </button>
                        {p.isActive ? (
                          <button
                            className="pros-action-btn danger"
                            title="Deactivate prosumer"
                            onClick={() => setConfirmDeactivate(p)}
                          >
                            <Trash size={15} />
                          </button>
                        ) : (
                          user.role === 'Backoffice' && (
                            <button
                              className="pros-action-btn success"
                              title="Reactivate prosumer"
                              onClick={() => handleReactivate(p)}
                            >
                              <Check size={15} />
                            </button>
                          )
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingNic ? `Edit prosumer ${editingNic}` : 'Register new prosumer'}
        subtitle={
          editingNic
            ? 'NIC is the primary key and cannot be changed.'
            : 'The password lets the prosumer sign in to the mobile app.'
        }
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setFormOpen(false)} disabled={saving}>
              Cancel
            </button>
            <button type="submit" form="prosumer-form" className="btn btn-solar" disabled={saving}>
              {saving ? <Spinner size={15} /> : editingNic ? 'Save changes' : 'Create prosumer'}
            </button>
          </>
        }
      >
        <form id="prosumer-form" onSubmit={handleSubmit}>
          <div className="row g-3">
            <div className="col-md-4">
              <label className="form-label-sm">NIC (primary key)</label>
              <input
                className="form-control"
                value={form.nic}
                disabled={!!editingNic}
                onChange={(e) => setForm({ ...form, nic: e.target.value })}
                required
                autoFocus
              />
            </div>
            <div className="col-md-8">
              <label className="form-label-sm">Full name</label>
              <input
                className="form-control"
                value={form.fullName}
                onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                required
              />
            </div>
            <div className="col-md-6">
              <label className="form-label-sm">Email</label>
              <input
                type="email"
                className="form-control"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
              />
            </div>
            <div className="col-md-6">
              <label className="form-label-sm">Phone</label>
              <input
                className="form-control"
                value={form.phone}
                onChange={(e) => setForm({ ...form, phone: e.target.value })}
                required
              />
            </div>
            <div className="col-12">
              <label className="form-label-sm">Address</label>
              <input
                className="form-control"
                value={form.address}
                onChange={(e) => setForm({ ...form, address: e.target.value })}
                required
              />
            </div>
            {!editingNic && (
              <div className="col-md-6">
                <label className="form-label-sm">
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                    <Key size={12} /> Mobile app password
                  </span>
                </label>
                <input
                  type="password"
                  className="form-control"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                  minLength={6}
                />
              </div>
            )}
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirmDeactivate}
        title="Deactivate prosumer"
        message={`"${confirmDeactivate?.fullName}" (${confirmDeactivate?.nic}) will no longer be able to reserve energy slots. Only a Backoffice officer can reactivate this account.`}
        confirmText="Deactivate"
        onCancel={() => setConfirmDeactivate(null)}
        onConfirm={() => (confirmDeactivate ? handleDeactivate(confirmDeactivate) : Promise.resolve())}
      />
      </div>
    </div>
  )
}