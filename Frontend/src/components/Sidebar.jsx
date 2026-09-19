import React from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

// Sidebar background image
import sidebarBackground from '../images/sidebar-background.png'

import {
  IconDashboard,
  IconUsers,
  IconProsumer,
  IconNode,
  IconReservation,
  IconLogout,
  IconSun,
} from './ui/Icons'

const NAV_SECTIONS = [
  {
    label: 'Overview',
    items: [
      {
        path: '/dashboard',
        label: 'Dashboard',
        icon: <IconDashboard size={17} />,
        roles: ['Backoffice', 'GridOperator'],
      },
      {
        path: '/reservations',
        label: 'Reservations',
        icon: <IconReservation size={17} />,
        roles: ['GridOperator'],
      },
    ],
  },
  {
    label: 'Manage',
    items: [
      {
        path: '/prosumers',
        label: 'Prosumers',
        icon: <IconProsumer size={17} />,
        roles: ['Backoffice', 'GridOperator'],
      },
      {
        path: '/nodes',
        label: 'Microgrid Nodes',
        icon: <IconNode size={17} />,
        roles: ['Backoffice', 'GridOperator'],
      },
    ],
  },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  const initials = (user?.fullName || user?.username || '?')
    .split(' ')
    .filter(Boolean)
    .map((name) => name[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()

  const sections = [
    ...NAV_SECTIONS,

    ...(user?.role === 'Backoffice'
      ? [
          {
            label: 'Administration',
            items: [
              {
                path: '/users',
                label: 'User Management',
                icon: <IconUsers size={17} />,
                roles: ['Backoffice'],
              },
            ],
          },
        ]
      : []),
  ]
    .map((section) => ({
      ...section,
      items: section.items.filter((item) =>
        item.roles.includes(user?.role)
      ),
    }))
    .filter((section) => section.items.length > 0)

  function isActivePath(path) {
    return (
      location.pathname === path ||
      location.pathname.startsWith(`${path}/`)
    )
  }

  return (
    <aside
      className="smg-sidebar"
      style={{
        '--sidebar-background': `url(${sidebarBackground})`,
      }}
    >
      {/* Brand */}
      <div className="smg-brand">
        <div className="smg-brand-logo">
          <IconSun size={20} />
        </div>

        <div className="smg-brand-text">
          <div className="smg-brand-title">Solar Microgrid</div>
          <div className="smg-brand-sub">Trading System</div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="smg-nav" aria-label="Main navigation">
        {sections.map((section) => (
          <React.Fragment key={section.label}>
            <div className="smg-nav-section">{section.label}</div>

            {section.items.map((item) => (
              <button
                key={item.path}
                type="button"
                className={`smg-nav-link ${
                  isActivePath(item.path) ? 'active' : ''
                }`}
                onClick={() => navigate(item.path)}
                aria-current={
                  isActivePath(item.path) ? 'page' : undefined
                }
              >
                {item.icon}

                <span>{item.label}</span>
              </button>
            ))}
          </React.Fragment>
        ))}
      </nav>

      {/* User footer */}
      <footer className="smg-foot">
        <div className="smg-foot-divider" />

        <div className="smg-user">
          <div className="smg-user-avatar">{initials}</div>

          <div className="smg-user-meta">
            <div className="smg-user-name">
              {user?.fullName || user?.username || 'User'}
            </div>

            <span className="smg-user-pill">
              {user?.role || 'User'}
            </span>
          </div>
        </div>

        <div className="smg-foot-bottom">
          <button
            className="smg-logout"
            type="button"
            onClick={handleLogout}
          >
            <IconLogout size={16} />
            <span>Log out</span>
          </button>

          <div className="smg-clean-energy" aria-hidden="true">
            <span>Clean Energy</span>
            <span>Stronger</span>
            <span>Communities</span>
          </div>
        </div>
      </footer>
    </aside>
  )
}