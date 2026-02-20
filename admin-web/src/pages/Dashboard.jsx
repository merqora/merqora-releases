import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { 
  MessageSquare, 
  Bot, 
  Users, 
  TrendingUp,
  Clock,
  CheckCircle,
  AlertCircle,
  ArrowUpRight,
  ArrowDownRight
} from 'lucide-react'
import { supabase } from '../supabaseClient'

function StatCard({ title, value, change, changeType, icon: Icon, color }) {
  return (
    <div className="bg-rendly-surface rounded-2xl p-6 border border-primary/10 hover:border-primary/30 transition-all duration-300">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-text-tertiary text-sm mb-1">{title}</p>
          <p className="text-3xl font-bold text-text-primary">{value}</p>
          {change && (
            <div className={`flex items-center gap-1 mt-2 text-sm ${changeType === 'up' ? 'text-accent-green' : 'text-accent-magenta'}`}>
              {changeType === 'up' ? <ArrowUpRight className="w-4 h-4" /> : <ArrowDownRight className="w-4 h-4" />}
              <span>{change}</span>
            </div>
          )}
        </div>
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${color}`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
    </div>
  )
}

function RecentEscalation({ escalation, onClick }) {
  const timeAgo = (date) => {
    const seconds = Math.floor((new Date() - new Date(date)) / 1000)
    if (seconds < 60) return 'Ahora'
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m`
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`
    return `${Math.floor(seconds / 86400)}d`
  }
  
  return (
    <div 
      onClick={onClick}
      className="flex items-center gap-4 p-4 bg-rendly-surface-elevated rounded-xl cursor-pointer hover:bg-primary/10 transition-all duration-200"
    >
      <div className="w-10 h-10 rounded-full bg-accent-magenta/20 flex items-center justify-center">
        <AlertCircle className="w-5 h-5 text-accent-magenta" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-text-primary font-medium truncate">
          {escalation.last_message || 'Sin mensaje'}
        </p>
        <p className="text-text-tertiary text-sm truncate">
          {escalation.reason}
        </p>
      </div>
      <div className="text-text-muted text-sm">
        {timeAgo(escalation.created_at)}
      </div>
    </div>
  )
}

export default function Dashboard() {
  const [stats, setStats] = useState({
    totalMessages: 0,
    aiResolved: 0,
    escalated: 0,
    avgResponseTime: 0
  })
  const [recentEscalations, setRecentEscalations] = useState([])
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    loadDashboardData()
  }, [])
  
  async function loadDashboardData() {
    try {
      // Cargar estadísticas
      const { data: statsData } = await supabase
        .from('ai_stats_daily')
        .select('*')
        .order('date', { ascending: false })
        .limit(7)
      
      if (statsData && statsData.length > 0) {
        const totals = statsData.reduce((acc, day) => ({
          totalMessages: acc.totalMessages + (day.total_messages || 0),
          aiResolved: acc.aiResolved + (day.ai_resolved || 0),
          escalated: acc.escalated + (day.escalated || 0),
        }), { totalMessages: 0, aiResolved: 0, escalated: 0 })
        
        setStats(totals)
      }
      
      // Cargar escalaciones pendientes
      const { data: escalations } = await supabase
        .from('ai_escalations')
        .select(`
          *,
          support_messages (content)
        `)
        .eq('status', 'pending')
        .order('created_at', { ascending: false })
        .limit(5)
      
      if (escalations) {
        setRecentEscalations(escalations.map(e => ({
          ...e,
          last_message: e.support_messages?.[0]?.content
        })))
      }
      
    } catch (error) {
      console.error('Error loading dashboard:', error)
    } finally {
      setLoading(false)
    }
  }
  
  const resolutionRate = stats.totalMessages > 0 
    ? Math.round((stats.aiResolved / stats.totalMessages) * 100) 
    : 0
  
  return (
    <div className="space-y-6 fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Dashboard</h1>
          <p className="text-text-tertiary">Resumen del sistema de soporte IA</p>
        </div>
        <div className="flex items-center gap-2 px-4 py-2 bg-accent-green/10 rounded-xl">
          <div className="w-2 h-2 rounded-full bg-accent-green animate-pulse" />
          <span className="text-sm text-accent-green font-medium">Sistema Activo</span>
        </div>
      </div>
      
      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Mensajes Totales"
          value={stats.totalMessages}
          change="+12% vs ayer"
          changeType="up"
          icon={MessageSquare}
          color="bg-primary"
        />
        <StatCard
          title="Resueltos por IA"
          value={stats.aiResolved}
          change={`${resolutionRate}% tasa`}
          changeType="up"
          icon={Bot}
          color="bg-accent-green"
        />
        <StatCard
          title="Escalaciones"
          value={stats.escalated}
          change="Pendientes"
          changeType="down"
          icon={AlertCircle}
          color="bg-accent-magenta"
        />
        <StatCard
          title="Tiempo Promedio"
          value="< 1s"
          change="Respuesta IA"
          changeType="up"
          icon={Clock}
          color="bg-accent-blue"
        />
      </div>
      
      {/* Recent Escalations */}
      <div className="bg-rendly-surface rounded-2xl border border-primary/10 overflow-hidden">
        <div className="flex items-center justify-between p-6 border-b border-primary/10">
          <h2 className="text-lg font-semibold text-text-primary">Escalaciones Recientes</h2>
          <Link 
            to="/escalations"
            className="text-primary hover:text-primary-bright transition-colors text-sm font-medium"
          >
            Ver todas →
          </Link>
        </div>
        
        <div className="p-4 space-y-3">
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
            </div>
          ) : recentEscalations.length > 0 ? (
            recentEscalations.map((escalation) => (
              <Link key={escalation.id} to={`/chat/${escalation.conversation_id}`}>
                <RecentEscalation escalation={escalation} />
              </Link>
            ))
          ) : (
            <div className="text-center py-8 text-text-tertiary">
              <CheckCircle className="w-12 h-12 mx-auto mb-3 text-accent-green" />
              <p>¡No hay escalaciones pendientes!</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
