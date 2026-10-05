// -----------------------------------------------------------------------------
// File: auth.js
// Purpose: Pure utility functions for user role validation and route authorization
// -----------------------------------------------------------------------------

/**
 * Determines whether a user has permission to access a route with specified role restrictions.
 * @param {object|null} user - The current authenticated user object.
 * @param {string[]} [allowedRoles] - Optional array of roles permitted to access the route.
 * @returns {boolean} True if the user is authorized; otherwise false.
 */
export function canAccessRoute(user, allowedRoles) {
  if (!user) return false
  if (!allowedRoles || allowedRoles.length === 0) return true
  return allowedRoles.includes(user.role)
}

/**
 * Checks if the user holds a specific target role.
 * @param {object|null} user - The current user object.
 * @param {string} role - The role to verify against.
 * @returns {boolean} True if the user has the target role; otherwise false.
 */
export function isRole(user, role) {
  return Boolean(user && user.role === role)
}

/**
 * Formats a system role enum string into a human-readable display title.
 * @param {string} [role] - The system role name.
 * @returns {string} Human-friendly role label.
 */
export function formatUserRole(role) {
  switch (role) {
    case 'Backoffice':
      return 'Backoffice Administrator'
    case 'GridOperator':
      return 'Grid Operator'
    case 'Prosumer':
      return 'Solar Prosumer'
    default:
      return 'Guest'
  }
}

/**
 * Validates whether a token string is present and structurally valid.
 * @param {string|null|undefined} token - The auth session token.
 * @returns {boolean} True if token is non-empty string.
 */
export function isValidToken(token) {
  return typeof token === 'string' && token.trim().length > 0
}
