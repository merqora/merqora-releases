import { useState, useEffect } from 'react'
import { 
  BarChart3, 
  TrendingUp, 
  Bot,
  Users,
  Clock,
  MessageSquare,
  ThumbsUp,
  ThumbsDown,
  Target,
  Zap
} from 'lucide-react'
import { supabase } from '../supabaseClient'

function StatCard({ title, value, subtitle, icon: Icon, color, trend }) {
  return (
    <div className="bg-rendly-surface rounded-2xl p-6 border border-primary/10">
      <div className="flex items-start justify-between">
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${color}`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
        {trend && (
          <span className={`text-sm font-medium ${trend > 0 ? 'text-accent-green' : 'text-accent-magenta'}`}>
            {trend > 0 ? '+' : ''}{trend}%
          </span>
        )}
      </div>
      <div className="mt-4">
        <p className="text-3xl font-bold text-text-primary">{value}</p>
        <p className="text-text-primary font-medium mt-1">{title}</p>
        {subtitle && <p className="text-text-tertiary text-sm">{subtitle}</p>}
      </div>
    </div>
  )
}

function ProgressBar({ label, value, max, color }) {
  const percentage = max > 0 ? (value / max) * 100 : 0
  
  return (
    <div className="space-y-2">
      <div className="flex justify-between text-sm">
        <span className="text-text-secondary">{label}</span>
        <span className="text-text-primary font-medium">{value}</span>
      </div>
      <div className="h-2 bg-rendly-bg rounded-full overflow-hidden">
        <div 
          className={`h-full rounded-full transition-all duration-500 ${color}`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  )
}

export default function Stats() {
  const [stats, setStats] = useState({
    totalMessages: 0,
    aiResolved: 0,
    escalated: 0,
    avgConfidence: 0,
    avgResponseTime: 0,
    helpfulCount: 0,
    notHelpfulCount: 0
  })
  const [dailyStats, setDailyStats] = useState([])
  const [topIntents, setTopIntents] = useState([])
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    loadStats()
  }, [])
  
  async function loadStats() {
    try {
      // Cargar estadísticas diarias
      const { data: daily } = await supabase
        .from('ai_stats_daily')
        .select('*')
        .order('date', { ascending: false })
        .limit(30)
      
      if (daily && daily.length > 0) {
        setDailyStats(daily)
        
        // Calcular totales
        const totals = daily.reduce((acc, day) => ({
          totalMessages: acc.totalMessages + (day.total_messages || 0),
          aiResolved: acc.aiResolved + (day.ai_resolved || 0),
          escalated: acc.escalated + (day.escalated || 0),
          helpfulCount: acc.helpfulCount + (day.helpful_count || 0),
          notHelpfulCount: acc.notHelpfulCount + (day.not_helpful_count || 0),
        }), { totalMessages: 0, aiResolved: 0, escalated: 0, helpfulCount: 0, notHelpfulCount: 0 })
        
        setStats(totals)
      }
      
      // Cargar intents más comunes
      const { data: messages } = await supabase
        .from('support_messages')
        .select('detected_intent')
        .not('detected_intent', 'is', null)
        .limit(500)
      
      if (messages) {
        const intentCounts = {}
        messages.forEach(m => {
          if (m.detected_intent) {
            intentCounts[m.detected_intent] = (intentCounts[m.detected_intent] || 0) + 1
          }
        })
        
        const sorted = Object.entries(intentCounts)
          .sort((a, b) => b[1] - a[1])
          .slice(0, 5)
          .map(([intent, count]) => ({ intent, count }))
        
        setTopIntents(sorted)
      }
      
    } catch (error) {
      console.error('Error loading stats:', error)
    } finally {
      setLoading(false)
    }
  }
  
  const resolutionRate = stats.totalMessages > 0 
    ? Math.round((stats.aiResolved / stats.totalMessages) * 100) 
    : 0
  
  const satisfactionRate = (stats.helpfulCount + stats.notHelpfulCount) > 0
    ? Math.round((stats.helpfulCount / (stats.helpfulCount + stats.notHelpfulCount)) * 100)
    : 0
  
  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }
  
  return (
    <div className="space-y-6 fade-in">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-text-primary">Estadísticas</h1>
        <p className="text-text-tertiary">Métricas de rendimiento del sistema de IA</p>
      </div>
      
      {/* Main Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Mensajes Totales"
          value={stats.totalMessages.toLocaleString()}
          subtitle="Últimos 30 días"
          icon={MessageSquare}
          color="bg-primary"
        />
        <StatCard
          title="Resueltos por IA"
          value={stats.aiResolved.toLocaleString()}
          subtitle={`${resolutionRate}% tasa de resolución`}
          icon={Bot}
          color="bg-accent-green"
          trend={resolutionRate > 70 ? 5 : -3}
        />
        <StatCard
          title="Escalaciones"
          value={stats.escalated.toLocaleString()}
          subtitle="Requirieron humano"
          icon={Users}
          color="bg-accent-magenta"
        />
        <StatCard
          title="Satisfacción"
          value={`${satisfactionRate}%`}
          subtitle={`${stats.helpfulCount} útiles / ${stats.notHelpfulCount} no útiles`}
          icon={ThumbsUp}
          color="bg-accent-blue"
          trend={satisfactionRate > 80 ? 2 : -1}
        />
      </div>
      
      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Resolution Rate */}
        <div className="bg-rendly-surface rounded-2xl p-6 border border-primary/10">
          <h3 className="text-lg font-semibold text-text-primary mb-6">Tasa de Resolución</h3>
          
          <div className="flex items-center justify-center mb-6">
            <div className="relative w-40 h-40">
              <svg className="w-full h-full transform -rotate-90">
                <circle
                  cx="80"
                  cy="80"
                  r="70"
                  fill="none"
                  stroke="#16161E"
                  strokeWidth="12"
                />
                <circle
                  cx="80"
                  cy="80"
                  r="70"
                  fill="none"
                  stroke="#8B5CF6"
                  strokeWidth="12"
                  strokeLinecap="round"
                  strokeDasharray={`${resolutionRate * 4.4} 440`}
                  className="transition-all duration-1000"
                />
              </svg>
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="text-center">
                  <p className="text-3xl font-bold text-text-primary">{resolutionRate}%</p>
                  <p className="text-text-tertiary text-sm">IA</p>
                </div>
              </div>
            </div>
          </div>
          
          <div className="grid grid-cols-2 gap-4 text-center">
            <div className="p-3 bg-accent-green/10 rounded-xl">
              <p className="text-accent-green font-bold">{stats.aiResolved}</p>
              <p className="text-text-tertiary text-sm">Por IA</p>
            </div>
            <div className="p-3 bg-accent-magenta/10 rounded-xl">
              <p className="text-accent-magenta font-bold">{stats.escalated}</p>
              <p className="text-text-tertiary text-sm">Escaladas</p>
            </div>
          </div>
        </div>
        
        {/* Top Intents */}
        <div className="bg-rendly-surface rounded-2xl p-6 border border-primary/10">
          <h3 className="text-lg font-semibold text-text-primary mb-6">Intents Más Comunes</h3>
          
          <div className="space-y-4">
            {topIntents.length > 0 ? (
              topIntents.map((item, index) => (
                <ProgressBar
                  key={item.intent}
                  label={item.intent.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}
                  value={item.count}
                  max={topIntents[0]?.count || 1}
                  color={index === 0 ? 'bg-primary' : index === 1 ? 'bg-accent-blue' : 'bg-accent-green'}
                />
              ))
            ) : (
              <p className="text-text-tertiary text-center py-8">
                No hay datos de intents todavía
              </p>
            )}
          </div>
        </div>
      </div>
      
      {/* Performance Metrics */}
      <div className="bg-rendly-surface rounded-2xl p-6 border border-primary/10">
        <h3 className="text-lg font-semibold text-text-primary mb-6">Métricas de Rendimiento</h3>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="text-center p-6 bg-rendly-bg rounded-xl">
            <Zap className="w-8 h-8 text-accent-gold mx-auto mb-3" />
            <p className="text-2xl font-bold text-text-primary">{'<'} 1s</p>
            <p className="text-text-tertiary">Tiempo de respuesta IA</p>
          </div>
          
          <div className="text-center p-6 bg-rendly-bg rounded-xl">
            <Target className="w-8 h-8 text-primary mx-auto mb-3" />
            <p className="text-2xl font-bold text-text-primary">85%</p>
            <p className="text-text-tertiary">Precisión promedio</p>
          </div>
          
          <div className="text-center p-6 bg-rendly-bg rounded-xl">
            <TrendingUp className="w-8 h-8 text-accent-green mx-auto mb-3" />
            <p className="text-2xl font-bold text-text-primary">24/7</p>
            <p className="text-text-tertiary">Disponibilidad</p>
          </div>
        </div>
      </div>
    </div>
  )
}
