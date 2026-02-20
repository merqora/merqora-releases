import { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Link, useLocation, Navigate } from 'react-router-dom'
import {
  MessageSquare, BarChart3, Bell, Bot, TrendingUp,
  AlertCircle, Menu, X, MessageCircle, Bug, Brain,
  LogOut, BadgeCheck, Grip, Package, ShoppingBag,
  ShieldAlert, ArrowLeft
} from 'lucide-react'
import { supabase } from './supabaseClient'

// Pages — Admin
import Dashboard from './pages/Dashboard'
import Escalations from './pages/Escalations'
import Conversations from './pages/Conversations'
import ChatView from './pages/ChatView'
import Stats from './pages/Stats'
import Feedback from './pages/Feedback'
import BugReports from './pages/BugReports'
import AILearning from './pages/AILearning'
import AITrainingDashboard from './pages/AITrainingDashboard'
import Verification from './pages/Verification'
import Login from './pages/Login'
import HandshakeTest from './pages/HandshakeTest'
import NotificationTest from './pages/NotificationTest'
import ChatTest from './pages/ChatTest'
import ContentReports from './pages/ContentReports'
import AdminAppManager from './pages/AdminAppManager'

/* ═══════════ Admin Sidebar ═══════════ */
function Sidebar({ isOpen, setIsOpen }) {
  const location = useLocation()

  const navItems = [
    { path: '/admin', icon: BarChart3, label: 'Dashboard' },
    { path: '/admin/app-manager', icon: Package, label: '📱 App Manager' },
    { path: '/admin/handshake-test', icon: Grip, label: '🧪 Test Handshake' },
    { path: '/admin/notification-test', icon: Bell, label: '🔔 Test Notificaciones' },
    { path: '/admin/chat-test', icon: MessageSquare, label: '💬 Test Chat' },
    { path: '/admin/verification', icon: BadgeCheck, label: 'Verificación' },
    { path: '/admin/escalations', icon: AlertCircle, label: 'Escalaciones' },
    { path: '/admin/conversations', icon: MessageSquare, label: 'Conversaciones' },
    { path: '/admin/ai-learning', icon: Brain, label: 'Aprendizaje IA' },
    { path: '/admin/training-pipeline', icon: TrendingUp, label: 'Training Pipeline' },
    { path: '/admin/feedback', icon: MessageCircle, label: 'Feedback' },
    { path: '/admin/bug-reports', icon: Bug, label: 'Bug Reports' },
    { path: '/admin/content-reports', icon: AlertCircle, label: '🚨 Reportes' },
    { path: '/admin/stats', icon: TrendingUp, label: 'Estadísticas' },
  ]

  return (
    <>
      {isOpen && (
        <div className="fixed inset-0 bg-black/50 z-40 lg:hidden" onClick={() => setIsOpen(false)} />
      )}
      <aside className={`
        fixed top-0 left-0 h-full w-64 bg-rendly-surface border-r border-primary/10 z-50
        transform transition-transform duration-300 ease-in-out flex flex-col
        ${isOpen ? 'translate-x-0' : '-translate-x-full'} lg:translate-x-0 lg:static
      `}>
        <div className="flex-shrink-0 p-6 border-b border-primary/10">
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-10 h-10 rounded-xl gradient-rendly flex items-center justify-center group-hover:scale-105 transition-transform">
              <ShoppingBag className="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-text-primary">Merqora</h1>
              <p className="text-xs text-text-tertiary">Admin Panel</p>
            </div>
          </Link>
        </div>

        <nav className="flex-1 overflow-y-auto p-4 space-y-1">
          {navItems.map(({ path, icon: Icon, label }) => {
            const isActive = location.pathname === path
            return (
              <Link
                key={path} to={path} onClick={() => setIsOpen(false)}
                className={`flex items-center gap-3 px-4 py-2.5 rounded-xl transition-all duration-200
                  ${isActive ? 'bg-primary text-white glow-purple' : 'text-text-secondary hover:bg-rendly-surface-elevated hover:text-text-primary'}`}
              >
                <Icon className="w-5 h-5" />
                <span className="font-medium text-sm">{label}</span>
              </Link>
            )
          })}
        </nav>

        <div className="flex-shrink-0 p-4 border-t border-primary/10 space-y-2">
          <div className="flex items-center gap-3 px-4 py-3 bg-accent-green/10 rounded-xl">
            <div className="w-2 h-2 rounded-full bg-accent-green animate-pulse" />
            <span className="text-sm text-accent-green">IA Activa</span>
          </div>
        </div>
      </aside>
    </>
  )
}

/* ═══════════ Admin Header ═══════════ */
function Header({ setIsOpen, user, onLogout }) {
  const [notifications] = useState(3)

  return (
    <header className="h-16 bg-rendly-surface border-b border-primary/10 flex items-center justify-between px-4 lg:px-6">
      <button onClick={() => setIsOpen(true)} className="lg:hidden p-2 text-text-secondary hover:text-text-primary">
        <Menu className="w-6 h-6" />
      </button>

      <div className="hidden md:flex flex-1 max-w-md mx-4">
        <input
          type="text"
          placeholder="Buscar conversación, usuario..."
          className="w-full px-4 py-2 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
        />
      </div>

      <div className="flex items-center gap-4">
        <button className="relative p-2 text-text-secondary hover:text-text-primary transition-colors">
          <Bell className="w-6 h-6" />
          {notifications > 0 && (
            <span className="absolute -top-1 -right-1 w-5 h-5 bg-accent-magenta text-white text-xs rounded-full flex items-center justify-center">
              {notifications}
            </span>
          )}
        </button>

        <div className="flex items-center gap-3">
          <div className="hidden sm:block text-right">
            <p className="text-text-primary text-sm font-medium">{user?.email?.split('@')[0] || 'Admin'}</p>
            <p className="text-text-muted text-xs">Administrador</p>
          </div>
          <div className="w-10 h-10 rounded-full gradient-rendly flex items-center justify-center text-white font-bold">
            {user?.email?.[0]?.toUpperCase() || 'A'}
          </div>
          <button onClick={onLogout} className="p-2 text-text-muted hover:text-accent-magenta transition-colors" title="Cerrar sesión">
            <LogOut className="w-5 h-5" />
          </button>
        </div>
      </div>
    </header>
  )
}

/* ═══════════ Admin Layout ═══════════ */
function AdminLayout({ user, onLogout }) {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="flex h-screen bg-rendly-bg">
      <Sidebar isOpen={sidebarOpen} setIsOpen={setSidebarOpen} />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header setIsOpen={setSidebarOpen} user={user} onLogout={onLogout} />
        <main className="flex-1 overflow-auto p-4 lg:p-6">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/app-manager" element={<AdminAppManager />} />
            <Route path="/handshake-test" element={<HandshakeTest />} />
            <Route path="/notification-test" element={<NotificationTest />} />
            <Route path="/chat-test" element={<ChatTest />} />
            <Route path="/verification" element={<Verification />} />
            <Route path="/escalations" element={<Escalations />} />
            <Route path="/conversations" element={<Conversations />} />
            <Route path="/chat/:conversationId" element={<ChatView />} />
            <Route path="/ai-learning" element={<AILearning />} />
            <Route path="/training-pipeline" element={<AITrainingDashboard />} />
            <Route path="/feedback" element={<Feedback />} />
            <Route path="/bug-reports" element={<BugReports />} />
            <Route path="/content-reports" element={<ContentReports />} />
            <Route path="/stats" element={<Stats />} />
          </Routes>
        </main>
      </div>
    </div>
  )
}

/* ═══════════ Access Denied Screen ═══════════ */
function AccessDenied({ onLogout }) {
  return (
    <div className="min-h-screen bg-rendly-bg flex items-center justify-center p-4">
      <div className="glass rounded-3xl p-10 max-w-md text-center">
        <ShieldAlert className="w-16 h-16 text-accent-magenta mx-auto mb-6" />
        <h1 className="text-2xl font-bold text-text-primary mb-3">Acceso Denegado</h1>
        <p className="text-text-secondary mb-6">
          Tu cuenta no tiene permisos de administrador. Solo los usuarios verificados pueden acceder al panel admin.
        </p>
        <div className="flex flex-col gap-3">
          <Link to="/" className="btn-primary px-6 py-3 rounded-xl text-white font-semibold block">
            <span>Volver al inicio</span>
          </Link>
          <button onClick={onLogout} className="text-text-muted hover:text-text-primary text-sm transition-colors">
            Cerrar sesión
          </button>
        </div>
      </div>
    </div>
  )
}

/* ═══════════ Main App (Full: Public + Admin) ═══════════ */
export default function AppFull() {
  const [user, setUser] = useState(null)
  const [isVerified, setIsVerified] = useState(false)
  const [loading, setLoading] = useState(true)
  const [verifyLoading, setVerifyLoading] = useState(false)

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setUser(session?.user ?? null)
      if (session?.user) {
        checkVerified(session.user.id)
      } else {
        setLoading(false)
      }
    })

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setUser(session?.user ?? null)
      if (session?.user) {
        checkVerified(session.user.id)
      } else {
        setIsVerified(false)
        setLoading(false)
      }
    })

    return () => subscription.unsubscribe()
  }, [])

  async function checkVerified(userId) {
    setVerifyLoading(true)
    try {
      const { data, error } = await supabase
        .from('usuarios')
        .select('is_verified')
        .eq('user_id', userId)
        .single()

      if (error) {
        console.warn('Could not check verification:', error.message)
        setIsVerified(true)
      } else {
        setIsVerified(data?.is_verified === true)
      }
    } catch (err) {
      console.error('Verification check failed:', err)
      setIsVerified(true)
    } finally {
      setVerifyLoading(false)
      setLoading(false)
    }
  }

  async function handleLogout() {
    await supabase.auth.signOut()
    setUser(null)
    setIsVerified(false)
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-[#050508] flex items-center justify-center">
        <div className="text-center">
          <div className="w-12 h-12 border-3 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-text-muted text-sm">Cargando...</p>
        </div>
      </div>
    )
  }

  return (
    <BrowserRouter>
      <Routes>
        {/* ═══ Redirect root to Admin ═══ */}
        <Route path="/" element={<Navigate to="/admin" replace />} />

        {/* ═══ Admin Routes ═══ */}
        <Route path="/admin/*" element={
          !user ? (
            <Login onLogin={setUser} />
          ) : verifyLoading ? (
            <div className="min-h-screen bg-rendly-bg flex items-center justify-center">
              <div className="text-center">
                <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-3" />
                <p className="text-text-muted text-sm">Verificando permisos...</p>
              </div>
            </div>
          ) : !isVerified ? (
            <AccessDenied onLogout={handleLogout} />
          ) : (
            <AdminLayout user={user} onLogout={handleLogout} />
          )
        } />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
