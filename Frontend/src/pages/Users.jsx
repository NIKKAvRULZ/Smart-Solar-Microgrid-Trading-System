import React, { useEffect, useState } from 'react'
import { getUsers, createUser, updateUser, deactivateUser } from '../api/api'
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
import { IconPlus, IconEdit, IconTrash, IconUsers } from '../components/ui/Icons'
import { useToast } from '../components/ui/Toast'

const EMPTY_FORM = { username: '', password: '', fullName: '', email: '', role: 'GridOperator' }

export default function Users() {
  const toast = useToast()
  const [users, setUsers] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [confirm, setConfirm] = useState(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadUsers()
  }, [])

  async function loadUsers() {
    setLoading(true)
    try {
      const res = await getUsers()
      setUsers(res.data)
    } catch (err) {
      toast.error('Could not load users', getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  function openCreate() {
    setEditing(null)
    setForm(EMPTY_FORM)
    setFormOpen(true)
  }

  function openEdit(u) {
    setEditing(u)
    setForm({ username: u.username, password: '', fullName: u.fullName, email: u.email, role: u.role })
    setFormOpen(true)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    try {
      if (editing) {
        await updateUser(editing.id, {
          fullName: form.fullName,
          email: form.email,
          role: form.role,
          isActive: true,
        })
        toast.success('User updated', `${form.username} has been updated.`)
      } else {
        await createUser(form)
        toast.success('User created', `${form.username} can now sign in.`)
      }
      setFormOpen(false)
      await loadUsers()
    } catch (err) {
      toast.error('Save failed', getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDeactivate(u) {
    try {
      await deactivateUser(u.id)
      toast.success('User deactivated', `${u.username} can no longer sign in.`)
      await loadUsers()
    } catch (err) {
      toast.error('Could not deactivate user', getErrorMessage(err))
    }
  }

  const q = search.trim().toLowerCase()
  const filtered = users.filter(
    (u) =>
      !q ||
      u.username?.toLowerCase().includes(q) ||
      u.fullName?.toLowerCase().includes(q) ||
      u.email?.toLowerCase().includes(q) ||
      u.role?.toLowerCase().includes(q),
  )

  return (
    <>
      <PageHeader
        title="User Management"
        subtitle="Create and manage Backoffice and Grid Operator accounts."
        actions={
          <button className="btn btn-solar" onClick={openCreate}>
            <IconPlus size={16} /> New User
          </button>
        }
      />

      <div className="panel">
        <div className="toolbar">
          <div className="toolbar-left">
            <SearchInput value={search} onChange={setSearch} placeholder="Search users..." />
            <span className="cell-muted" style={{ fontSize: 13 }}>
              {filtered.length} of {users.length}
            </span>
          </div>
        </div>

        {loading ? (
          <div className="loading-block">
            <Spinner size={20} /> Loading users...
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<IconUsers size={26} />}
            title={users.length === 0 ? 'No users yet' : 'No matching users'}
            hint="Create web application accounts from the New User button."
          />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Contact</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th style={{ width: 90 }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((u) => (
                  <tr key={u.id}>
                    <td>
                      <div className="cell-strong">{u.username}</div>
                      <div className="cell-muted" style={{ fontSize: 12 }}>{u.fullName}</div>
                    </td>
                    <td>
                      <span className="cell-muted" style={{ fontSize: 13 }}>{u.email}</span>
                    </td>
                    <td>
                      <Badge tone={u.role === 'Backoffice' ? 'backoffice' : 'grid'}>{u.role}</Badge>
                    </td>
                    <td>
                      <Badge tone={u.isActive ? 'active' : 'inactive'}>
                        {u.isActive ? 'Active' : 'Deactivated'}
                      </Badge>
                    </td>
                    <td>
                      <button className="icon-action" title="Edit" onClick={() => openEdit(u)}>
                        <IconEdit size={16} />
                      </button>
                      {u.isActive && (
                        <button
                          className="icon-action warn"
                          title="Deactivate"
                          onClick={() => setConfirm(u)}
                        >
                          <IconTrash size={16} />
                        </button>
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
        title={editing ? 'Edit user' : 'Create new user'}
        subtitle={editing ? form.username : 'Choose a role — Backoffice or Grid Operator.'}
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setFormOpen(false)} disabled={saving}>
              Cancel
            </button>
            <button type="submit" form="user-form" className="btn btn-solar" disabled={saving}>
              {saving ? <Spinner size={15} /> : editing ? 'Save changes' : 'Create user'}
            </button>
          </>
        }
      >
        <form id="user-form" onSubmit={handleSubmit}>
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label-sm">Username</label>
              <input
                className="form-control"
                value={form.username}
                disabled={!!editing}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                required
                autoFocus
              />
            </div>
            <div className="col-md-6">
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
            {!editing && (
              <div className="col-md-6">
                <label className="form-label-sm">Password</label>
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
            <div className="col-md-6">
              <label className="form-label-sm">Role</label>
              <select
                className="form-select"
                value={form.role}
                onChange={(e) => setForm({ ...form, role: e.target.value })}
              >
                <option value="Backoffice">Backoffice — system administration</option>
                <option value="GridOperator">Grid Operator — operational tools</option>
              </select>
            </div>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirm}
        title="Deactivate user"
        message={`The account "${confirm?.username}" will no longer be able to sign in. You can update it later to restore access.`}
        confirmText="Deactivate"
        onCancel={() => setConfirm(null)}
        onConfirm={() => (confirm ? handleDeactivate(confirm) : Promise.resolve())}
      />
    </>
  )
}