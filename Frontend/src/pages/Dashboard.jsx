import React, { useCallback, useEffect, useState } from 'react'
import {
  Network,
  Zap,
  Battery,
  CircleCheck,
  RefreshCw,
  Users,
  TriangleAlert,
  ArrowUpRight,
  ArrowDownRight,
  Activity,
  TrendingUp,
} from 'lucide-react'
import { getDashboard } from '../api/api'
import { getErrorMessage } from '../utils/errors'
import ReservationOverview from '../components/ReservationOverview'

const POLL_INTERVAL = 30000

const fmtCount = (n) => (n ?? 0).toLocaleString()
const fmtKwh = (n) => (n ?? 0).toLocaleString('en-US', { maximumFractionDigits: 2 })
const pct = (value, total) => (total > 0 ? (value / total) * 100 : 0)
const occupiedPct = (total, available) => (total > 0 ? ((total - available) / total) * 100 : 0)

const clampPct = (value) => Math.min(100, Math.max(0, Number(value) || 0))

const KPI_TONES = {
  blue: {
    icon: 'bg-[#E7F0FB] text-[#2E6FD8]',
    accent: 'bg-[#2E6FD8]',
    bar: 'bg-linear-to-r from-[#6FA7F5] to-[#2E6FD8]',
    badge: 'bg-[#E4EDFB] text-[#2E6FD8]',
  },
  amber: {
    icon: 'bg-[#FDF0DC] text-[#F59A05]',
    accent: 'bg-[#F59A05]',
    bar: 'bg-linear-to-r from-[#FFC766] to-[#F59A05]',
    badge: 'bg-[#FFF4E0] text-[#C77300]',
  },
  teal: {
    icon: 'bg-[#DDF4EE] text-[#13A085]',
    accent: 'bg-[#13A085]',
    bar: 'bg-linear-to-r from-[#3AD0AF] to-[#13A085]',
    badge: 'bg-[#E0F5EF] text-[#0E8F75]',
  },
  emerald: {
    icon: 'bg-[#E2F5EB] text-[#16A266]',
    accent: 'bg-[#16A266]',
    bar: 'bg-linear-to-r from-[#43D499] to-[#16A266]',
    badge: 'bg-[#E5F7EE] text-[#128152]',
  },
}

function Skeleton({ className }) {
  return <div className={`dash-skeleton ${className || ''}`} />
}

function KpiCard({ icon, tone, value, label, sub, pill, barPct, warning }) {
  const t = KPI_TONES[tone] || KPI_TONES.blue
  return (
    <div className="relative flex min-h-0 min-w-0 flex-col overflow-hidden rounded-2xl border border-[#DBE7F2] bg-linear-to-b from-white via-[#FBFDFF] to-[#F2F8FE] px-5 pt-5 pb-4 shadow-[0_1px_2px_rgba(13,47,74,0.04),0_22px_40px_-26px_rgba(13,47,74,0.3)]">
      <span aria-hidden="true" className={`absolute inset-x-0 top-0 h-1 ${t.accent}`} />
      <div className="flex items-start justify-between gap-2.5">
        <span className={`grid h-12 w-12 shrink-0 place-items-center rounded-2xl ${t.icon}`}>{icon}</span>
        {warning}
      </div>
      <div className="mt-3 min-w-0">
        <div className="truncate text-[27px] font-extrabold leading-tight tracking-tight text-[#0A2233]">
          {value}
        </div>
        <div className="mt-1 truncate text-[15px] font-semibold text-[#23405A]">{label}</div>
        <div className="mt-0.5 truncate text-[12.5px] font-medium text-[#6F8297]">{sub}</div>
      </div>
      <div className="mt-auto flex items-center gap-3 pt-3">
        <span className={`shrink-0 rounded-full px-2.5 py-1 text-[11px] font-bold ${t.badge}`}>{pill}</span>
        <div className="h-1.5 min-w-0 flex-1 overflow-hidden rounded-full bg-[#EBF1F6]">
          <span
            className={`block h-full rounded-full transition-[width] duration-500 ease-out ${t.bar}`}
            style={{ width: `${clampPct(barPct)}%` }}
          />
        </div>
      </div>
    </div>
  )
}

function DeltaPill({ pct: value, suffix = '%' }) {
  const v = Number(value || 0)
  const up = v > 0
  const down = v < 0
  return (
    <span className={`dash-delta ${down ? 'down' : up ? 'up' : 'flat'}`}>
      {down ? <ArrowDownRight size={13} /> : up ? <ArrowUpRight size={13} /> : null}
      {up ? '+' : ''}
      {fmtKwh(v)}
      {suffix}
    </span>
  )
}

function TradingChart({ last7Days }) {
  const days = last7Days || []
  const maxKwh = Math.max(...days.map((d) => d.kwh || 0), 0)
  const W = 400
  const H = 150
  const pad = 10
  const innerW = W - pad * 2
  const innerH = H - pad * 2
  const hasData = days.some((d) => (d.kwh || 0) > 0)

  if (!days.length) {
    return (
      <div className="dash-empty dash-empty-sm">
        <Activity size={22} />
        <p>No trading data available.</p>
      </div>
    )
  }

  if (!hasData) {
    return (
      <div className="dash-empty dash-empty-sm">
        <Activity size={22} />
        <p>No completed trades in the last 7 days yet.</p>
        <span>Complete a reservation to see its energy here.</span>
      </div>
    )
  }

  const points = days.map((d, i) => {
    const x = pad + (days.length === 1 ? innerW / 2 : (innerW * i) / (days.length - 1))
    const y = maxKwh > 0 ? pad + innerH - (innerH * (d.kwh || 0)) / maxKwh : pad + innerH
    return { x, y, kwh: d.kwh || 0, date: d.date }
  })

  const line = points.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ')
  const area = `${line} L${points[points.length - 1].x.toFixed(1)},${pad + innerH} L${points[0].x.toFixed(1)},${pad + innerH} Z`

  return (
    <div className="dash-chart-wrap">
      <svg className="dash-chart" viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none" aria-hidden="true">
        <defs>
          <linearGradient id="dashArea" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#2E6FD8" stopOpacity="0.28" />
            <stop offset="100%" stopColor="#2E6FD8" stopOpacity="0.02" />
          </linearGradient>
        </defs>
        <path d={area} fill="url(#dashArea)" />
        <path d={line} fill="none" stroke="#2E6FD8" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
        {points.map((p, i) =>
          p.kwh > 0 ? (
            <circle key={i} cx={p.x} cy={p.y} r="3.2" fill="#fff" stroke="#2E6FD8" strokeWidth="2" />
          ) : null,
        )}
      </svg>
      <div className="dash-chart-labels">
        {points.map((p, i) => (
          <span key={i}>{p.date.slice(5)}</span>
        ))}
      </div>
    </div>
  )
}

export default function Dashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState(null)
  const [lastUpdated, setLastUpdated] = useState(null)

  const load = useCallback(async (skeleton = true) => {
    if (skeleton) setLoading(true)
    else setRefreshing(true)
    setError(null)
    try {
      const res = await getDashboard()
      setData(res.data)
      setLastUpdated(new Date())
    } catch (e) {
      setError(getErrorMessage(e))
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    load(true)
    const timer = setInterval(() => load(false), POLL_INTERVAL)
    const onFocus = () => load(false)
    window.addEventListener('focus', onFocus)
    return () => {
      clearInterval(timer)
      window.removeEventListener('focus', onFocus)
    }
  }, [load])

  const nodes = data?.nodes
  const prosumers = data?.prosumers
  const reservations = data?.reservations
  const energy = data?.energy

  return (
    <div className="dash-dash">
      <div className="dash-container">
        <header className="dash-header">
          <div className="dash-title">
            <h1>Dashboard</h1>
            <p>Microgrid network overview and live trading activity.</p>
          </div>
          <div className="dash-header-actions">
            {lastUpdated && (
              <span className="dash-updated">
                Last updated {lastUpdated.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
              </span>
            )}
            <button
              className={`dash-refresh ${refreshing ? 'is-spinning' : ''}`}
              type="button"
              title="Refresh dashboard"
              onClick={() => load(false)}
              disabled={refreshing}
            >
              <RefreshCw size={16} />
            </button>
          </div>
        </header>

        {error ? (
          <div className="dash-error">
            <TriangleAlert size={26} />
            <h3>Could not load the dashboard</h3>
            <p>{error}</p>
            <button type="button" className="btn btn-solar" onClick={() => load(true)}>
              Retry
            </button>
          </div>
        ) : loading && !data ? (
          <div className="dash-skeleton-grid">
            <div className="kpi-grid">
              {[0, 1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="flex flex-col overflow-hidden rounded-2xl border border-[#DBE7F2] bg-white px-5 pt-5 pb-4 shadow-[0_22px_40px_-26px_rgba(13,47,74,0.3)]"
                >
                  <div className="dash-skeleton h-12 w-12 rounded-2xl" />
                  <div className="mt-3 space-y-2">
                    <div className="dash-skeleton h-6 w-3/4" />
                    <div className="dash-skeleton h-3.5 w-1/2" />
                  </div>
                  <div className="mt-auto flex items-center gap-3 pt-3">
                    <div className="dash-skeleton h-5 w-2/5 rounded-full" />
                    <div className="dash-skeleton h-1.5 w-full rounded-full" />
                  </div>
                </div>
              ))}
            </div>
            <div className="dash-grid">
              <div className="dash-card"><Skeleton className="dash-skel-block" /></div>
              <div className="dash-card"><Skeleton className="dash-skel-block" /></div>
            </div>
          </div>
        ) : (
          <>
            <div className="kpi-grid">
              <KpiCard
                icon={<Network size={22} />}
                tone="blue"
                value={fmtCount(nodes?.total)}
                label="Total Nodes"
                sub="Active monitoring hubs"
                pill={`${fmtCount(nodes?.active)} active`}
                barPct={pct(nodes?.active, nodes?.total)}
              />
              <KpiCard
                icon={<Zap size={22} />}
                tone="amber"
                value={`${fmtKwh(nodes?.capacityKWh)} kWh`}
                label="Total Capacity"
                sub="Across all microgrid nodes"
                pill={`${occupiedPct(nodes?.totalBatterySlots, nodes?.availableBatterySlots).toFixed(1)}% utilized`}
                barPct={occupiedPct(nodes?.totalBatterySlots, nodes?.availableBatterySlots)}
              />
              <KpiCard
                icon={<Battery size={22} />}
                tone="teal"
                value={`${fmtCount(nodes?.availableBatterySlots)} / ${fmtCount(nodes?.totalBatterySlots)}`}
                label="Available Battery Slots"
                sub={`${nodes?.availabilityPct?.toFixed(2)}% available`}
                pill="Free slots"
                barPct={nodes?.availabilityPct}
              />
              <KpiCard
                icon={<CircleCheck size={22} />}
                tone="emerald"
                value={`${fmtCount(nodes?.active)} / ${fmtCount(nodes?.total)}`}
                label="Operational Status"
                sub={`${fmtCount(nodes?.offline)} node${nodes?.offline === 1 ? '' : 's'} offline`}
                pill={nodes?.offline > 0 ? 'Degraded' : 'Operational'}
                barPct={pct(nodes?.active, nodes?.total)}
                warning={
                  nodes?.offline > 0 ? (
                    <span className="inline-flex items-center gap-1 rounded-full bg-[#FDF0DA] px-2 py-1 text-[11px] font-extrabold text-[#B87104]">
                      <TriangleAlert size={13} /> {fmtCount(nodes?.offline)} offline
                    </span>
                  ) : null
                }
              />
            </div>

            <div className="dash-grid">
              <ReservationOverview />

              <section className="dash-card">
                <div className="dash-card-head">
                  <h3>Latest reservations</h3>
                  <span className="dash-card-note">Most recent</span>
                </div>
                {reservations?.latest?.length ? (
                  <div className="dash-table-wrap">
                    <table className="dash-table">
                      <thead>
                        <tr>
                          <th>Reservation ID</th>
                          <th>Date &amp; Time</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {reservations.latest.map((r) => {
                          const status = r.status || 'Pending'
                          const t = new Date(r.scheduledDateTime)
                          return (
                            <tr key={r.id}>
                              <td className="dash-id">{r.id}</td>
                              <td className="dash-time">
                                {t.toLocaleDateString()}
                                <span>{t.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                              </td>
                              <td>
                                <span className={`dash-badge dash-badge-${status.toLowerCase()}`}>{status}</span>
                              </td>
                            </tr>
                          )
                        })}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div className="dash-empty">
                    <CircleCheck size={22} />
                    <p>No reservations yet.</p>
                  </div>
                )}
              </section>
            </div>

            <div className="dash-bottom">
              <section className="dash-card">
                <div className="dash-card-head">
                  <h3>Energy traded today</h3>
                  <span className="dash-card-icon amber"><TrendingUp size={15} /></span>
                </div>
                <div className="dash-bottom-value">{fmtKwh(energy?.todayKWh)} <small>kWh</small></div>
                <div className="dash-bottom-sub">
                  vs yesterday&nbsp;·&nbsp;{fmtKwh(energy?.yesterdayKWh)} kWh
                </div>
                <div className="dash-bottom-footer">
                  <DeltaPill pct={energy?.todayDeltaPct} />
                  <span className="dash-bottom-hint">completed reservations</span>
                </div>
              </section>

              <section className="dash-card">
                <div className="dash-card-head">
                  <h3>Active prosumers</h3>
                  <span className="dash-card-icon green"><Users size={15} /></span>
                </div>
                <div className="dash-bottom-value">{fmtCount(prosumers?.active)}</div>
                <div className="dash-bottom-sub">of {fmtCount(prosumers?.total)} registered prosumers</div>
                <div className="dash-bottom-footer">
                  <span className="dash-pct">{fmtKwh(prosumers?.activePct)}%</span>
                  <span className="dash-bottom-hint">account active</span>
                </div>
              </section>

              <section className="dash-card dash-card-chart">
                <div className="dash-card-head">
                  <h3>Trading activity</h3>
                  <span className="dash-card-note">Last 7 days · kWh</span>
                </div>
                <TradingChart last7Days={energy?.last7Days} />
              </section>
            </div>
          </>
        )}
      </div>
    </div>
  )
}