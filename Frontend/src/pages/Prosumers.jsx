import React, { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { getProsumers, createProsumer, updateProsumer, deactivateProsumer, reactivateProsumer } from '../api/api'
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
import { IconPlus, IconEdit, IconTrash, IconProsumer, IconCheck, IconKey } from '../components/ui/Icons'
import { useToast } from '../components/ui/Toast'

const EMPTY_FORM = { nic: '', fullName: '', email: '', phone: '', address: '', password: '' }

export default function Prosumers() {
  const { user } = useAuth()
  const toast = useToast()
  const [prosumers, setProsumers] = useState([])
  const [search, setSearch] = useState('')
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
      !q ||
      p.nic?.toLowerCase().includes(q) ||
      p.fullName?.toLowerCase().includes(q) ||
      p.email?.toLowerCase().includes(q) ||
      p.phone?.toLowerCase().includes(q),
  )

  return (
    <>
      <PageHeader
        title="Prosumers"
        subtitle="Property owners with solar arrays trading energy on the grid."
        actions={
          <button className="btn btn-solar" onClick={openCreate}>
            <IconPlus size={16} /> New Prosumer
          </button>
        }
      />

      <div className="panel">
        <div className="toolbar">
          <div className="toolbar-left">
            <SearchInput value={search} onChange={setSearch} placeholder="Search by NIC, name, email..." />
            <span className="cell-muted" style={{ fontSize: 13 }}>
              {filtered.length} of {prosumers.length}
            </span>
          </div>
        </div>

        {loading ? (
          <div className="loading-block">
            <Spinner size={20} /> Loading prosumers...
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<IconProsumer size={26} />}
            title={prosumers.length === 0 ? 'No prosumers registered yet' : 'No matching prosumers'}
            hint="Register a prosumer with their NIC to grant mobile app access."
          />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
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
                    <td>
                      <span style={{ fontFamily: 'Sora', fontWeight: 600 }}>{p.nic}</span>
                    </td>
                    <td>
                      <div className="cell-strong">{p.fullName}</div>
                      <div className="cell-muted" style={{ fontSize: 12 }}>{p.address}</div>
                    </td>
                    <td>
                      <div style={{ fontSize: 13 }}>{p.email}</div>
                      <div className="cell-muted" style={{ fontSize: 12 }}>{p.phone}</div>
                    </td>
                    <td>
                      <Badge tone={p.isActive ? 'active' : 'inactive'}>
                        {p.isActive ? 'Active' : 'Deactivated'}
                      </Badge>
                    </td>
                    <td>
                      <button className="icon-action" title="Edit" onClick={() => openEdit(p)}>
                        <IconEdit size={16} />
                      </button>
                      {p.isActive ? (
                        <button className="icon-action warn" title="Deactivate" onClick={() => setConfirmDeactivate(p)}>
                          <IconTrash size={16} />
                        </button>
                      ) : (
                        user.role === 'Backoffice' && (
                          <button className="icon-action success" title="Reactivate" onClick={() => handleReactivate(p)}>
                            <IconCheck size={16} />
                          </button>
                        )
                      )}
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
                    <IconKey size={12} /> Mobile app password
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
    </>
  )
}