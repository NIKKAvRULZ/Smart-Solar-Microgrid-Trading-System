import React from 'react'
import {
  IconNode,
  IconBattery,
  IconChevronDown,
  IconZap,
  IconCheckCircle,
  IconArrowRight,
} from '../components/ui/Icons'

const KPIS = [
  {
    tone: 'blue',
    icon: <IconNode size={20} />,
    label: 'Total Nodes',
    display: '5',
    sub: 'Active monitoring hubs',
    kind: 'network',
    footerTone: 'success',
    footerLabel: 'Live',
    fill: 100,
  },
  {
    tone: 'amber',
    icon: <IconZap size={20} />,
    label: 'Total Capacity',
    display: '43,620 kWh',
    sub: 'Across all nodes',
    kind: 'power',
    footerTone: 'neutral',
    footerLabel: 'Peak load',
    fill: 72,
  },
  {
    tone: 'green',
    icon: <IconBattery size={20} />,
    label: 'Available Battery Slots',
    display: '32,720 / 32,720',
    sub: '100% available',
    kind: 'battery',
    footerTone: 'success',
    footerLabel: 'Stored',
    fill: 100,
  },
  {
    tone: 'ice',
    icon: <IconCheckCircle size={20} />,
    label: 'Operational Status',
    display: '4 / 5',
    sub: 'Some nodes offline',
    kind: 'status',
    footerTone: 'warning',
    footerLabel: '1 offline',
    fill: 78,
  },
]

const RESERVATIONS = [
  { id: '200159502678', time: '9/21/2026, 7:20:00 PM', status: 'Pending' },
  { id: '200159502678', time: '9/18/2026, 6:56:00 PM', status: 'Approved' },
  { id: '200159502678', time: '9/17/2026, 1:15:00 PM', status: 'Completed' },
]

const DONUT_TOTAL = 3

function MainArt() {
  return (
    <svg className="smg-mainbg" viewBox="0 0 1200 460" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
      <defs>
        <radialGradient id="smgMgSun" cx="0.5" cy="0.5" r="0.5">
          <stop offset="0%" stopColor="#FFDFA8" stopOpacity="0.8" />
          <stop offset="60%" stopColor="#FFDFA8" stopOpacity="0.3" />
          <stop offset="100%" stopColor="#FFDFA8" stopOpacity="0" />
        </radialGradient>
      </defs>

      <ellipse cx="380" cy="300" rx="360" ry="190" fill="url(#smgMgSun)" opacity="0.5" />

      <g fill="#FFFFFF" opacity="0.75">
        <ellipse cx="230" cy="108" rx="100" ry="24" />
        <ellipse cx="318" cy="92" rx="72" ry="20" />
        <ellipse cx="740" cy="150" rx="110" ry="26" />
        <ellipse cx="836" cy="130" rx="72" ry="20" />
        <ellipse cx="1010" cy="96" rx="84" ry="20" />
        <ellipse cx="1088" cy="80" rx="60" ry="16" />
      </g>

      <path d="M0 330 L150 190 300 330 Z" fill="#C8E1F5" opacity="0.5" />
      <path d="M150 348 L330 178 510 348 Z" fill="#B6D7EF" opacity="0.46" />
      <path d="M360 362 L560 198 750 362 Z" fill="#A9CFE9" opacity="0.42" />
      <path d="M620 368 L805 220 985 368 Z" fill="#C3DEF4" opacity="0.44" />
      <path d="M880 356 L1060 208 1200 322 V460 H880 Z" fill="#B8D8F1" opacity="0.4" />

      <g transform="translate(720 -70) rotate(38)" opacity="0.5">
        <rect width="250" height="10" rx="2.5" fill="#C3DDF4" />
        <rect y="15" width="236" height="10" rx="2.5" fill="#CEE1F4" opacity="0.85" />
        <rect y="30" width="212" height="10" rx="2.5" fill="#D7E6F5" opacity="0.75" />
        <rect y="45" width="180" height="10" rx="2.5" fill="#DDE9F6" opacity="0.65" />
      </g>
    </svg>
  )
}

function Spark({ stroke, points }) {
  return (
    <svg className="smg-spark" viewBox="0 0 40 22" preserveAspectRatio="none" aria-hidden="true">
      <polyline
        points={points}
        fill="none"
        stroke={stroke}
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
        opacity="0.9"
      />
    </svg>
  )
}

function Ring() {
  const R = 16.5
  const C = 2 * Math.PI * R
  const off = C * (1 - 99.9 / 100)
  return (
    <svg className="smg-ring" viewBox="0 0 44 44" aria-hidden="true">
      <circle cx="22" cy="22" r={R} className="smg-ring-track" />
      <circle
        cx="22"
        cy="22"
        r={R}
        className="smg-ring-value"
        style={{ '--rOff': `${off}px`, '--drawStart': `${C}px` }}
        stroke="#F59B00"
        strokeDasharray={C}
      />
      <text x="22" y="23" textAnchor="middle" className="smg-ring-text">
        99.9%
      </text>
    </svg>
  )
}

function StatusDonut({ value, total, color, label, cancelled }) {
  const R = 26
  const C = 2 * Math.PI * R
  const frac = total ? value / total : 0
  const off = C * (1 - frac)
  return (
    <div className="smg-donut">
      <div className="smg-donut-svg">
        <svg viewBox="0 0 72 72" aria-hidden="true">
          <circle cx="36" cy="36" r={R} className="smg-donut-track" />
          {frac > 0 && (
            <circle
              cx="36"
              cy="36"
              r={R}
              className="smg-donut-arc"
              style={{ '--arcOff': `${off}px`, '--drawStart': `${C}px` }}
              stroke={color}
              strokeDasharray={C}
            />
          )}
        </svg>
        <span className="smg-donut-value" style={{ color: cancelled ? '#AEB9C5' : color }}>
          {value}
        </span>
        {cancelled && <span className="smg-donut-flag" />}
      </div>
      <div className="smg-donut-label" style={cancelled ? { color: 'var(--smg-cancelled)' } : undefined}>
        {cancelled && <span className="smg-dot" />}
        {label}
      </div>
    </div>
  )
}

export default function Dashboard() {
  return (
    <div className="smg-page">
      <MainArt />
      <div className="smg-wrap">
        <header className="smg-heading">
          <h1>Dashboard</h1>
          <p>Microgrid network overview and live trading activity.</p>
        </header>

        <div className="smg-kpis">
          {KPIS.map((k) => (
            <div className={`smg-kpi ${k.tone}`} key={k.label}>
              <button className="smg-kpi-arrow" type="button" aria-label="Open card summary">
                <IconArrowRight size={13} />
              </button>

              <div className="smg-kpi-icon">{k.icon}</div>

              <div className="smg-kpi-main">
                <div className="smg-kpi-value">{k.display}</div>
                <div className="smg-kpi-label">{k.label}</div>
                <div className="smg-kpi-sub">{k.sub}</div>

                <div className="smg-kpi-footer">
                  <span className={`smg-kpi-pill ${k.footerTone}`}>{k.footerLabel}</span>
                  <span className="smg-kpi-track">
                    <i style={{ width: `${k.fill}%` }} />
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="smg-grid">
          <section className="smg-card">
            <div className="smg-card-head">
              <h3>Reservation status</h3>
              <button className="smg-dd" type="button">
                All time
                <IconChevronDown size={13} />
              </button>
            </div>
            <div className="smg-donuts">
              <StatusDonut value={1} total={DONUT_TOTAL} color="#F59E0B" label="Pending" />
              <StatusDonut value={1} total={DONUT_TOTAL} color="#2563EB" label="Approved" />
              <StatusDonut value={1} total={DONUT_TOTAL} color="#059669" label="Completed" />
              <StatusDonut value={0} total={DONUT_TOTAL} color="#E3E9F1" label="Cancelled" cancelled />
            </div>
          </section>

          <section className="smg-card">
            <div className="smg-card-head">
              <h3>Latest reservations</h3>
              <button className="smg-dd" type="button">
                Most recent
                <IconChevronDown size={13} />
              </button>
            </div>
            <div className="smg-table-wrap">
              <table className="smg-table">
                <thead>
                  <tr>
                    <th>Reservation ID</th>
                    <th>Date &amp; Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {RESERVATIONS.map((r, i) => (
                    <tr key={i}>
                      <td className="smg-id">{r.id}</td>
                      <td className="smg-time">{r.time}</td>
                      <td>
                        <span className={`smg-badge ${r.status.toLowerCase()}`}>{r.status}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </div>
    </div>
  )
}
