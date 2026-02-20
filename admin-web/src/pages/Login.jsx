import { useState } from 'react'
import { Bot, Lock, Mail, Eye, EyeOff, AlertCircle, CheckCircle, ArrowLeft, UserPlus } from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function Login({ onLogin }) {
  const [email, setEmail] = useState('admin@rendly.com')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [mode, setMode] = useState('login') // login, recover, register

  async function handleLogin(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    setSuccess('')

    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password
      })

      if (error) throw error

      console.log('✅ Login exitoso:', data.user.email)
      onLogin(data.user)
    } catch (error) {
      console.error('❌ Error de login:', error)
      setError(error.message === 'Invalid login credentials' 
        ? 'Credenciales inválidas. Verifica tu email y contraseña.'
        : error.message
      )
    } finally {
      setLoading(false)
    }
  }

  async function handleRecoverPassword(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    setSuccess('')

    try {
      const { error } = await supabase.auth.resetPasswordForEmail(email, {
        redirectTo: `${window.location.origin}/reset-password`
      })

      if (error) throw error

      setSuccess('¡Enlace de recuperación enviado! Revisa tu email (también la carpeta de spam).')
    } catch (error) {
      console.error('❌ Error al enviar recuperación:', error)
      setError(error.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleRegister(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    setSuccess('')

    if (password.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres')
      setLoading(false)
      return
    }

    try {
      const { data, error } = await supabase.auth.signUp({
        email,
        password,
        options: {
          data: {
            role: 'admin'
          }
        }
      })

      if (error) throw error

      if (data.user && !data.session) {
        setSuccess('¡Usuario creado! Revisa tu email para confirmar la cuenta, o inicia sesión directamente.')
        setMode('login')
      } else if (data.session) {
        console.log('✅ Registro y login exitoso:', data.user.email)
        onLogin(data.user)
      }
    } catch (error) {
      console.error('❌ Error al registrar:', error)
      if (error.message.includes('already registered')) {
        setError('Este email ya está registrado. Intenta iniciar sesión.')
      } else {
        setError(error.message)
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-rendly-bg flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="w-20 h-20 mx-auto rounded-2xl gradient-rendly flex items-center justify-center mb-4">
            <Bot className="w-10 h-10 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-text-primary">Rendly Admin</h1>
          <p className="text-text-tertiary mt-2">Panel de Administración</p>
        </div>

        {/* Form */}
        <form 
          onSubmit={mode === 'login' ? handleLogin : mode === 'recover' ? handleRecoverPassword : handleRegister} 
          className="bg-rendly-surface rounded-2xl border border-primary/10 p-8 space-y-6"
        >
          {/* Header */}
          <div>
            {mode !== 'login' && (
              <button
                type="button"
                onClick={() => { setMode('login'); setError(''); setSuccess(''); }}
                className="flex items-center gap-2 text-text-muted hover:text-text-primary mb-4 transition-colors"
              >
                <ArrowLeft className="w-4 h-4" />
                Volver
              </button>
            )}
            <h2 className="text-xl font-semibold text-text-primary mb-2">
              {mode === 'login' && 'Iniciar Sesión'}
              {mode === 'recover' && 'Recuperar Contraseña'}
              {mode === 'register' && 'Crear Usuario Admin'}
            </h2>
            <p className="text-text-muted text-sm">
              {mode === 'login' && 'Accede al panel de administración'}
              {mode === 'recover' && 'Te enviaremos un enlace para restablecer tu contraseña'}
              {mode === 'register' && 'Crea una nueva cuenta de administrador'}
            </p>
          </div>

          {/* Error */}
          {error && (
            <div className="flex items-center gap-3 p-4 bg-accent-magenta/10 border border-accent-magenta/30 rounded-xl">
              <AlertCircle className="w-5 h-5 text-accent-magenta flex-shrink-0" />
              <p className="text-accent-magenta text-sm">{error}</p>
            </div>
          )}

          {/* Success */}
          {success && (
            <div className="flex items-center gap-3 p-4 bg-accent-green/10 border border-accent-green/30 rounded-xl">
              <CheckCircle className="w-5 h-5 text-accent-green flex-shrink-0" />
              <p className="text-accent-green text-sm">{success}</p>
            </div>
          )}

          <div className="space-y-4">
            {/* Email */}
            <div>
              <label className="block text-text-secondary text-sm font-medium mb-2">
                Email
              </label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-muted" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="admin@rendly.com"
                  required
                  className="w-full pl-12 pr-4 py-3 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
                />
              </div>
            </div>

            {/* Password - only show in login and register modes */}
            {mode !== 'recover' && (
              <div>
                <label className="block text-text-secondary text-sm font-medium mb-2">
                  Contraseña {mode === 'register' && <span className="text-text-muted">(mín. 6 caracteres)</span>}
                </label>
                <div className="relative">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-muted" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    minLength={mode === 'register' ? 6 : undefined}
                    className="w-full pl-12 pr-12 py-3 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-secondary transition-colors"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className={`w-full py-3 text-white font-semibold rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 ${
              mode === 'register' 
                ? 'bg-accent-green hover:bg-accent-green/80' 
                : 'bg-primary hover:bg-primary-bright'
            }`}
          >
            {loading ? (
              <>
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                {mode === 'login' && 'Iniciando sesión...'}
                {mode === 'recover' && 'Enviando...'}
                {mode === 'register' && 'Creando usuario...'}
              </>
            ) : (
              <>
                {mode === 'login' && 'Iniciar Sesión'}
                {mode === 'recover' && 'Enviar Enlace de Recuperación'}
                {mode === 'register' && (
                  <>
                    <UserPlus className="w-5 h-5" />
                    Crear Usuario Admin
                  </>
                )}
              </>
            )}
          </button>

          {/* Mode switchers - only show in login mode */}
          {mode === 'login' && (
            <div className="space-y-3">
              <button
                type="button"
                onClick={() => { setMode('recover'); setError(''); setSuccess(''); }}
                className="w-full text-center text-text-muted hover:text-primary text-sm transition-colors"
              >
                ¿Olvidaste tu contraseña?
              </button>
              
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-primary/10"></div>
                </div>
                <div className="relative flex justify-center text-xs">
                  <span className="px-2 bg-rendly-surface text-text-muted">o</span>
                </div>
              </div>

              <button
                type="button"
                onClick={() => { setMode('register'); setError(''); setSuccess(''); }}
                className="w-full py-2.5 border border-primary/30 text-primary hover:bg-primary/10 font-medium rounded-xl transition-colors flex items-center justify-center gap-2"
              >
                <UserPlus className="w-4 h-4" />
                Crear usuario admin
              </button>
            </div>
          )}

          <p className="text-center text-text-muted text-xs">
            Solo personal autorizado de Rendly
          </p>
        </form>

        {/* Footer */}
        <p className="text-center text-text-muted text-sm mt-6">
          © 2024 Rendly. Todos los derechos reservados.
        </p>
      </div>
    </div>
  )
}
