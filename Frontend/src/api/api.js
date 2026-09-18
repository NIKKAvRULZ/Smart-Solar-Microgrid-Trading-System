// -----------------------------------------------------------------------------
// File: api.js
// Purpose: Thin wrapper functions around axiosClient, one per Web Service
// endpoint. Pages import from here rather than calling axios directly, so
// the web application stays limited to UI logic (per FAT-service spec).
// -----------------------------------------------------------------------------
import axiosClient from './axiosClient'

// ---- Auth ----
export const login = (username, password) =>
  axiosClient.post('/auth/login', { username, password })

// ---- Users (Backoffice-only administration) ----
export const getUsers = () => axiosClient.get('/users')
export const getUser = (id) => axiosClient.get(`/users/${id}`)
export const createUser = (data) => axiosClient.post('/users', data)
export const updateUser = (id, data) => axiosClient.put(`/users/${id}`, data)
export const deactivateUser = (id) => axiosClient.patch(`/users/${id}/deactivate`)

// ---- Prosumers ----
export const getProsumers = () => axiosClient.get('/prosumers')
export const getProsumer = (nic) => axiosClient.get(`/prosumers/${nic}`)
export const createProsumer = (data) => axiosClient.post('/prosumers', data)
export const updateProsumer = (nic, data) => axiosClient.put(`/prosumers/${nic}`, data)
export const deactivateProsumer = (nic) => axiosClient.patch(`/prosumers/${nic}/deactivate`)
export const reactivateProsumer = (nic) => axiosClient.patch(`/prosumers/${nic}/reactivate`)

// ---- Microgrid Nodes ----
export const getNodes = () => axiosClient.get('/nodes')
export const getNode = (id) => axiosClient.get(`/nodes/${id}`)
export const createNode = (data) => axiosClient.post('/nodes', data)
export const updateNode = (id, data) => axiosClient.put(`/nodes/${id}`, data)
export const deactivateNode = (id) => axiosClient.patch(`/nodes/${id}/deactivate`)

// ---- Reservations ----
export const getReservations = () => axiosClient.get('/reservations')
export const getReservationsByProsumer = (nic) => axiosClient.get(`/reservations/prosumer/${nic}`)
export const createReservation = (data) => axiosClient.post('/reservations', data)
export const updateReservation = (id, data) => axiosClient.put(`/reservations/${id}`, data)
export const cancelReservation = (id) => axiosClient.patch(`/reservations/${id}/cancel`)
export const approveReservation = (id) => axiosClient.patch(`/reservations/${id}/approve`)
export const completeReservation = (id) => axiosClient.patch(`/reservations/${id}/complete`)
