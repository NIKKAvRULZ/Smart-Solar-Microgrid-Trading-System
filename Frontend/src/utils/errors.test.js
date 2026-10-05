// -----------------------------------------------------------------------------
// File: errors.test.js
// Purpose: Unit tests for frontend error extraction utility
// -----------------------------------------------------------------------------
import { describe, it } from 'node:test'
import assert from 'node:assert/strict'
import { getErrorMessage } from './errors.js'

describe('getErrorMessage Utility', () => {
  it('extracts error message from response.data.message', () => {
    const error = {
      response: {
        data: {
          message: 'Reservation must be scheduled within the next 7 days.'
        }
      }
    }
    const result = getErrorMessage(error)
    assert.strictEqual(result, 'Reservation must be scheduled within the next 7 days.')
  })

  it('extracts error title when message is absent', () => {
    const error = {
      response: {
        data: {
          title: 'One or more validation errors occurred.'
        }
      }
    }
    const result = getErrorMessage(error)
    assert.strictEqual(result, 'One or more validation errors occurred.')
  })

  it('falls back to err.message when response.data is absent', () => {
    const error = new Error('Network timeout connecting to microgrid node.')
    const result = getErrorMessage(error)
    assert.strictEqual(result, 'Network timeout connecting to microgrid node.')
  })

  it('returns default fallback when err is empty or unknown shape', () => {
    const result = getErrorMessage({})
    assert.strictEqual(result, 'Something went wrong.')
  })

  it('returns custom fallback when specified and no message exists', () => {
    const result = getErrorMessage(null, 'Unable to communicate with solar hub.')
    assert.strictEqual(result, 'Unable to communicate with solar hub.')
  })
})
