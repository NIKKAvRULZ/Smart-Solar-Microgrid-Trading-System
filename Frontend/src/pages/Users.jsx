import React, { useEffect, useMemo, useState } from 'react'
import { getUsers, createUser, updateUser, deactivateUser } from '../api/api'
import { getErrorMessage } from '../utils/errors'
import Modal from '../components/ui/Modal'
import { EmptyState, Spinner, ConfirmDialog } from '../components/ui/Widgets'
import { IconPlus } from '../components/ui/Icons'
import { useToast } from '../components/ui/Toast'
import {
  Search,
  ListFilter,
  Pencil,
  Trash,
  Mail,
  RefreshCw,
  Users as UsersIcon,
  TriangleAlert,
} from 'lucide-react'

const EMPTY_FORM = { username: '', password: '', fullName: '', email: '', role: 'GridOperator' }

const ALL_ROLES = 'All Roles'
const ALL_STATUSES = 'All Statuses'

const ROLE_BADGE = {
  Backoffice: 'um-role-indigo',
  GridOperator: 'um-role-sky',
}

const DEFAULT_BADGE = 'um-role-slate'

export default function Users() {
  const toast = useToast()
  const [users, setUsers] = useState([])
  const [search, setSearch] = useState('')
  const [draftRole, setDraftRole] = useState(ALL_ROLES)
  const [draftStatus, setDraftStatus] = useState(ALL_STATUSES)
  const [appliedRole, setAppliedRole] = useState(ALL_ROLES)
  const [appliedStatus, setAppliedStatus] = useState(ALL_STATUSES)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
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
    setError(null)
    try {
      const res = await getUsers()
      setUsers(res.data)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const roleOptions = useMemo(
    () => [...new Set(users.map((u) => u.role).filter(Boolean))],
    [users],
  )

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
  const filtered = users.filter((u) => {
    if (
      q &&
      ![u.username, u.fullName, u.email, u.role].some((v) => (v || '').toLowerCase().includes(q))
    ) {
      return false
    }
    if (appliedRole !== ALL_ROLES && u.role !== appliedRole) return false
    if (appliedStatus === 'Active' && !u.isActive) return false
    if (appliedStatus === 'Deactivated' && u.isActive) return false
    return true
  })

  const filtersActive = appliedRole !== ALL_ROLES || appliedStatus !== ALL_STATUSES

  function applyFilters() {
    setAppliedRole(draftRole)
    setAppliedStatus(draftStatus)
  }

  function clearFilters() {
    setSearch('')
    setDraftRole(ALL_ROLES)
    setDraftStatus(ALL_STATUSES)
    setAppliedRole(ALL_ROLES)
    setAppliedStatus(ALL_STATUSES)
  }

  return (
    <div className="um-dash">
      <div className="um-container">
        <div className="um-header">
          <div className="um-title">
            <h1>User Management</h1>
            <p>Create and manage Backoffice and Grid Operator accounts.</p>
          </div>
          <button className="btn btn-solar" onClick={openCreate}>
            <IconPlus size={16} /> New User
          </button>
        </div>

        <div className="um-panel">
          <div className="um-toolbar">
            <div className="um-search">
              <Search size={17} />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search users..."
              />
            </div>
            <span className="um-count">
              {filtered.length} of {users.length} users
            </span>

            <select
              className="um-select"
              value={draftRole}
              onChange={(e) => setDraftRole(e.target.value)}
            >
              <option value={ALL_ROLES}>{ALL_ROLES}</option>
              {roleOptions.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>

            <select
              className="um-select"
              value={draftStatus}
              onChange={(e) => setDraftStatus(e.target.value)}
            >
              <option value={ALL_STATUSES}>{ALL_STATUSES}</option>
              <option value="Active">Active</option>
              <option value="Deactivated">Deactivated</option>
            </select>

            <button
              className={`um-filter${filtersActive ? ' is-on' : ''}`}
              title="Apply role and status filters"
              onClick={applyFilters}
            >
              <ListFilter size={16} /> Filter
            </button>
          </div>

          {loading ? (
            <div className="loading-block">
              <Spinner size={20} /> Loading users...
            </div>
          ) : error ? (
            <div className="um-error">
              <span className="um-error-icon">
                <TriangleAlert size={20} />
              </span>
              <div className="um-error-body">
                <strong>Could not load users</strong>
                <span>{error}</span>
              </div>
              <button className="btn btn-ghost" onClick={() => loadUsers()}>
                <RefreshCw size={15} /> Retry
              </button>
            </div>
          ) : filtered.length === 0 ? (
            <div className="um-empty">
              <EmptyState
                icon={<UsersIcon size={26} />}
                title={users.length === 0 ? 'No users yet' : 'No matching users'}
                hint={
                  users.length === 0
                    ? 'Create web application accounts from the New User button.'
                    : 'Adjust or clear the search and filters to see more results.'
                }
              />
              {users.length > 0 && filtersActive && (
                <button className="btn btn-ghost" onClick={clearFilters}>
                  Clear filters
                </button>
              )}
            </div>
          ) : (
            <div className="um-table-wrap">
              <table className="um-table">
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Contact</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th style={{ width: 96 }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((u) => (
                    <tr key={u.id}>
<td data-label="User">
                        <div>
                          <div className="um-name">{u.username}</div>
                          <div className="um-fullname">{u.fullName}</div>
                        </div>
                      </td>
                      <td data-label="Contact">
                        <span className="um-email">
                          <Mail size={14} /> {u.email}
                        </span>
                      </td>
                      <td data-label="Role">
                        <span className={`um-role ${ROLE_BADGE[u.role] || DEFAULT_BADGE}`}>
                          {u.role}
                        </span>
                      </td>
                      <td data-label="Status">
                        <span className={`pill ${u.isActive ? 'pill-on' : 'pill-off'}`}>
                          {u.isActive ? 'Active' : 'Deactivated'}
                        </span>
                      </td>
                      <td data-label="Actions">
                        <div className="um-actions">
                          <button className="um-action-btn" title="Edit user" onClick={() => openEdit(u)}>
                            <Pencil size={15} />
                          </button>
                          {u.isActive && (
                            <button
                              className="um-action-btn danger"
                              title="Deactivate user"
                              onClick={() => setConfirm(u)}
                            >
                              <Trash size={15} />
                            </button>
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
                {roleOptions.length > 0
                  ? roleOptions.map((r) => (
                      <option key={r} value={r}>
                        {r === 'Backoffice' ? 'Backoffice — system administration' : `${r} — operational tools`}
                      </option>
                    ))
                  : (
                      <>
                        <option value="Backoffice">Backoffice — system administration</option>
                        <option value="GridOperator">Grid Operator — operational tools</option>
                      </>
                    )}
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
    </div>
  )
}