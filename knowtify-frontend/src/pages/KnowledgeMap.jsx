import { useEffect, useMemo, useState } from 'react'
import { studyAPI } from '../services/api'
import { subjectColor } from '../utils/colors'

function KnowledgeMap() {
  // modes: 'all' | 'week' | 'range'
  const [mode, setMode] = useState('all')
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Range state (yyyy-MM-dd)
  const today = new Date()
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  // Filters
  const [subjectFilter, setSubjectFilter] = useState('ALL')
  const [search, setSearch] = useState('')

  // Sorting
  // options: 'count_desc' | 'last_desc' | 'name_asc'
  const [sortBy, setSortBy] = useState('count_desc')

  // Density
  const [compact, setCompact] = useState(false)

  // REAL API CALLS - RESTORED
  const fetchAll = async () => studyAPI.getKnowledgeMap()
  const fetchWeek = async () => studyAPI.getWeeklyReport()
  const fetchRange = async () =>
    studyAPI.getKnowledgeMap({ params: { startDate, endDate } })

  const load = async (currentMode) => {
    setLoading(true)
    setError(null)
    try {
      let res
      if (currentMode === 'all') {
        res = await fetchAll()
      } else if (currentMode === 'week') {
        res = await fetchWeek()
      } else {
        // 'range'
        if (!startDate || !endDate) {
          setData(null)
          setError('Select both start and end dates')
          setLoading(false)
          return
        }
        res = await fetchRange()
      }
      setData(res.data)
    } catch {
      setError('Failed to load knowledge data')
      setData(null)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(mode)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mode])

  const applyRange = async () => {
    await load('range')
  }

  const clearRange = () => {
    setStartDate('')
    setEndDate('')
    setMode('all')
  }

  // Collect subjects for dropdown
  const subjectOptions = useMemo(() => {
    const set = new Set((data?.subjects || []).map(s => s.subject))
    return ['ALL', ...Array.from(set).sort((a, b) => a.localeCompare(b))]
  }, [data])

  // Flattened rows for table render
  const flattenedRows = useMemo(() => {
    if (!data?.subjects?.length) return []

    // Build { subject, name, count, isPriority, lastStudiedAt }
    let rows = data.subjects.flatMap(s =>
      (s.topics || []).map(t => ({
        subject: s.subject,
        name: t.name,
        count: t.count ?? 0,
        isPriority: !!t.isPriority,
        lastStudiedAt: t.lastStudiedAt || null,
      }))
    )

    // Apply filters
    if (subjectFilter !== 'ALL') {
      rows = rows.filter(r => r.subject === subjectFilter)
    }
    if (search.trim()) {
      const q = search.trim().toLowerCase()
      rows = rows.filter(r =>
        r.name.toLowerCase().includes(q) ||
        r.subject.toLowerCase().includes(q)
      )
    }

    // Apply sorting
    const compare = {
      count_desc: (a, b) => {
        if (b.count !== a.count) return b.count - a.count
        // tie-breaker: latest first
        const aT = a.lastStudiedAt ? Date.parse(a.lastStudiedAt) : 0
        const bT = b.lastStudiedAt ? Date.parse(b.lastStudiedAt) : 0
        if (bT !== aT) return bT - aT
        return a.name.localeCompare(b.name)
      },
      last_desc: (a, b) => {
        const aT = a.lastStudiedAt ? Date.parse(a.lastStudiedAt) : 0
        const bT = b.lastStudiedAt ? Date.parse(b.lastStudiedAt) : 0
        if (bT !== aT) return bT - aT
        if (b.count !== a.count) return b.count - a.count
        return a.name.localeCompare(b.name)
      },
      name_asc: (a, b) => {
        const s = a.name.localeCompare(b.name)
        if (s !== 0) return s
        if (a.subject !== b.subject) return a.subject.localeCompare(b.subject)
        return (b.count - a.count)
      },
    }[sortBy]

    rows.sort(compare)
    return rows
  }, [data, subjectFilter, search, sortBy])

  // Row padding style
  const cellPad = compact ? 'px-4 py-2' : 'px-6 py-3'

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* Header + Mode Switch */}
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3 mb-4">
        <h1 className="text-2xl font-bold text-gray-900">Knowledge Map</h1>

        <div className="flex items-center gap-2">
          <button
            className={`px-3 py-1 rounded-md text-sm ${mode === 'all' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'}`}
            onClick={() => setMode('all')}
          >
            All Time
          </button>
          <button
            className={`px-3 py-1 rounded-md text-sm ${mode === 'week' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'}`}
            onClick={() => setMode('week')}
          >
            This Week
          </button>
          <button
            className={`px-3 py-1 rounded-md text-sm ${mode === 'range' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700'}`}
            onClick={() => setMode('range')}
          >
            Date Range
          </button>
        </div>
      </div>

      {/* Date Range Controls */}
      {mode === 'range' && (
        <div className="bg-white border rounded-lg p-4 mb-4">
          <div className="flex flex-col sm:flex-row gap-3 sm:items-end">
            <div>
              <label className="block text-xs font-medium text-gray-600">Start date</label>
              <input
                type="date"
                className="input-field mt-1"
                value={startDate}
                max={endDate || undefined}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-600">End date</label>
              <input
                type="date"
                className="input-field mt-1"
                value={endDate}
                min={startDate || undefined}
                max={new Date(today.getTime() - today.getTimezoneOffset() * 60000)
                  .toISOString()
                  .slice(0, 10)}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>

            <div className="flex gap-2">
              <button className="btn-primary" onClick={applyRange}>Apply</button>
              <button className="btn-secondary" onClick={clearRange}>Clear</button>
            </div>
          </div>
          <p className="mt-2 text-xs text-gray-500">
            Both dates are inclusive. Clear to return to All Time.
          </p>
        </div>
      )}

      {/* Toolbar: Filters, Search, Sort, Density */}
      <div className="bg-white border rounded-lg p-4 mb-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
          <div>
            <label className="block text-xs font-medium text-gray-600">Subject</label>
            <select
              className="input-field mt-1"
              value={subjectFilter}
              onChange={(e) => setSubjectFilter(e.target.value)}
            >
              {subjectOptions.map(opt => (
                <option key={opt} value={opt}>{opt === 'ALL' ? 'All subjects' : opt}</option>
              ))}
            </select>
          </div>

          <div className="md:col-span-2">
            <label className="block text-xs font-medium text-gray-600">Search topics</label>
            <input
              type="text"
              className="input-field mt-1"
              placeholder="Type to filter by topic or subject"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-600">Sort by</label>
            <select
              className="input-field mt-1"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
            >
              <option value="count_desc">Most studied</option>
              <option value="last_desc">Recently studied</option>
              <option value="name_asc">Topic A → Z</option>
            </select>
          </div>
        </div>

        <div className="mt-3 flex items-center gap-3">
          <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={compact}
              onChange={(e) => setCompact(e.target.checked)}
            />
            Compact table
          </label>

          <button
            className="btn-secondary"
            onClick={() => {
              setSubjectFilter('ALL')
              setSearch('')
              setSortBy('count_desc')
              setCompact(false)
            }}
          >
            Reset filters
          </button>
        </div>
      </div>

      {/* Status */}
      {loading && <div>Loading…</div>}
      {error && <div className="text-red-600">{error}</div>}

      {/* Date range banner (if present) */}
      {!loading && !error && data?.dateRange && (
        <p className="text-sm text-gray-600 mb-3">
          {data.dateRange.startDate} → {data.dateRange.endDate}
        </p>
      )}

      {/* Table */}
      {!loading && !error && (
        <>
          {!flattenedRows.length ? (
            <p className="text-sm text-gray-600">No topics match your filters.</p>
          ) : (
            <div className="bg-white rounded-lg shadow overflow-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className={`${cellPad} text-left text-xs font-medium text-gray-500 uppercase`}>Subject</th>
                    <th className={`${cellPad} text-left text-xs font-medium text-gray-500 uppercase`}>Topic</th>
                    <th className={`${cellPad} text-left text-xs font-medium text-gray-500 uppercase`}>Count</th>
                    <th className={`${cellPad} text-left text-xs font-medium text-gray-500 uppercase`}>Priority</th>
                    <th className={`${cellPad} text-left text-xs font-medium text-gray-500 uppercase`}>Last Studied</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-100">
                  {flattenedRows.map((r, idx) => {
                    const c = subjectColor(r.subject)
                    return (
                      <tr key={`${r.subject}-${r.name}-${idx}`} className="hover:bg-gray-50">
                        {/* Subject chip */}
                        <td className={`${cellPad} text-sm`}>
                          <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${c.bg} ${c.text}`}>
                            {r.subject}
                          </span>
                        </td>

                        {/* Topic chip */}
                        <td className={`${cellPad} text-sm`}>
                          <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                            {r.name}
                          </span>
                        </td>

                        {/* Count */}
                        <td className={`${cellPad} text-sm text-gray-900`}>{r.count}</td>

                        {/* Priority badge */}
                        <td className={`${cellPad} text-sm`}>
                          {r.isPriority ? (
                            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold bg-red-100 text-red-800 border border-red-200">
                              PRIORITY
                            </span>
                          ) : (
                            <span className="text-xs text-gray-400">—</span>
                          )}
                        </td>

                        {/* Last studied */}
                        <td className={`${cellPad} text-sm text-gray-500`}>{r.lastStudiedAt ?? '-'}</td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default KnowledgeMap
