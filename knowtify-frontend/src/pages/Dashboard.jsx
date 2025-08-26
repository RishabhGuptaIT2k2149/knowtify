// import { useEffect, useState } from 'react'
// import { studyAPI } from '../services/api'
// import { useAuthStore } from '../store/authStore'
// import { subjectColor } from '../utils/colors'
// import { PlusIcon, RefreshCwIcon, BookOpenIcon, ClockIcon, TrendingUpIcon } from 'lucide-react'

// function Dashboard() {
//   const { user } = useAuthStore()

//   // Entry form state
//   const [sentence, setSentence] = useState('')
//   const [creating, setCreating] = useState(false)
//   const [result, setResult] = useState(null)
//   const [error, setError] = useState(null)

//   // Weekly report state
//   const [report, setReport] = useState(null)
//   const [loadingReport, setLoadingReport] = useState(false)

//   // Recent entries state
//   const [recent, setRecent] = useState([])
//   const [loadingRecent, setLoadingRecent] = useState(false)

//   // Load weekly report
//   const fetchReport = async () => {
//     try {
//       setLoadingReport(true)
//       const res = await studyAPI.getWeeklyReport()
//       setReport(res.data)
//     } catch (e) {
//       setError('Failed to load weekly report')
//     } finally {
//       setLoadingReport(false)
//     }
//   }

//   // Load recent entries
//   const loadRecent = async () => {
//     try {
//       setLoadingRecent(true)
//       const res = await studyAPI.listEntries(10)
//       setRecent(res.data || [])
//     } catch (e) {
//       // optional: console.warn('Failed to load recent entries', e)
//     } finally {
//       setLoadingRecent(false)
//     }
//   }

//   // Initial load with auth guard
//   useEffect(() => {
//     if (!localStorage.getItem('accessToken')) return
//     fetchReport()
//     loadRecent()
//   }, [])

//   // Submit a new entry
//   const submitEntry = async (e) => {
//     e.preventDefault()
//     if (!sentence.trim()) return
//     setCreating(true)
//     setError(null)
//     try {
//       const res = await studyAPI.createEntry({ sentence })
//       setResult(res.data)
//       setSentence('')
//       // Refresh both report and recent list
//       fetchReport()
//       loadRecent()
//     } catch (e) {
//       setError(e.response?.data?.message || 'Failed to create entry')
//     } finally {
//       setCreating(false)
//     }
//   }

//   return (
//     <div className="min-h-screen bg-gray-50">
//       <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
//         {/* Header */}
//         <div className="mb-8">
//           <h1 className="text-3xl font-bold text-gray-900">
//             Welcome back{user?.username ? `, ${user.username}` : ''}!
//           </h1>
//           <p className="mt-2 text-gray-600">Track your learning journey and monitor your progress.</p>
//         </div>

//         {/* Quick Entry Card */}
//         <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8">
//           <div className="flex items-center mb-4">
//             <div className="p-2 bg-blue-100 rounded-lg mr-3">
//               <PlusIcon className="h-5 w-5 text-blue-600" />
//             </div>
//             <h2 className="text-lg font-semibold text-gray-900">Quick Study Entry</h2>
//           </div>
          
//           <form onSubmit={submitEntry} className="space-y-4">
//             <div>
//               <textarea
//                 className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none transition-colors"
//                 rows="3"
//                 placeholder="e.g., Struggled with Docker containers; learned quicksort algorithm; practiced React hooks"
//                 value={sentence}
//                 onChange={(e) => setSentence(e.target.value)}
//               />
//             </div>
            
//             <div className="flex items-center justify-between">
//               <button
//                 type="submit"
//                 disabled={creating || !sentence.trim()}
//                 className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
//               >
//                 {creating ? (
//                   <>
//                     <RefreshCwIcon className="animate-spin -ml-1 mr-2 h-4 w-4" />
//                     Saving...
//                   </>
//                 ) : (
//                   <>
//                     <PlusIcon className="h-4 w-4 mr-2" />
//                     Save Entry
//                   </>
//                 )}
//               </button>
              
//               {sentence.trim() && (
//                 <span className="text-sm text-gray-500">
//                   {sentence.length} characters
//                 </span>
//               )}
//             </div>

//             {error && (
//               <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
//                 <p className="text-sm text-red-600">{error}</p>
//               </div>
//             )}

//             {result && (
//               <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
//                 <div className="flex items-center mb-2">
//                   <div className="h-2 w-2 bg-green-400 rounded-full mr-2"></div>
//                   <p className="text-sm font-medium text-green-800">Entry saved successfully!</p>
//                 </div>
//                 {result.parsedTopics?.length > 0 && (
//                   <div className="flex flex-wrap gap-2 mt-3">
//                     {result.parsedTopics.map((t, idx) => {
//                       const c = subjectColor(t.subject || 'Other')
//                       return (
//                         <div key={idx} className="flex items-center space-x-2">
//                           <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${c.bg} ${c.text}`}>
//                             {t.subject || 'Other'}
//                           </span>
//                           <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
//                             {t.name}
//                           </span>
//                           {t.isPriority && (
//                             <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800 border border-red-200">
//                               PRIORITY
//                             </span>
//                           )}
//                         </div>
//                       )
//                     })}
//                   </div>
//                 )}
//               </div>
//             )}
//           </form>
//         </div>

//         <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
//           {/* Weekly Report */}
//           <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
//             <div className="flex items-center justify-between mb-6">
//               <div className="flex items-center">
//                 <div className="p-2 bg-green-100 rounded-lg mr-3">
//                   <TrendingUpIcon className="h-5 w-5 text-green-600" />
//                 </div>
//                 <h2 className="text-lg font-semibold text-gray-900">This Week</h2>
//               </div>
//               <button
//                 onClick={fetchReport}
//                 disabled={loadingReport}
//                 className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
//               >
//                 <RefreshCwIcon className={`h-4 w-4 ${loadingReport ? 'animate-spin' : ''}`} />
//               </button>
//             </div>

//             {loadingReport ? (
//               <div className="flex items-center justify-center py-8">
//                 <RefreshCwIcon className="animate-spin h-6 w-6 text-gray-400" />
//                 <span className="ml-2 text-gray-600">Loading report...</span>
//               </div>
//             ) : report ? (
//               <div className="space-y-4">
//                 {report.reportWeek && (
//                   <div className="flex items-center text-sm text-gray-600 mb-4">
//                     <ClockIcon className="h-4 w-4 mr-1" />
//                     {report.reportWeek.startDate} → {report.reportWeek.endDate}
//                   </div>
//                 )}
                
//                 {report.subjects?.length ? (
//                   report.subjects.map((subject, i) => {
//                     const c = subjectColor(subject.subject)
//                     return (
//                       <div key={i} className="border border-gray-100 rounded-lg p-4">
//                         <div className="flex items-center justify-between mb-3">
//                           <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${c.bg} ${c.text}`}>
//                             {subject.subject}
//                           </span>
//                           <span className="text-sm text-gray-500">
//                             {subject.totalStudies} {subject.totalStudies === 1 ? 'entry' : 'entries'}
//                           </span>
//                         </div>
                        
//                         <div className="space-y-2">
//                           {subject.topics?.map((topic, j) => (
//                             <div key={j} className="flex items-center justify-between text-sm">
//                               <div className="flex items-center space-x-2">
//                                 <span className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-gray-100 text-gray-800">
//                                   {topic.name}
//                                 </span>
//                                 <span className="text-gray-600">× {topic.count}</span>
//                                 {topic.isPriority && (
//                                   <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-red-100 text-red-800">
//                                     PRIORITY
//                                   </span>
//                                 )}
//                               </div>
//                               <span className="text-xs text-gray-500">
//                                 {topic.lastStudiedAt ? new Date(topic.lastStudiedAt).toLocaleDateString() : '-'}
//                               </span>
//                             </div>
//                           ))}
//                         </div>
//                       </div>
//                     )
//                   })
//                 ) : (
//                   <div className="text-center py-8">
//                     <BookOpenIcon className="mx-auto h-12 w-12 text-gray-400" />
//                     <p className="mt-2 text-gray-600">No entries this week yet.</p>
//                     <p className="text-sm text-gray-500">Create your first entry above!</p>
//                   </div>
//                 )}
//               </div>
//             ) : (
//               <div className="text-center py-8">
//                 <p className="text-gray-600">Unable to load report</p>
//                 <button
//                   onClick={fetchReport}
//                   className="mt-2 text-sm text-blue-600 hover:text-blue-700"
//                 >
//                   Try again
//                 </button>
//               </div>
//             )}
//           </div>

//           {/* Recent Entries */}
//           <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
//             <div className="flex items-center justify-between mb-6">
//               <div className="flex items-center">
//                 <div className="p-2 bg-purple-100 rounded-lg mr-3">
//                   <ClockIcon className="h-5 w-5 text-purple-600" />
//                 </div>
//                 <h2 className="text-lg font-semibold text-gray-900">Recent Entries</h2>
//               </div>
//               <button
//                 onClick={loadRecent}
//                 disabled={loadingRecent}
//                 className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
//               >
//                 <RefreshCwIcon className={`h-4 w-4 ${loadingRecent ? 'animate-spin' : ''}`} />
//               </button>
//             </div>

//             {loadingRecent ? (
//               <div className="flex items-center justify-center py-8">
//                 <RefreshCwIcon className="animate-spin h-6 w-6 text-gray-400" />
//                 <span className="ml-2 text-gray-600">Loading entries...</span>
//               </div>
//             ) : recent.length ? (
//               <div className="space-y-4 max-h-96 overflow-y-auto">
//                 {recent.map((item) => (
//                   <div key={item.id} className="border border-gray-100 rounded-lg p-4 hover:bg-gray-50 transition-colors">
//                     <div className="flex justify-between items-start mb-2">
//                       <p className="text-sm text-gray-900 flex-1 mr-4">{item.originalSentence}</p>
//                       <span className="text-xs text-gray-500 whitespace-nowrap">
//                         {item.studiedAt ? new Date(item.studiedAt).toLocaleDateString() : '-'}
//                       </span>
//                     </div>
                    
//                     {item.topics?.length > 0 && (
//                       <div className="flex flex-wrap gap-1 mt-2">
//                         {item.topics.map((topic, i) => {
//                           const c = subjectColor(topic.subject || 'Other')
//                           return (
//                             <div key={i} className="flex items-center space-x-1">
//                               <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${c.bg} ${c.text}`}>
//                                 {topic.subject || 'Other'}
//                               </span>
//                               <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800">
//                                 {topic.name}
//                               </span>
//                               {topic.isPriority && (
//                                 <span className="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-semibold bg-red-100 text-red-800">
//                                   !
//                                 </span>
//                               )}
//                             </div>
//                           )
//                         })}
//                       </div>
//                     )}
//                   </div>
//                 ))}
//               </div>
//             ) : (
//               <div className="text-center py-8">
//                 <ClockIcon className="mx-auto h-12 w-12 text-gray-400" />
//                 <p className="mt-2 text-gray-600">No recent entries yet.</p>
//                 <p className="text-sm text-gray-500">Your learning history will appear here.</p>
//               </div>
//             )}
//           </div>
//         </div>
//       </div>
//     </div>
//   )
// }

// export default Dashboard



import { useEffect, useState } from 'react'
import { studyAPI } from '../services/api'
import { useAuthStore } from '../store/authStore'
import { subjectColor } from '../utils/colors'

function Dashboard() {
  const { user } = useAuthStore()

  // Entry form state
  const [sentence, setSentence] = useState('')
  const [creating, setCreating] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  // Weekly report state
  const [report, setReport] = useState(null)
  const [loadingReport, setLoadingReport] = useState(false)

  // Recent entries state
  const [recent, setRecent] = useState([])
  const [loadingRecent, setLoadingRecent] = useState(false)

  // Load weekly report
  const fetchReport = async () => {
    try {
      setLoadingReport(true)
      const res = await studyAPI.getWeeklyReport()
      setReport(res.data)
    } catch (e) {
      setError('Failed to load weekly report')
    } finally {
      setLoadingReport(false)
    }
  }

  // Load recent entries
  const loadRecent = async () => {
    try {
      setLoadingRecent(true)
      const res = await studyAPI.listEntries(10)
      setRecent(res.data || [])
    } catch (e) {
      // optional: console.warn('Failed to load recent entries', e)
    } finally {
      setLoadingRecent(false)
    }
  }

  // Initial load with auth guard
  useEffect(() => {
    if (!localStorage.getItem('accessToken')) return
    fetchReport()
    loadRecent()
  }, [])

  // Submit a new entry
  const submitEntry = async (e) => {
    e.preventDefault()
    if (!sentence.trim()) return
    setCreating(true)
    setError(null)
    try {
      const res = await studyAPI.createEntry({ sentence })
      setResult(res.data)
      setSentence('')
      // Refresh both report and recent list
      fetchReport()
      loadRecent()
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to create entry')
    } finally {
      setCreating(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto p-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Welcome back{user?.username ? `, ${user.username}` : ''}!
          </h1>
          <p className="text-gray-600">Track your learning journey and monitor your progress.</p>
        </div>

        {/* Quick Entry Card */}
        <div className="bg-white rounded-lg shadow-md border p-6 mb-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">📝 Quick Study Entry</h2>
          
          <form onSubmit={submitEntry} className="space-y-4">
            <textarea
              className="w-full p-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
              rows="3"
              placeholder="e.g., Struggled with Docker containers; learned quicksort algorithm; practiced React hooks"
              value={sentence}
              onChange={(e) => setSentence(e.target.value)}
            />
            
            <div className="flex justify-between items-center">
              <button
                type="submit"
                disabled={creating || !sentence.trim()}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                {creating ? 'Saving...' : '✅ Save Entry'}
              </button>
              
              {sentence.trim() && (
                <span className="text-sm text-gray-500">
                  {sentence.length} characters
                </span>
              )}
            </div>

            {error && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
                {error}
              </div>
            )}

            {result && (
              <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
                <p className="text-green-800 font-medium mb-2">✅ Entry saved successfully!</p>
                {result.parsedTopics?.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {result.parsedTopics.map((t, idx) => {
                      const c = subjectColor(t.subject || 'Other')
                      return (
                        <div key={idx} className="flex items-center gap-2">
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${c.bg} ${c.text}`}>
                            {t.subject || 'Other'}
                          </span>
                          <span className="px-2 py-1 rounded-full text-xs bg-gray-100 text-gray-800">
                            {t.name}
                          </span>
                          {t.isPriority && (
                            <span className="px-2 py-1 rounded-full text-xs font-bold bg-red-100 text-red-800">
                              ⚠️ PRIORITY
                            </span>
                          )}
                        </div>
                      )
                    })}
                  </div>
                )}
              </div>
            )}
          </form>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Weekly Report */}
          <div className="bg-white rounded-lg shadow-md border p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold text-gray-900">📊 This Week</h2>
              <button
                onClick={fetchReport}
                disabled={loadingReport}
                className="px-3 py-1 text-sm text-blue-600 hover:text-blue-800 border border-blue-200 rounded hover:bg-blue-50"
              >
                {loadingReport ? '🔄 Loading...' : '🔄 Refresh'}
              </button>
            </div>

            {loadingReport ? (
              <div className="text-center py-8 text-gray-600">Loading report...</div>
            ) : report?.subjects?.length ? (
              <div className="space-y-4">
                {report.reportWeek && (
                  <p className="text-sm text-gray-600 mb-4">
                    📅 {report.reportWeek.startDate} → {report.reportWeek.endDate}
                  </p>
                )}
                
                {report.subjects.map((subject, i) => {
                  const c = subjectColor(subject.subject)
                  return (
                    <div key={i} className="border border-gray-200 rounded-lg p-4">
                      <div className="flex justify-between items-center mb-3">
                        <span className={`px-3 py-1 rounded-full text-sm font-medium ${c.bg} ${c.text}`}>
                          {subject.subject}
                        </span>
                        <span className="text-sm text-gray-500">
                          {subject.totalStudies} entries
                        </span>
                      </div>
                      
                      <div className="space-y-2">
                        {subject.topics?.map((topic, j) => (
                          <div key={j} className="flex justify-between items-center text-sm">
                            <div className="flex items-center gap-2">
                              <span className="px-2 py-1 rounded bg-gray-100 text-gray-800 text-xs">
                                {topic.name}
                              </span>
                              <span className="text-gray-600">× {topic.count}</span>
                              {topic.isPriority && (
                                <span className="px-2 py-1 rounded bg-red-100 text-red-800 text-xs font-bold">
                                  ⚠️ PRIORITY
                                </span>
                              )}
                            </div>
                            <span className="text-xs text-gray-500">
                              {topic.lastStudiedAt ? new Date(topic.lastStudiedAt).toLocaleDateString() : '-'}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )
                })}
              </div>
            ) : (
              <div className="text-center py-8">
                <p className="text-gray-600 text-lg mb-2">📚 No entries this week yet</p>
                <p className="text-gray-500">Create your first entry above!</p>
              </div>
            )}
          </div>

          {/* Recent Entries */}
          <div className="bg-white rounded-lg shadow-md border p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold text-gray-900">🕐 Recent Entries</h2>
              <button
                onClick={loadRecent}
                disabled={loadingRecent}
                className="px-3 py-1 text-sm text-blue-600 hover:text-blue-800 border border-blue-200 rounded hover:bg-blue-50"
              >
                {loadingRecent ? '🔄 Loading...' : '🔄 Refresh'}
              </button>
            </div>

            {loadingRecent ? (
              <div className="text-center py-8 text-gray-600">Loading entries...</div>
            ) : recent.length ? (
              <div className="space-y-3 max-h-96 overflow-y-auto">
                {recent.map((item) => (
                  <div key={item.id} className="border border-gray-200 rounded-lg p-3 hover:bg-gray-50">
                    <div className="flex justify-between mb-2">
                      <p className="text-sm text-gray-900 flex-1 mr-4">{item.originalSentence}</p>
                      <span className="text-xs text-gray-500">
                        {item.studiedAt ? new Date(item.studiedAt).toLocaleDateString() : '-'}
                      </span>
                    </div>
                    
                    {item.topics?.length > 0 && (
                      <div className="flex flex-wrap gap-1">
                        {item.topics.map((topic, i) => {
                          const c = subjectColor('Other') // simplify for now
                          return (
                            <span key={i} className={`px-2 py-0.5 rounded text-xs ${c.bg} ${c.text}`}>
                              {topic.name} {topic.isPriority ? '⚠️' : ''}
                            </span>
                          )
                        })}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <p className="text-gray-600 text-lg mb-2">📝 No recent entries</p>
                <p className="text-gray-500">Your learning history will appear here</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
