import { useState, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import {
  Shield, HeartHandshake, MessageCircle, Bot, Tag, Users,
  Download, ChevronRight, Star, CheckCircle, Smartphone,
  ArrowRight, Zap, Lock, Heart, ShoppingBag, TrendingUp,
  Eye, Sparkles, ChevronDown, Menu, X
} from 'lucide-react'

/* ═══════════ Hooks & Helpers ═══════════ */
function useInView(threshold = 0.1) {
  const ref = useRef(null)
  const [visible, setVisible] = useState(false)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    const obs = new IntersectionObserver(([e]) => {
      if (e.isIntersecting) { setVisible(true); obs.disconnect() }
    }, { threshold })
    obs.observe(el)
    return () => obs.disconnect()
  }, [threshold])
  return [ref, visible]
}

function Reveal({ children, className = '', delay = 0 }) {
  const [ref, vis] = useInView()
  return (
    <div
      ref={ref}
      className={`transition-all duration-700 ease-out ${vis ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'} ${className}`}
      style={{ transitionDelay: `${delay}ms` }}
    >{children}</div>
  )
}

function Counter({ target, suffix = '' }) {
  const [count, setCount] = useState(0)
  const [ref, vis] = useInView()
  useEffect(() => {
    if (!vis) return
    let n = 0
    const step = Math.max(1, Math.ceil(target / 60))
    const t = setInterval(() => {
      n += step
      if (n >= target) { setCount(target); clearInterval(t) }
      else setCount(n)
    }, 25)
    return () => clearInterval(t)
  }, [vis, target])
  return <span ref={ref}>{count.toLocaleString('es-AR')}{suffix}</span>
}

/* ═══════════ Data ═══════════ */
const FEATURES = [
  { icon: Shield, color: '#0A3D62', title: 'Vendedores Verificados', desc: 'Cada vendedor pasa por un proceso de verificación real. Comprá con la confianza de saber con quién tratás.' },
  { icon: HeartHandshake, color: '#FF6B35', title: 'Sistema Handshake', desc: 'Transacciones presenciales seguras con confirmación QR bilateral. Sin estafas, sin sorpresas.' },
  { icon: MessageCircle, color: '#2E8B57', title: 'Chat en Tiempo Real', desc: 'Mensajería instantánea con presencia online, reacciones y notificaciones push.' },
  { icon: Bot, color: '#0A3D62', title: 'Soporte IA 24/7', desc: 'Nuestro asistente inteligente resuelve tus dudas al instante. Si necesitás más, escalamos a un humano.' },
  { icon: Tag, color: '#FF6B35', title: 'Ofertas Dinámicas', desc: 'Campañas flash, descuentos por tiempo limitado y liquidaciones actualizadas en tiempo real.' },
  { icon: Users, color: '#2E8B57', title: 'Comunidad Social', desc: 'Seguí a tus vendedores favoritos, mirá stories, dale like y guardá lo que te gusta.' },
]

const STEPS = [
  { num: '01', title: 'Descargá la App', desc: 'Bajá Merqora gratis desde nuestro sitio en segundos.', icon: Download },
  { num: '02', title: 'Creá tu Cuenta', desc: 'Registrate y verificá tu perfil para mayor confianza.', icon: CheckCircle },
  { num: '03', title: 'Comprá y Vendé', desc: 'Explorá miles de productos o publicá los tuyos.', icon: ShoppingBag },
]

/* ═══════════ Navbar ═══════════ */
function Navbar() {
  const [scrolled, setScrolled] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  useEffect(() => {
    const h = () => setScrolled(window.scrollY > 50)
    window.addEventListener('scroll', h)
    return () => window.removeEventListener('scroll', h)
  }, [])

  return (
    <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${scrolled ? 'navbar-scrolled py-3' : 'py-5'}`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-3 group">
          <div className="w-10 h-10 rounded-xl gradient-rendly flex items-center justify-center group-hover:scale-110 transition-transform">
            <ShoppingBag className="w-5 h-5 text-white" />
          </div>
          <span className="text-xl font-bold text-white">Merqora</span>
        </Link>

        {/* Desktop links */}
        <div className="hidden md:flex items-center gap-8">
          <a href="#features" className="text-text-secondary hover:text-white transition-colors text-sm font-medium">Características</a>
          <a href="#how" className="text-text-secondary hover:text-white transition-colors text-sm font-medium">Cómo funciona</a>
          <a href="#download" className="text-text-secondary hover:text-white transition-colors text-sm font-medium">Descargar</a>
          <Link to="/download" className="btn-primary px-5 py-2.5 rounded-xl text-sm font-semibold text-white">
            <span className="flex items-center gap-2"><Download className="w-4 h-4" /> Descargar App</span>
          </Link>
        </div>

        {/* Mobile toggle */}
        <button onClick={() => setMobileOpen(!mobileOpen)} className="md:hidden p-2 text-white">
          {mobileOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="md:hidden glass-strong mt-2 mx-4 rounded-2xl p-6 space-y-4 animate-slide-down">
          <a href="#features" onClick={() => setMobileOpen(false)} className="block text-text-secondary hover:text-white py-2">Características</a>
          <a href="#how" onClick={() => setMobileOpen(false)} className="block text-text-secondary hover:text-white py-2">Cómo funciona</a>
          <a href="#download" onClick={() => setMobileOpen(false)} className="block text-text-secondary hover:text-white py-2">Descargar</a>
          <Link to="/download" onClick={() => setMobileOpen(false)} className="btn-primary block text-center px-5 py-3 rounded-xl text-white font-semibold">
            <span>Descargar App</span>
          </Link>
        </div>
      )}
    </nav>
  )
}

/* ═══════════ Hero ═══════════ */
function Hero() {
  return (
    <section className="relative min-h-screen flex items-center pt-20 overflow-hidden">
      {/* Animated background blobs */}
      <div className="hero-glow w-[500px] h-[500px] bg-[#0A3D62]/40 top-10 -left-40 animate-float" />
      <div className="hero-glow w-[600px] h-[600px] bg-[#FF6B35]/20 bottom-0 -right-40 animate-float-delay" />
      <div className="hero-glow w-[300px] h-[300px] bg-[#2E8B57]/25 top-1/2 left-1/3 animate-float-slow" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid lg:grid-cols-2 gap-12 lg:gap-20 items-center relative z-10">
        {/* Left content */}
        <div className="text-center lg:text-left">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full glass text-sm text-text-secondary mb-6 animate-fade-in">
            <Sparkles className="w-4 h-4 text-accent-gold" />
            <span>Nuevo en Uruguay</span>
            <ChevronRight className="w-3 h-3" />
          </div>

          <h1 className="text-4xl sm:text-5xl lg:text-6xl xl:text-7xl font-black leading-[1.1] tracking-tight mb-6 animate-slide-up">
            El marketplace<br />social{' '}
            <span className="gradient-text">más seguro</span>
          </h1>

          <p className="text-lg sm:text-xl text-text-secondary max-w-lg mx-auto lg:mx-0 mb-8 leading-relaxed animate-slide-up" style={{ animationDelay: '150ms' }}>
            Compra, vende y conecta con confianza. Vendedores verificados, transacciones seguras y una comunidad que crece cada día.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start animate-slide-up" style={{ animationDelay: '300ms' }}>
            <Link to="/download" className="btn-primary px-8 py-4 rounded-2xl text-white font-bold text-lg download-pulse">
              <span className="flex items-center justify-center gap-3">
                <Download className="w-5 h-5" />
                Descargar Gratis
              </span>
            </Link>
            <a href="#features" className="btn-outline px-8 py-4 rounded-2xl text-white font-semibold text-lg flex items-center justify-center gap-2">
              Conocer más <ArrowRight className="w-5 h-5" />
            </a>
          </div>

          {/* Social proof */}
          <div className="flex items-center gap-6 mt-10 justify-center lg:justify-start animate-fade-in" style={{ animationDelay: '500ms' }}>
            <div className="flex -space-x-3">
              {['🟣','🔵','🟢','🟡','🔴'].map((c, i) => (
                <div key={i} className="w-10 h-10 rounded-full border-2 border-rendly-bg bg-rendly-surface-elevated flex items-center justify-center text-lg">{c}</div>
              ))}
            </div>
            <div className="text-left">
              <div className="flex items-center gap-1">
                {[...Array(5)].map((_, i) => <Star key={i} className="w-4 h-4 fill-accent-gold text-accent-gold" />)}
              </div>
              <p className="text-text-tertiary text-sm">Usuarios confían en Merqora</p>
            </div>
          </div>
        </div>

        {/* Right — Phone mockup */}
        <div className="flex justify-center lg:justify-end animate-scale-in" style={{ animationDelay: '400ms' }}>
          <div className="relative">
            {/* Glow behind phone */}
            <div className="absolute inset-0 bg-gradient-to-r from-[#0A3D62]/30 to-[#2E8B57]/30 blur-[80px] scale-125" />

            <div className="phone-frame animate-float relative z-10">
              <div className="phone-screen">
                {/* Fake app UI */}
                <div className="p-4 pt-10">
                  <div className="flex items-center justify-between mb-6">
                    <div className="text-lg font-bold text-white">Merqora</div>
                    <div className="flex gap-2">
                      <div className="w-8 h-8 rounded-full bg-rendly-surface-elevated" />
                    </div>
                  </div>
                  {/* Search bar */}
                  <div className="h-10 rounded-xl bg-rendly-surface-elevated mb-4 flex items-center px-3">
                    <div className="w-4 h-4 rounded-full border border-text-muted" />
                    <div className="ml-2 h-3 w-24 rounded bg-text-muted/20" />
                  </div>
                  {/* Categories */}
                  <div className="flex gap-2 mb-4 overflow-hidden">
                    {['Ofertas', 'Top', 'Zona'].map((c, i) => (
                      <div key={i} className={`px-3 py-1.5 rounded-lg text-xs font-medium ${i === 0 ? 'bg-primary text-white' : 'bg-rendly-surface-elevated text-text-secondary'}`}>{c}</div>
                    ))}
                  </div>
                  {/* Product cards */}
                  <div className="grid grid-cols-2 gap-2">
                    {[1,2,3,4].map(i => (
                      <div key={i} className="rounded-xl bg-rendly-surface-elevated overflow-hidden">
                        <div className={`h-20 ${i % 2 === 0 ? 'bg-gradient-to-br from-[#0A3D62]/40 to-[#2E8B57]/40' : 'bg-gradient-to-br from-[#2E8B57]/30 to-[#FF6B35]/30'}`} />
                        <div className="p-2">
                          <div className="h-2 w-16 rounded bg-text-muted/30 mb-1" />
                          <div className="h-3 w-12 rounded bg-accent-green/30" />
                        </div>
                      </div>
                    ))}
                  </div>
                  {/* Bottom nav - matches app's BottomNavBar */}
                  <div className="absolute bottom-2 left-3 right-3 h-14 rounded-2xl bg-rendly-tab-bar flex items-center justify-around px-2">
                    {/* Home */}
                    <svg viewBox="0 0 24 24" className="w-5 h-5 text-[#3A8FD4]" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
                    {/* Search */}
                    <svg viewBox="0 0 24 24" className="w-5 h-5 text-white/30" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                    {/* Center Publish Button */}
                    <div className="w-10 h-10 rounded-[10px] flex items-center justify-center" style={{background: 'linear-gradient(135deg, #0A3D62, #0E4D7B)', border: '1.5px solid rgba(255,107,53,0.5)'}}>
                      <svg viewBox="0 0 24 24" className="w-4 h-4 text-white" fill="currentColor"><path d="M3 4V1h2v3h3v2H5v3H3V6H0V4h3zm3 6V7h3V4h7l1.83 2H21c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H5c-1.1 0-2-.9-2-2V10h3zm7 9c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-3.2-5c0 1.77 1.43 3.2 3.2 3.2s3.2-1.43 3.2-3.2-1.43-3.2-3.2-3.2-3.2 1.43-3.2 3.2z"/></svg>
                    </div>
                    {/* Rends (Movie) */}
                    <svg viewBox="0 0 24 24" className="w-5 h-5 text-white/30" fill="currentColor"><path d="M18 4l2 4h-3l-2-4h-2l2 4h-3l-2-4H8l2 4H7L5 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V4h-4z"/></svg>
                    {/* Profile */}
                    <svg viewBox="0 0 24 24" className="w-5 h-5 text-white/30" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Scroll indicator */}
      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce-slow">
        <ChevronDown className="w-6 h-6 text-text-muted" />
      </div>
    </section>
  )
}

/* ═══════════ Stats Bar ═══════════ */
function StatsBar() {
  return (
    <Reveal>
      <div className="max-w-5xl mx-auto px-4 mb-10 mt-16">
        <div className="glass rounded-3xl px-8 py-8 grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
          {[
            { label: 'Descargas', value: 500, suffix: '+' },
            { label: 'Vendedores Verificados', value: 120, suffix: '+' },
            { label: 'Transacciones Seguras', value: 1800, suffix: '+' },
            { label: 'Valoración', value: 4.9, suffix: '★', fixed: true },
          ].map((s, i) => (
            <div key={i}>
              <div className="text-3xl md:text-4xl font-black text-white">
                {s.fixed ? <span>{s.value}{s.suffix}</span> : <Counter target={s.value} suffix={s.suffix} />}
              </div>
              <div className="text-text-tertiary text-sm mt-1">{s.label}</div>
            </div>
          ))}
        </div>
      </div>
    </Reveal>
  )
}

/* ═══════════ Features ═══════════ */
function Features() {
  return (
    <section id="features" className="py-24 relative overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <Reveal>
          <div className="text-center mb-16">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4">
              <Zap className="w-4 h-4" /> Características
            </div>
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-black text-white mb-4">
              Todo lo que necesitás<br /><span className="gradient-text">en un solo lugar</span>
            </h2>
            <p className="text-text-secondary max-w-2xl mx-auto text-lg">
              Merqora combina lo mejor del marketplace con la seguridad que necesitás para comprar y vender tranquilo.
            </p>
          </div>
        </Reveal>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {FEATURES.map((f, i) => (
            <Reveal key={i} delay={i * 100}>
              <div className="glass card-hover rounded-2xl p-6 h-full group cursor-default">
                <div className="feature-icon mb-4" style={{ color: f.color }}>
                  <f.icon className="w-6 h-6 relative z-10" />
                </div>
                <h3 className="text-lg font-bold text-white mb-2 group-hover:text-primary-bright transition-colors">{f.title}</h3>
                <p className="text-text-secondary text-sm leading-relaxed">{f.desc}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}

/* ═══════════ How It Works ═══════════ */
function HowItWorks() {
  return (
    <section id="how" className="py-24 relative overflow-hidden">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <Reveal>
          <div className="text-center mb-16">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent-green/10 text-accent-green text-sm font-medium mb-4">
              <CheckCircle className="w-4 h-4" /> Súper fácil
            </div>
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-black text-white mb-4">
              Empezá en <span className="gradient-text">3 pasos</span>
            </h2>
          </div>
        </Reveal>

        <div className="grid md:grid-cols-3 gap-8 relative">
          {/* Connector line (desktop) */}
          <div className="hidden md:block absolute top-16 left-[20%] right-[20%] h-[2px] bg-gradient-to-r from-[#0A3D62]/30 via-[#FF6B35]/30 to-[#2E8B57]/30" />

          {STEPS.map((s, i) => (
            <Reveal key={i} delay={i * 150}>
              <div className="text-center relative">
                <div className="w-16 h-16 rounded-2xl gradient-rendly flex items-center justify-center mx-auto mb-6 relative z-10 shadow-lg shadow-[#0A3D62]/20">
                  <s.icon className="w-7 h-7 text-white" />
                </div>
                <div className="text-xs font-bold text-primary mb-2 tracking-widest">{s.num}</div>
                <h3 className="text-xl font-bold text-white mb-2">{s.title}</h3>
                <p className="text-text-secondary text-sm">{s.desc}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}

/* ═══════════ Security Highlight ═══════════ */
function SecuritySection() {
  return (
    <section className="py-24 relative overflow-hidden">
      <div className="hero-glow w-[400px] h-[400px] bg-[#0A3D62]/25 -left-40 top-1/2 -translate-y-1/2" />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid lg:grid-cols-2 gap-16 items-center">
        <Reveal>
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent-magenta/10 text-accent-magenta text-sm font-medium mb-4">
              <Lock className="w-4 h-4" /> Seguridad primero
            </div>
            <h2 className="text-3xl sm:text-4xl font-black text-white mb-6">
              Tu seguridad es<br />nuestra <span className="gradient-text">prioridad #1</span>
            </h2>
            <div className="space-y-5">
              {[
                { icon: Shield, text: 'Verificación de identidad real para vendedores' },
                { icon: HeartHandshake, text: 'Sistema Handshake con confirmación QR bilateral' },
                { icon: Eye, text: 'Reputación transparente basada en transacciones reales' },
                { icon: Lock, text: 'Encriptación de datos y pagos seguros con MercadoPago' },
              ].map((item, i) => (
                <div key={i} className="flex items-start gap-4">
                  <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center flex-shrink-0">
                    <item.icon className="w-5 h-5 text-primary" />
                  </div>
                  <p className="text-text-secondary leading-relaxed pt-2">{item.text}</p>
                </div>
              ))}
            </div>
          </div>
        </Reveal>

        <Reveal delay={200}>
          <div className="glass rounded-3xl p-8 relative">
            <div className="absolute -top-4 -right-4 w-20 h-20 bg-gradient-to-br from-[#0A3D62] to-[#2E8B57] rounded-2xl flex items-center justify-center rotate-12 shadow-lg">
              <Shield className="w-10 h-10 text-white -rotate-12" />
            </div>
            <div className="space-y-6 pt-4">
              {/* Fake verification card */}
              <div className="bg-rendly-bg rounded-2xl p-4 flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-[#0A3D62] flex items-center justify-center text-white font-bold">M</div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-white">@mauro_shop</span>
                    <span className="text-blue-400">✓</span>
                  </div>
                  <span className="text-accent-green text-sm font-medium">Vendedor Verificado</span>
                </div>
                <div className="text-right">
                  <div className="text-accent-gold font-bold">98%</div>
                  <div className="text-text-muted text-xs">reputación</div>
                </div>
              </div>
              {/* Handshake demo */}
              <div className="bg-rendly-bg rounded-2xl p-4">
                <div className="flex items-center gap-3 mb-3">
                  <HeartHandshake className="w-5 h-5 text-accent-magenta" />
                  <span className="font-semibold text-white text-sm">Handshake Completado</span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded-full bg-[#0A3D62] flex items-center justify-center text-xs text-white">C</div>
                    <span className="text-text-secondary">Comprador</span>
                  </div>
                  <div className="text-accent-green">✓ Confirmado</div>
                  <div className="flex items-center gap-2">
                    <span className="text-text-secondary">Vendedor</span>
                <div className="w-6 h-6 rounded-full bg-[#FF6B35]/80 flex items-center justify-center text-xs text-white">V</div>
                  </div>
                </div>
              </div>
              {/* Transaction badge */}
              <div className="bg-accent-green/10 rounded-2xl p-4 flex items-center gap-3">
                <CheckCircle className="w-6 h-6 text-accent-green" />
                <div>
                  <div className="font-semibold text-white text-sm">Transacción exitosa</div>
                  <div className="text-text-tertiary text-xs">Ambas partes confirmaron · Hace 2 min</div>
                </div>
              </div>
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  )
}

/* ═══════════ Download CTA ═══════════ */
function DownloadCTA() {
  return (
    <section id="download" className="py-24 relative overflow-hidden">
      <div className="hero-glow w-[600px] h-[600px] bg-[#0A3D62]/25 left-1/2 -translate-x-1/2 top-1/2 -translate-y-1/2" />
      <div className="max-w-4xl mx-auto px-4 text-center relative z-10">
        <Reveal>
          <div className="glass rounded-3xl p-10 sm:p-16">
            <div className="w-20 h-20 rounded-3xl gradient-rendly flex items-center justify-center mx-auto mb-6 animate-glow">
              <Smartphone className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-black text-white mb-4">
              Descargá <span className="gradient-text">Merqora</span> ahora
            </h2>
            <p className="text-text-secondary text-lg mb-8 max-w-xl mx-auto">
              Disponible para Android. Descarga directa, sin Play Store. Instalación rápida y segura.
            </p>
            <Link to="/download" className="btn-primary inline-flex items-center gap-3 px-10 py-5 rounded-2xl text-white font-bold text-lg download-pulse">
              <span className="flex items-center gap-3">
                <Download className="w-6 h-6" />
                Descargar APK Gratis
              </span>
            </Link>
            <p className="text-text-muted text-sm mt-4">Android 8.0+ · Menos de 50MB · 100% Gratis</p>
          </div>
        </Reveal>
      </div>
    </section>
  )
}

/* ═══════════ Footer ═══════════ */
function Footer() {
  return (
    <footer className="border-t border-white/5 py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid md:grid-cols-4 gap-8 mb-10">
          <div className="md:col-span-2">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-xl gradient-rendly flex items-center justify-center">
                <ShoppingBag className="w-5 h-5 text-white" />
              </div>
              <span className="text-xl font-bold text-white">Merqora</span>
            </div>
            <p className="text-text-secondary text-sm leading-relaxed max-w-md">
              El marketplace social más seguro de Uruguay. Compra, vende y conecta con confianza gracias a nuestro sistema de verificación y transacciones seguras.
            </p>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4">Producto</h4>
            <div className="space-y-2">
              <a href="#features" className="block text-text-secondary hover:text-white text-sm transition-colors">Características</a>
              <a href="#how" className="block text-text-secondary hover:text-white text-sm transition-colors">Cómo funciona</a>
              <Link to="/download" className="block text-text-secondary hover:text-white text-sm transition-colors">Descargar</Link>
            </div>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-4">Legal</h4>
            <div className="space-y-2">
              <a href="#" className="block text-text-secondary hover:text-white text-sm transition-colors">Términos de uso</a>
              <a href="#" className="block text-text-secondary hover:text-white text-sm transition-colors">Privacidad</a>
              <a href="#" className="block text-text-secondary hover:text-white text-sm transition-colors">Contacto</a>
            </div>
          </div>
        </div>
        <div className="section-separator mb-6" />
        <div className="flex flex-col sm:flex-row justify-between items-center gap-4">
          <p className="text-text-muted text-sm">© {new Date().getFullYear()} Merqora. Todos los derechos reservados.</p>
          <div className="flex items-center gap-2 text-text-muted text-sm">
            <Heart className="w-4 h-4 text-accent-magenta" />
            <span>Hecho en Uruguay</span>
          </div>
        </div>
      </div>
    </footer>
  )
}

/* ═══════════ Main Export ═══════════ */
export default function LandingPage() {
  return (
    <div className="min-h-screen bg-[#050508] overflow-x-hidden">
      <Navbar />
      <Hero />
      <StatsBar />
      <div className="section-separator max-w-5xl mx-auto" />
      <Features />
      <div className="section-separator max-w-5xl mx-auto" />
      <HowItWorks />
      <div className="section-separator max-w-5xl mx-auto" />
      <SecuritySection />
      <DownloadCTA />
      <Footer />
    </div>
  )
}
