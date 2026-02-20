import { useState, useEffect, useCallback } from 'react'
import { supabase } from '../supabaseClient'
import {
  Brain, TrendingUp, Target, AlertTriangle, CheckCircle, XCircle,
  Play, Download, RefreshCw, Eye, Edit3, ChevronDown, ChevronUp,
  BarChart3, Zap, Clock, Users, FileText, Filter, Search,
  ArrowUpRight, ArrowDownRight, Minus, Activity, Database
} from 'lucide-react'

const API_BASE = 'https://merqora-releases-production.up.railway.app'

export default function AITrainingDashboard() {
  const [activeTab, setActiveTab] = useState('metrics')
  const [loading, setLoading] = useState(true)
  const [metrics, setMetrics] = useState(null)
  const [pendingReview, setPendingReview] = useState([])
  const [errors, setErrors] = useState([])
  const [trainingRuns, setTrainingRuns] = useState([])
  const [intentPerformance, setIntentPerformance] = useState([])
  const [selectedItem, setSelectedItem] = useState(null)
  const [correctionModal, setCorrectionModal] = useState(null)
  const [trainingInProgress, setTrainingInProgress] = useState(false)
  const [testMessage, setTestMessage] = useState('')
  const [testResult, setTestResult] = useState(null)

  useEffect(() => {
    loadData()
    const interval = setInterval(loadData, 60000)
    return () => clearInterval(interval)
  }, [])

  async function loadData() {
    setLoading(true)
    try {
      await Promise.all([
        loadMetrics(),
        loadPendingReview(),
        loadErrors(),
        loadTrainingRuns(),
        loadIntentPerformance(),
      ])
    } catch (e) {
      console.error('Error loading data:', e)
    } finally {
      setLoading(false)
    }
  }

  async function loadMetrics() {
    try {
      const res = await fetch(`${API_BASE}/ai/training/metrics?hours=24`)
      if (res.ok) {
        const data = await res.json()
        setMetrics(data)
      }
    } catch (e) { console.warn('Metrics load error:', e) }
  }

  async function loadPendingReview() {
    try {
      const { data } = await supabase
        .from('ai_training_data')
        .select('*')
        .eq('dataset_status', 'pending')
        .order('confidence_score', { ascending: true })
        .order('created_at', { ascending: false })
        .limit(100)
      setPendingReview(data || [])
    } catch (e) { console.warn('Pending review load error:', e) }
  }

  async function loadErrors() {
    try {
      const { data } = await supabase
        .from('ai_training_data')
        .select('*')
        .eq('intent_correct', false)
        .order('created_at', { ascending: false })
        .limit(100)
      setErrors(data || [])
    } catch (e) { console.warn('Errors load error:', e) }
  }

  async function loadTrainingRuns() {
    try {
      const { data } = await supabase
        .from('ai_training_runs')
        .select('*')
        .order('created_at', { ascending: false })
        .limit(20)
      setTrainingRuns(data || [])
    } catch (e) { console.warn('Training runs load error:', e) }
  }

  async function loadIntentPerformance() {
    try {
      const { data } = await supabase
        .from('v_intent_performance')
        .select('*')
      setIntentPerformance(data || [])
    } catch (e) { console.warn('Intent performance load error:', e) }
  }

  async function triggerTraining() {
    if (trainingInProgress) return
    setTrainingInProgress(true)
    try {
      const res = await fetch(`${API_BASE}/ai/training/run`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ run_type: 'full' })
      })
      const data = await res.json()
      alert(`Entrenamiento ${data.status}!\nAccuracy: ${(data.final_metrics?.accuracy * 100)?.toFixed(1)}%\nF1: ${(data.final_metrics?.f1 * 100)?.toFixed(1)}%\nSamples: ${data.training_samples}`)
      loadData()
    } catch (e) {
      alert('Error: ' + e.message)
    } finally {
      setTrainingInProgress(false)
    }
  }

  async function flushBuffer() {
    try {
      const res = await fetch(`${API_BASE}/ai/training/flush`, { method: 'POST' })
      const data = await res.json()
      alert(`Flushed ${data.flushed} records to Supabase`)
      loadData()
    } catch (e) { alert('Error: ' + e.message) }
  }

  async function submitCorrection(item, correctIntent, correctResponse, notes) {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      const { error } = await supabase.rpc('submit_intent_correction', {
        p_training_data_id: item.id,
        p_correct_intent: correctIntent,
        p_correct_response: correctResponse || null,
        p_should_escalate: false,
        p_notes: notes || null,
        p_corrector_id: user?.id || null,
      })
      if (error) throw error
      setCorrectionModal(null)
      loadData()
    } catch (e) {
      alert('Error: ' + e.message)
    }
  }

  async function markAsCorrect(item) {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      await supabase.rpc('submit_intent_correction', {
        p_training_data_id: item.id,
        p_correct_intent: item.detected_intent,
        p_correct_response: null,
        p_should_escalate: false,
        p_notes: 'Marked as correct by reviewer',
        p_corrector_id: user?.id || null,
      })
      loadData()
    } catch (e) { alert('Error: ' + e.message) }
  }

  async function testPredict() {
    if (!testMessage.trim()) return
    try {
      const res = await fetch(`${API_BASE}/ai/training/predict?message=${encodeURIComponent(testMessage)}`)
      const data = await res.json()
      setTestResult(data)
    } catch (e) { alert('Error: ' + e.message) }
  }

  const tabs = [
    { id: 'metrics', label: 'Métricas', icon: BarChart3 },
    { id: 'review', label: `Revisión (${pendingReview.length})`, icon: Eye },
    { id: 'errors', label: `Errores (${errors.length})`, icon: AlertTriangle },
    { id: 'intents', label: 'Intents', icon: Target },
    { id: 'training', label: 'Entrenamientos', icon: Zap },
    { id: 'test', label: 'Test', icon: Brain },
  ]

  const lm = metrics?.live_metrics || {}
  const ps = metrics?.pipeline_stats || {}

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <Brain className="w-7 h-7 text-purple-400" />
            Training Pipeline
          </h1>
          <p className="text-sm text-gray-400 mt-1">
            Sistema de aprendizaje continuo con ML real
          </p>
        </div>
        <div className="flex gap-2">
          <button onClick={flushBuffer} className="px-3 py-2 bg-blue-600/20 text-blue-400 rounded-lg text-sm hover:bg-blue-600/30 flex items-center gap-1">
            <Database className="w-4 h-4" /> Flush Buffer
          </button>
          <button onClick={loadData} className="px-3 py-2 bg-gray-700 text-gray-300 rounded-lg text-sm hover:bg-gray-600 flex items-center gap-1">
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </button>
          <button
            onClick={triggerTraining}
            disabled={trainingInProgress}
            className="px-4 py-2 bg-purple-600 text-white rounded-lg text-sm hover:bg-purple-500 disabled:opacity-50 flex items-center gap-2"
          >
            {trainingInProgress ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Play className="w-4 h-4" />}
            {trainingInProgress ? 'Entrenando...' : 'Entrenar Modelo'}
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-gray-800/50 rounded-xl p-1">
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              activeTab === tab.id
                ? 'bg-purple-600 text-white shadow-lg'
                : 'text-gray-400 hover:text-white hover:bg-gray-700/50'
            }`}
          >
            <tab.icon className="w-4 h-4" />
            {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'metrics' && <MetricsTab lm={lm} ps={ps} />}
      {activeTab === 'review' && (
        <ReviewTab
          items={pendingReview}
          onCorrect={markAsCorrect}
          onEdit={setCorrectionModal}
        />
      )}
      {activeTab === 'errors' && <ErrorsTab items={errors} onEdit={setCorrectionModal} />}
      {activeTab === 'intents' && <IntentsTab data={intentPerformance} />}
      {activeTab === 'training' && <TrainingRunsTab runs={trainingRuns} />}
      {activeTab === 'test' && (
        <TestTab
          message={testMessage}
          setMessage={setTestMessage}
          result={testResult}
          onTest={testPredict}
        />
      )}

      {/* Correction Modal */}
      {correctionModal && (
        <CorrectionModal
          item={correctionModal}
          onSubmit={submitCorrection}
          onClose={() => setCorrectionModal(null)}
        />
      )}
    </div>
  )
}

/* ═══════════ METRICS TAB ═══════════ */
function MetricsTab({ lm, ps }) {
  const cards = [
    { label: 'Mensajes (24h)', value: lm.total_messages || 0, icon: MessageSquare, color: 'blue' },
    { label: 'IA Resueltos', value: lm.ai_resolved || 0, icon: CheckCircle, color: 'green' },
    { label: 'Escalados', value: lm.escalated || 0, icon: AlertTriangle, color: 'yellow' },
    { label: 'Tasa Escalación', value: `${lm.escalation_rate || 0}%`, icon: TrendingUp, color: 'red' },
    { label: 'Confianza Prom.', value: `${lm.avg_confidence || 0}%`, icon: Target, color: 'purple' },
    { label: 'Modelo Entrenado', value: ps.model_trained ? 'Sí' : 'No', icon: Brain, color: ps.model_trained ? 'green' : 'gray' },
    { label: 'Intents Modelo', value: ps.model_intents || 0, icon: Database, color: 'indigo' },
    { label: 'Desde Último Train', value: ps.samples_since_last_train || 0, icon: Activity, color: 'orange' },
  ]

  const MessageSquare = ({ className }) => <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>

  return (
    <div className="space-y-6">
      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {cards.map((card, i) => (
          <div key={i} className="bg-gray-800/50 rounded-xl p-4 border border-gray-700/50">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs text-gray-400">{card.label}</span>
              <card.icon className={`w-4 h-4 text-${card.color}-400`} />
            </div>
            <div className="text-2xl font-bold text-white">{card.value}</div>
          </div>
        ))}
      </div>

      {/* Confidence Distribution */}
      {lm.confidence_distribution && (
        <div className="bg-gray-800/50 rounded-xl p-6 border border-gray-700/50">
          <h3 className="text-lg font-semibold text-white mb-4">Distribución de Confianza</h3>
          <div className="flex gap-2 items-end h-32">
            {Object.entries(lm.confidence_distribution).map(([range, count]) => {
              const max = Math.max(...Object.values(lm.confidence_distribution), 1)
              const height = (count / max) * 100
              const colors = {
                '0-20': 'bg-red-500', '21-40': 'bg-orange-500',
                '41-60': 'bg-yellow-500', '61-80': 'bg-blue-500', '81-100': 'bg-green-500'
              }
              return (
                <div key={range} className="flex-1 flex flex-col items-center gap-1">
                  <span className="text-xs text-gray-400">{count}</span>
                  <div className={`w-full rounded-t ${colors[range] || 'bg-gray-500'}`} style={{ height: `${Math.max(height, 4)}%` }} />
                  <span className="text-[10px] text-gray-500">{range}</span>
                </div>
              )
            })}
          </div>
        </div>
      )}

      {/* Intent Distribution */}
      {lm.intent_distribution && Object.keys(lm.intent_distribution).length > 0 && (
        <div className="bg-gray-800/50 rounded-xl p-6 border border-gray-700/50">
          <h3 className="text-lg font-semibold text-white mb-4">Top Intents (24h)</h3>
          <div className="space-y-2">
            {Object.entries(lm.intent_distribution).slice(0, 15).map(([intent, count]) => {
              const max = Math.max(...Object.values(lm.intent_distribution), 1)
              return (
                <div key={intent} className="flex items-center gap-3">
                  <span className="text-xs text-gray-400 w-40 truncate">{intent}</span>
                  <div className="flex-1 bg-gray-700 rounded-full h-2">
                    <div className="bg-purple-500 rounded-full h-2" style={{ width: `${(count / max) * 100}%` }} />
                  </div>
                  <span className="text-xs text-gray-300 w-8 text-right">{count}</span>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}

/* ═══════════ REVIEW TAB ═══════════ */
function ReviewTab({ items, onCorrect, onEdit }) {
  if (items.length === 0) {
    return (
      <div className="bg-gray-800/50 rounded-xl p-12 text-center border border-gray-700/50">
        <CheckCircle className="w-12 h-12 text-green-400 mx-auto mb-3" />
        <p className="text-gray-300 text-lg">No hay items pendientes de revisión</p>
        <p className="text-gray-500 text-sm mt-1">Los datos nuevos aparecerán aquí</p>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      <p className="text-sm text-gray-400">
        {items.length} muestras pendientes de revisión. Ordenadas por menor confianza primero.
      </p>
      {items.map(item => (
        <TrainingDataCard
          key={item.id}
          item={item}
          onCorrect={() => onCorrect(item)}
          onEdit={() => onEdit(item)}
        />
      ))}
    </div>
  )
}

/* ═══════════ ERRORS TAB ═══════════ */
function ErrorsTab({ items, onEdit }) {
  if (items.length === 0) {
    return (
      <div className="bg-gray-800/50 rounded-xl p-12 text-center border border-gray-700/50">
        <CheckCircle className="w-12 h-12 text-green-400 mx-auto mb-3" />
        <p className="text-gray-300 text-lg">No hay errores de clasificación detectados</p>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      <p className="text-sm text-gray-400">
        {items.length} errores de clasificación (detected_intent ≠ correct_intent)
      </p>
      {items.map(item => (
        <div key={item.id} className="bg-gray-800/50 rounded-xl p-4 border border-red-500/20">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1 min-w-0">
              <p className="text-sm text-gray-200 mb-2">"{item.user_message?.slice(0, 150)}"</p>
              <div className="flex flex-wrap gap-2 text-xs">
                <span className="px-2 py-1 bg-red-500/20 text-red-300 rounded">
                  IA dijo: {item.detected_intent}
                </span>
                <span className="text-gray-500">→</span>
                <span className="px-2 py-1 bg-green-500/20 text-green-300 rounded">
                  Correcto: {item.correct_intent}
                </span>
                <span className="px-2 py-1 bg-gray-700 text-gray-300 rounded">
                  Conf: {item.confidence_score}%
                </span>
              </div>
            </div>
            <button onClick={() => onEdit(item)} className="px-3 py-1 bg-gray-700 text-gray-300 rounded text-xs hover:bg-gray-600">
              <Edit3 className="w-3 h-3" />
            </button>
          </div>
        </div>
      ))}
    </div>
  )
}

/* ═══════════ INTENTS TAB ═══════════ */
function IntentsTab({ data }) {
  if (data.length === 0) {
    return (
      <div className="bg-gray-800/50 rounded-xl p-12 text-center border border-gray-700/50">
        <Target className="w-12 h-12 text-gray-500 mx-auto mb-3" />
        <p className="text-gray-300">No hay datos de rendimiento por intent aún</p>
      </div>
    )
  }

  return (
    <div className="bg-gray-800/50 rounded-xl border border-gray-700/50 overflow-hidden">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-gray-700">
            <th className="text-left p-3 text-gray-400 font-medium">Intent</th>
            <th className="text-center p-3 text-gray-400 font-medium">Muestras</th>
            <th className="text-center p-3 text-gray-400 font-medium">Correctas</th>
            <th className="text-center p-3 text-gray-400 font-medium">Errores</th>
            <th className="text-center p-3 text-gray-400 font-medium">Accuracy</th>
            <th className="text-center p-3 text-gray-400 font-medium">Confianza</th>
            <th className="text-center p-3 text-gray-400 font-medium">Correcciones</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={i} className="border-b border-gray-700/50 hover:bg-gray-700/20">
              <td className="p-3 text-white font-mono text-xs">{row.intent}</td>
              <td className="p-3 text-center text-gray-300">{row.total_samples}</td>
              <td className="p-3 text-center text-green-400">{row.correct_classifications || 0}</td>
              <td className="p-3 text-center text-red-400">{row.misclassifications || 0}</td>
              <td className="p-3 text-center">
                <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                  (row.accuracy || 0) >= 90 ? 'bg-green-500/20 text-green-300' :
                  (row.accuracy || 0) >= 70 ? 'bg-yellow-500/20 text-yellow-300' :
                  'bg-red-500/20 text-red-300'
                }`}>
                  {row.accuracy || '-'}%
                </span>
              </td>
              <td className="p-3 text-center text-gray-300">{row.avg_confidence || '-'}%</td>
              <td className="p-3 text-center text-purple-400">{row.human_corrections || 0}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

/* ═══════════ TRAINING RUNS TAB ═══════════ */
function TrainingRunsTab({ runs }) {
  if (runs.length === 0) {
    return (
      <div className="bg-gray-800/50 rounded-xl p-12 text-center border border-gray-700/50">
        <Zap className="w-12 h-12 text-gray-500 mx-auto mb-3" />
        <p className="text-gray-300">No hay entrenamientos registrados</p>
        <p className="text-gray-500 text-sm mt-1">Ejecuta el primer entrenamiento con el botón de arriba</p>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {runs.map(run => (
        <div key={run.id} className="bg-gray-800/50 rounded-xl p-4 border border-gray-700/50">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-3">
              <span className={`w-2 h-2 rounded-full ${
                run.status === 'completed' ? 'bg-green-400' :
                run.status === 'running' ? 'bg-yellow-400 animate-pulse' :
                'bg-red-400'
              }`} />
              <span className="text-white font-medium">{run.run_name}</span>
              <span className="text-xs text-gray-500 px-2 py-0.5 bg-gray-700 rounded">{run.run_type}</span>
            </div>
            <span className="text-xs text-gray-500">{new Date(run.created_at).toLocaleString()}</span>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-3 text-center">
            <Stat label="Accuracy" value={run.intent_accuracy ? `${(run.intent_accuracy * 100).toFixed(1)}%` : '-'} />
            <Stat label="F1 Score" value={run.intent_f1_score ? `${(run.intent_f1_score * 100).toFixed(1)}%` : '-'} />
            <Stat label="Precision" value={run.intent_precision ? `${(run.intent_precision * 100).toFixed(1)}%` : '-'} />
            <Stat label="Train Samples" value={run.training_samples || 0} />
            <Stat label="Intents" value={run.total_intents || 0} />
          </div>
        </div>
      ))}
    </div>
  )
}

function Stat({ label, value }) {
  return (
    <div>
      <div className="text-xs text-gray-500">{label}</div>
      <div className="text-lg font-semibold text-white">{value}</div>
    </div>
  )
}

/* ═══════════ TEST TAB ═══════════ */
function TestTab({ message, setMessage, result, onTest }) {
  return (
    <div className="space-y-4">
      <div className="bg-gray-800/50 rounded-xl p-6 border border-gray-700/50">
        <h3 className="text-lg font-semibold text-white mb-4">Probar Clasificador ML</h3>
        <div className="flex gap-3">
          <input
            type="text"
            value={message}
            onChange={e => setMessage(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && onTest()}
            placeholder="Escribe un mensaje de prueba..."
            className="flex-1 bg-gray-700 text-white rounded-lg px-4 py-3 border border-gray-600 focus:border-purple-500 focus:outline-none"
          />
          <button onClick={onTest} className="px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-500 font-medium">
            Predecir
          </button>
        </div>
      </div>

      {result && (
        <div className="bg-gray-800/50 rounded-xl p-6 border border-gray-700/50">
          <h3 className="text-lg font-semibold text-white mb-4">Resultado</h3>
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div>
              <div className="text-xs text-gray-500">Intent Predicho</div>
              <div className="text-xl font-bold text-purple-400">{result.predicted_intent}</div>
            </div>
            <div>
              <div className="text-xs text-gray-500">Confianza</div>
              <div className="text-xl font-bold text-white">{(result.confidence * 100).toFixed(1)}%</div>
            </div>
          </div>
          {result.top_intents && Object.keys(result.top_intents).length > 0 && (
            <div>
              <div className="text-xs text-gray-500 mb-2">Top Intents</div>
              <div className="space-y-1">
                {Object.entries(result.top_intents).map(([intent, prob]) => (
                  <div key={intent} className="flex items-center gap-2">
                    <span className="text-xs text-gray-400 w-40 truncate">{intent}</span>
                    <div className="flex-1 bg-gray-700 rounded-full h-1.5">
                      <div className="bg-purple-500 rounded-full h-1.5" style={{ width: `${prob * 100}%` }} />
                    </div>
                    <span className="text-xs text-gray-300 w-12 text-right">{(prob * 100).toFixed(1)}%</span>
                  </div>
                ))}
              </div>
            </div>
          )}
          {!result.model_trained && (
            <p className="text-yellow-400 text-sm mt-3">⚠️ El modelo ML no está entrenado. Ejecuta un entrenamiento primero.</p>
          )}
        </div>
      )}
    </div>
  )
}

/* ═══════════ TRAINING DATA CARD ═══════════ */
function TrainingDataCard({ item, onCorrect, onEdit }) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div className="bg-gray-800/50 rounded-xl p-4 border border-gray-700/50 hover:border-gray-600/50 transition-colors">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1 min-w-0">
          <p className="text-sm text-gray-200 mb-2">
            "{item.user_message?.slice(0, expanded ? 500 : 120)}"
            {!expanded && item.user_message?.length > 120 && (
              <button onClick={() => setExpanded(true)} className="text-purple-400 ml-1">más</button>
            )}
          </p>
          <div className="flex flex-wrap gap-2 text-xs">
            <span className="px-2 py-1 bg-purple-500/20 text-purple-300 rounded">
              {item.detected_intent || 'unknown'}
            </span>
            <span className={`px-2 py-1 rounded ${
              item.confidence_score >= 70 ? 'bg-green-500/20 text-green-300' :
              item.confidence_score >= 40 ? 'bg-yellow-500/20 text-yellow-300' :
              'bg-red-500/20 text-red-300'
            }`}>
              {item.confidence_score}%
            </span>
            {item.escalated && <span className="px-2 py-1 bg-red-500/20 text-red-300 rounded">Escalado</span>}
            {item.category && <span className="px-2 py-1 bg-gray-700 text-gray-400 rounded">{item.category}</span>}
            {item.response_source && <span className="px-2 py-1 bg-gray-700 text-gray-400 rounded">{item.response_source}</span>}
          </div>
        </div>
        <div className="flex gap-1">
          <button onClick={onCorrect} className="p-2 bg-green-600/20 text-green-400 rounded-lg hover:bg-green-600/30" title="Marcar como correcto">
            <CheckCircle className="w-4 h-4" />
          </button>
          <button onClick={onEdit} className="p-2 bg-yellow-600/20 text-yellow-400 rounded-lg hover:bg-yellow-600/30" title="Corregir">
            <Edit3 className="w-4 h-4" />
          </button>
        </div>
      </div>
      {expanded && item.ai_response && (
        <div className="mt-3 pt-3 border-t border-gray-700/50">
          <div className="text-xs text-gray-500 mb-1">Respuesta IA:</div>
          <p className="text-xs text-gray-400">{item.ai_response?.slice(0, 300)}</p>
        </div>
      )}
    </div>
  )
}

/* ═══════════ CORRECTION MODAL ═══════════ */
function CorrectionModal({ item, onSubmit, onClose }) {
  const [correctIntent, setCorrectIntent] = useState(item.correct_intent || item.detected_intent || '')
  const [correctResponse, setCorrectResponse] = useState(item.correct_response || '')
  const [notes, setNotes] = useState('')

  const knownIntents = [
    'purchase_status', 'purchase_cancel', 'purchase_problem', 'payment_methods',
    'payment_problem', 'refund', 'account_access', 'account_settings', 'account_delete',
    'account_verify', 'shipping_info', 'shipping_problem', 'sell_how', 'sell_payment',
    'product_manage', 'security_report', 'security_verify', 'app_bug', 'wallet_info',
    'handshake_info', 'notification_settings', 'return_process', 'stories_info',
    'rends_info', 'privacy_info', 'chat_info', 'social_info', 'interaction_info',
    'review_info', 'offer_info', 'reputation_info', 'zone_info', 'language_info',
    'livestream_info', 'seller_problem', 'highlights_info', 'escalation_request',
    'farewell', 'greeting',
  ]

  return (
    <div className="fixed inset-0 bg-black/70 z-50 flex items-center justify-center p-4" onClick={onClose}>
      <div className="bg-gray-800 rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
        <div className="p-6 border-b border-gray-700">
          <h3 className="text-lg font-semibold text-white">Corregir Clasificación</h3>
        </div>
        <div className="p-6 space-y-4">
          {/* Original message */}
          <div>
            <label className="text-xs text-gray-500 block mb-1">Mensaje del usuario</label>
            <p className="text-sm text-gray-200 bg-gray-700/50 p-3 rounded-lg">{item.user_message}</p>
          </div>

          {/* Current intent */}
          <div>
            <label className="text-xs text-gray-500 block mb-1">Intent detectado (IA)</label>
            <p className="text-sm text-red-300 bg-red-500/10 p-2 rounded">{item.detected_intent}</p>
          </div>

          {/* Correct intent */}
          <div>
            <label className="text-xs text-gray-500 block mb-1">Intent correcto</label>
            <select
              value={correctIntent}
              onChange={e => setCorrectIntent(e.target.value)}
              className="w-full bg-gray-700 text-white rounded-lg px-3 py-2 border border-gray-600 focus:border-purple-500 focus:outline-none"
            >
              <option value="">-- Seleccionar --</option>
              {knownIntents.map(i => (
                <option key={i} value={i}>{i}</option>
              ))}
            </select>
          </div>

          {/* Correct response */}
          <div>
            <label className="text-xs text-gray-500 block mb-1">Respuesta correcta (opcional)</label>
            <textarea
              value={correctResponse}
              onChange={e => setCorrectResponse(e.target.value)}
              placeholder="Deja vacío para mantener la respuesta actual"
              rows={3}
              className="w-full bg-gray-700 text-white rounded-lg px-3 py-2 border border-gray-600 focus:border-purple-500 focus:outline-none resize-none"
            />
          </div>

          {/* Notes */}
          <div>
            <label className="text-xs text-gray-500 block mb-1">Notas</label>
            <input
              type="text"
              value={notes}
              onChange={e => setNotes(e.target.value)}
              placeholder="Razón de la corrección..."
              className="w-full bg-gray-700 text-white rounded-lg px-3 py-2 border border-gray-600 focus:border-purple-500 focus:outline-none"
            />
          </div>
        </div>
        <div className="p-6 border-t border-gray-700 flex gap-3 justify-end">
          <button onClick={onClose} className="px-4 py-2 bg-gray-700 text-gray-300 rounded-lg hover:bg-gray-600">
            Cancelar
          </button>
          <button
            onClick={() => onSubmit(item, correctIntent, correctResponse || null, notes || null)}
            disabled={!correctIntent}
            className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-500 disabled:opacity-50"
          >
            Guardar Corrección
          </button>
        </div>
      </div>
    </div>
  )
}
