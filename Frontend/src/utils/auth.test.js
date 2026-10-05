// -----------------------------------------------------------------------------
// File: auth.test.js
// Purpose: Unit tests for authentication and role authorization logic
// -----------------------------------------------------------------------------
import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { canAccessRoute, isRole, formatUserRole, isValidToken } from './auth.js'

describe('canAccessRoute Authorization Logic', () => {
  it('denies access if user is null or unauthenticated', () => {
    assert.strictEqual(canAccessRoute(null, ['Backoffice']), false)
    assert.strictEqual(canAccessRoute(undefined, ['GridOperator']), false)
    assert.strictEqual(canAccessRoute(null), false)
  })

  it('allows access to open routes with no role restrictions', () => {
    const user = { username: 'testuser', role: 'GridOperator' }
    assert.strictEqual(canAccessRoute(user), true)
    assert.strictEqual(canAccessRoute(user, []), true)
  })

  it('allows access when user role is in the allowedRoles list', () => {
    const backofficeUser = { username: 'admin', role: 'Backoffice' }
    assert.strictEqual(canAccessRoute(backofficeUser, ['Backoffice']), true)
    assert.strictEqual(canAccessRoute(backofficeUser, ['Backoffice', 'GridOperator']), true)
  })

  it('denies access when user role is not in the allowedRoles list', () => {
    const gridOperator = { username: 'operator', role: 'GridOperator' }
    assert.strictEqual(canAccessRoute(gridOperator, ['Backoffice']), false)

    const prosumer = { username: '200012345678', role: 'Prosumer' }
    assert.strictEqual(canAccessRoute(prosumer, ['Backoffice', 'GridOperator']), false)
  })
})

describe('isRole Helper', () => {
  it('identifies target role accurately', () => {
    const user = { username: 'admin', role: 'Backoffice' }
    assert.strictEqual(isRole(user, 'Backoffice'), true)
    assert.strictEqual(isRole(user, 'GridOperator'), false)
  })

  it('returns false when user is null', () => {
    assert.strictEqual(isRole(null, 'Backoffice'), false)
  })
})

describe('formatUserRole Helper', () => {
  it('formats known roles into user-friendly titles', () => {
    assert.strictEqual(formatUserRole('Backoffice'), 'Backoffice Administrator')
    assert.strictEqual(formatUserRole('GridOperator'), 'Grid Operator')
    assert.strictEqual(formatUserRole('Prosumer'), 'Solar Prosumer')
  })

  it('handles unknown or missing roles gracefully', () => {
    assert.strictEqual(formatUserRole('UnknownRole'), 'Guest')
    assert.strictEqual(formatUserRole(), 'Guest')
  })
})

describe('isValidToken Helper', () => {
  it('validates non-empty string tokens', () => {
    assert.strictEqual(isValidToken('valid-uuid-token-12345'), true)
  })

  it('rejects empty, null, or invalid tokens', () => {
    assert.strictEqual(isValidToken(''), false)
    assert.strictEqual(isValidToken('   '), false)
    assert.strictEqual(isValidToken(null), false)
    assert.strictEqual(isValidToken(undefined), false)
    assert.strictEqual(isValidToken(12345), false)
  })
})
