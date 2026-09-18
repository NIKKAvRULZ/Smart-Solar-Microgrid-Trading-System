import React, { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  IconSun,
  IconUser,
  IconLock,
  IconEye,
  IconEyeOff,
  IconArrowRight,
  IconMonitor,
  IconShieldCheck,
  IconSpinner,
} from '../components/ui/Icons'

const PARTICLES = Array.from({ length: 16 })

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [remember, setRemember] = useState(true)
  const [forgotHint, setForgotHint] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const passwordRef = useRef(null)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  function togglePassword() {
    setShowPassword((s) => !s)
    if (passwordRef.current) {
      passwordRef.current.focus()
    }
  }

  function handleForgot(e) {
    e.preventDefault()
    setForgotHint(true)
  }

  return (
    <div className="login-page">
      <div className="banner-particles" aria-hidden="true">
        {PARTICLES.map((_, i) => (
          <span key={i} className="particle"></span>
        ))}
      </div>

      <header className="login-header">
        <div className="brand anim-drop">
          <div className="brand-logo">
            <IconSun size={22} />
          </div>
          <div>
            <div className="brand-title">Smart Solar Microgrid</div>
            <div className="brand-sub">Trading System</div>
          </div>
        </div>

        <div className="status-badge anim-pop" role="status">
          <span className="status-dot-wrap"><span className="status-dot"></span></span>
          System online
        </div>
      </header>

      <main className="login-center">
        <div className="login-card anim-card">
          <h1>Welcome back</h1>
          <p className="login-sub">Sign in to manage your microgrid network.</p>

          {error && (
            <div className="error-banner shake" key={error} role="alert">{error}</div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            <div className="login-field">
              <label className="form-label-sm" htmlFor="login-username">Username</label>
              <div className="login-input-wrap">
                <IconUser size={17} className="field-icon" />
                <input
                  id="login-username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter your username"
                  required
                  autoComplete="username"
                  autoFocus
                />
              </div>
            </div>

            <div className="login-field">
              <label className="form-label-sm" htmlFor="login-password">Password</label>
              <div className="login-input-wrap">
                <IconLock size={17} className="field-icon" />
                <input
                  id="login-password"
                  ref={passwordRef}
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  required
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={togglePassword}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  tabIndex="0"
                >
                  {showPassword ? <IconEyeOff size={17} /> : <IconEye size={17} />}
                </button>
              </div>
            </div>

            <div className="login-options">
              <label className="checkbox-row">
                <input
                  type="checkbox"
                  checked={remember}
                  onChange={(e) => setRemember(e.target.checked)}
                />
                <span>Remember me</span>
              </label>
              <button
                type="button"
                className="forgot-link"
                onClick={handleForgot}
                aria-label="Forgot password - not yet available"
              >
                Forgot password?
              </button>
            </div>

            {forgotHint && (
              <div className="forgot-hint" role="note">
                Password recovery is not available yet. Contact your system
                administrator to reset your password.
              </div>
            )}

            <button
              type="submit"
              className="btn btn-solar login-btn"
              disabled={loading}
            >
              {loading ? (
                <>
                  <IconSpinner size={16} /> Signing in...
                </>
              ) : (
                <>
                  Sign in <IconArrowRight size={16} />
                </>
              )}
            </button>
          </form>

          <div className="login-divider">
            <span className="divider-line"></span>
            <IconLock size={15} className="divider-lock" />
            <span className="divider-line"></span>
          </div>
          <p className="login-secure">
            Secure access for Backoffice and Grid Operators
          </p>
        </div>
      </main>

      <footer className="login-footer">
        <div className="login-features anim-rise delay-4">
          <div className="feature-item">
            <span className="feature-icon"><IconMonitor size={17} /></span>
            <span>Live grid monitoring</span>
          </div>
          <div className="feature-item">
            <span className="feature-icon"><IconShieldCheck size={17} /></span>
            <span>Secure reservations</span>
          </div>
          <div className="feature-item">
            <span className="feature-icon"><IconLock size={17} /></span>
            <span>Role-based access</span>
          </div>
        </div>
      </footer>
    </div>
  )
}