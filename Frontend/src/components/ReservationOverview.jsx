import React, { useCallback, useEffect, useState } from 'react'
import { PieChart as RechartsPieChart, Pie, Cell, ResponsiveContainer } from 'recharts'
import { ChartColumn, ChevronDown, Check, TriangleAlert, ChartPie } from 'lucide-react'
import { getDashboard } from '../api/api'
import { getErrorMessage } from '../utils/errors'

const STATUS_COLORS = {
  Pending: '#F5A524',
  Approved: '#2E6FD8',
  Completed: '#16A266',
  Cancelled: '#AEC2D8',
}
const STATUS_ORDER = ['Pending', 'Approved', 'Completed', 'Cancelled']
const PERIODS = [
  { key: 'all', label: 'All time' },
  { key: 'today', label: 'Today' },
  { key: 'week', label: 'This week' },
  { key: 'month', label: 'This month' },
]

const pctOf = (value, total) => (total > 0 ? (value / total) * 100 : 0)
const fmtPct = (value) => `${value.toFixed(1)}%`

export default function ReservationOverview() {
  const [period, setPeriod] = useState('all')
  const [menuOpen, setMenuOpen] = useState(false)
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = useCallback(async (periodKey, asSkeleton = true) => {
    if (asSkeleton) setLoading(true)
    setError(null)
    try {
      const res = await getDashboard(periodKey)
      setData(res.data?.reservations || null)
    } catch (e) {
      setError(getErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load(period, true)
  }, [period, load])

  const changePeriod = (key) => {
    setPeriod(key)
    setMenuOpen(false)
    load(key, true)
  }

  const ready = data !== null
  const total = ready ? data.total || 0 : 0
  const slices = ready
    ? STATUS_ORDER.map((s) => ({
        status: s,
        value: data[s.toLowerCase()] || 0,
        color: STATUS_COLORS[s],
        pct: pctOf(data[s.toLowerCase()] || 0, total),
      }))
    : []
  const donutData = slices.filter((s) => s.value > 0)

  const successShare = ready
    ? pctOf((data.approved || 0) + (data.completed || 0), total)
    : 0

  const renderInnerLabel = (props) => {
    const percent = props?.percent ?? 0
    if (percent < 0.07) return null
    const RADIAN = Math.PI / 180
    const radius = props.innerRadius + (props.outerRadius - props.innerRadius) * 0.55
    const x = props.cx + radius * Math.cos(-props.midAngle * RADIAN)
    const y = props.cy + radius * Math.sin(-props.midAngle * RADIAN)
    return (
      <text
        x={x}
        y={y}
        fill="#FFFFFF"
        fontSize={12}
        fontWeight={700}
        textAnchor="middle"
        dominantBaseline="central"
      >
        {Math.round(percent * total)}
      </text>
    )
  }

  const periodLabel = PERIODS.find((p) => p.key === period)?.label || 'All time'

  return (
    <section className="ro-card">
      <div className="ro-head">
        <div className="ro-title">
          <h3>Reservation overview</h3>
          <p>Distribution of reservation status across all time.</p>
        </div>
        <div className="ro-dd-wrap">
          <button
            className="ro-dd"
            type="button"
            onClick={() => setMenuOpen(!menuOpen)}
            aria-haspopup="menu"
            aria-expanded={menuOpen}
          >
            {periodLabel}
            <ChevronDown size={14} />
          </button>
          {menuOpen && (
            <div className="ro-dd-menu" role="menu">
              {PERIODS.map((o) => (
                <button
                  key={o.key}
                  role="menuitem"
                  className={period === o.key ? 'active' : ''}
                  onClick={() => changePeriod(o.key)}
                >
                  <span>{o.label}</span>
                  {period === o.key && <Check size={13} />}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {error ? (
        <div className="ro-state ro-error">
          <TriangleAlert size={26} />
          <h4>Could not load reservations</h4>
          <p>{error}</p>
          <button type="button" className="btn btn-solar" onClick={() => load(period, true)}>
            Retry
          </button>
        </div>
      ) : loading && !ready ? (
        <div className="ro-loading">
          <div className="ro-skel-donut" />
          <div className="ro-skel-col">
            {[0, 1, 2, 3].map((i) => (
              <div className="ro-skel-row" key={i} />
            ))}
            <div className="ro-skel-sum" />
          </div>
        </div>
      ) : total === 0 ? (
        <div className="ro-state">
          <ChartPie size={26} />
          <h4>No reservations yet</h4>
          <p>Reservations will appear here once bookings are created.</p>
        </div>
      ) : (
        <div className="ro-body">
          <div className="ro-chart">
            <ResponsiveContainer width="100%" height="100%">
              <RechartsPieChart>
                <Pie
                  data={donutData}
                  dataKey="value"
                  nameKey="status"
                  cx="50%"
                  cy="50%"
                  innerRadius={62}
                  outerRadius={88}
                  paddingAngle={2}
                  cornerRadius={5}
                  stroke="none"
                  label={renderInnerLabel}
                  isAnimationActive={true}
                >
                  {donutData.map((slice) => (
                    <Cell key={slice.status} fill={slice.color} />
                  ))}
                </Pie>
              </RechartsPieChart>
            </ResponsiveContainer>
            <div className="ro-chart-center">
              <span className="ro-center-total">{total.toLocaleString()}</span>
              <span className="ro-center-caption">Total</span>
              <span className="ro-center-sub">{total === 1 ? 'reservation' : 'reservations'}</span>
            </div>
          </div>

          <div className="ro-side">
            <div className="ro-legend">
              {slices.map((s) => (
                <div className="ro-legend-row" key={s.status}>
                  <span className="ro-dot" style={{ background: s.color }} />
                  <span className="ro-name">{s.status}</span>
                  <span className="ro-count">{s.value.toLocaleString()}</span>
                  <span className="ro-pct">{fmtPct(s.pct)}</span>
                </div>
              ))}
            </div>

            <div className="ro-summary">
              <span className="ro-summary-icon">
                <ChartColumn size={16} />
              </span>
              <div className="ro-summary-text">
                <span className="ro-summary-value">{fmtPct(successShare)}</span>
                <span className="ro-summary-label">of reservations completed or approved</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}