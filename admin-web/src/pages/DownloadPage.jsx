import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import {
  Download, ArrowLeft, Shield, CheckCircle, Smartphone,
  Clock, FileText, ChevronDown, ChevronUp, AlertTriangle,
  ShoppingBag, ExternalLink, Info, HardDrive
} from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function DownloadPage() {
  const [versions, setVersions] = useState([])
  const [loading, setLoading] = useState(true)
  const [expandedVersion, setExpandedVersion] = useState(null)
  const [downloading, setDownloading] = useState(false)

  useEffect(() => {
    loadVersions()
  }, [])

  async function loadVersions() {
    try {
      const { data, error } = await supabase
        .from('app_versions')
        .select('*')
        .order('created_at', { ascending: false })

      if (error) throw error
      setVersions(data || [])
    } catch (err) {
      console.error('Error loading versions:', err)
      // Fallback if table doesn't exist yet
      setVersions([{
        id: 'fallback',
        version_name: '1.0.0',
        version_code: 1,
        changelog: 'Primera versión de Merqora. Incluye:\n• Marketplace con búsqueda y filtros\n• Chat en tiempo real\n• Sistema de verificación\n• Handshake para transacciones seguras\n• Soporte IA 24/7\n• Ofertas dinámicas',
        file_size_mb: 45,
        min_android: '8.0',
        is_latest: true,
        download_count: 0,
        created_at: new Date().toISOString(),
        file_url: null
      }])
    } finally {
      setLoading(false)
    }
  }

  async function handleDownload(version) {
    setDownloading(true)
    try {
      // Increment download count
      if (version.id !== 'fallback') {
        await supabase
          .from('app_versions')
          .update({ download_count: (version.download_count || 0) + 1 })
          .eq('id', version.id)
      }

      let downloadUrl = null
      if (version.file_url) {
        downloadUrl = version.file_url
      } else {
        // Try to get from storage bucket
        const fileName = `merqora-v${version.version_name}.apk`
        const { data } = supabase.storage.from('app-releases').getPublicUrl(fileName)
        downloadUrl = data?.publicUrl
      }

      if (downloadUrl) {
        // Proxy GitHub URLs through Netlify to avoid redirect to github.com
        let finalUrl = downloadUrl
        const ghPrefix = 'https://github.com/merqora/merqora-releases/releases/download/'
        if (downloadUrl.startsWith(ghPrefix)) {
          finalUrl = '/download-apk/' + downloadUrl.slice(ghPrefix.length)
        }

        // Force direct download
        const link = document.createElement('a')
        link.href = finalUrl
        link.download = `merqora-v${version.version_name}.apk`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } else {
        alert('El APK aún no está disponible. El administrador debe subir la primera versión desde el panel de admin.')
      }
    } catch (err) {
      console.error('Download error:', err)
      alert('Error al descargar. Intentá de nuevo.')
    } finally {
      setDownloading(false)
    }
  }

  const latestVersion = versions.find(v => v.is_latest) || versions[0]
  const olderVersions = versions.filter(v => v.id !== latestVersion?.id)

  function formatDate(dateStr) {
    try {
      return new Date(dateStr).toLocaleDateString('es-AR', {
        year: 'numeric', month: 'long', day: 'numeric'
      })
    } catch { return dateStr }
  }

  return (
    <div className="min-h-screen bg-[#050508] relative overflow-hidden">
      {/* Background */}
      <div className="hero-glow w-[500px] h-[500px] bg-[#0A3D62]/25 -top-40 -right-40" />
      <div className="hero-glow w-[400px] h-[400px] bg-[#2E8B57]/20 bottom-40 -left-40" />

      {/* Navbar */}
      <nav className="glass-strong fixed top-0 left-0 right-0 z-50 py-4">
        <div className="max-w-5xl mx-auto px-4 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-9 h-9 rounded-xl gradient-rendly flex items-center justify-center">
              <ShoppingBag className="w-4 h-4 text-white" />
            </div>
            <span className="text-lg font-bold text-white">Merqora</span>
          </Link>
          <Link to="/" className="flex items-center gap-2 text-text-secondary hover:text-white transition-colors text-sm">
            <ArrowLeft className="w-4 h-4" /> Volver al inicio
          </Link>
        </div>
      </nav>

      <main className="max-w-5xl mx-auto px-4 pt-28 pb-20 relative z-10">
        {/* Hero */}
        <div className="text-center mb-12">
          <div className="w-20 h-20 rounded-3xl gradient-rendly flex items-center justify-center mx-auto mb-6 animate-glow">
            <Download className="w-10 h-10 text-white" />
          </div>
          <h1 className="text-4xl sm:text-5xl font-black text-white mb-4">
            Descargá <span className="gradient-text">Merqora</span>
          </h1>
          <p className="text-text-secondary text-lg max-w-xl mx-auto">
            Descarga directa y segura. Instalá en tu Android en menos de un minuto.
          </p>
        </div>

        {loading ? (
          <div className="flex justify-center py-20">
            <div className="w-10 h-10 border-3 border-primary border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          <>
            {/* Latest Version Card */}
            {latestVersion && (
              <div className="glass rounded-3xl p-8 mb-8 relative overflow-hidden">
                {/* Gradient accent */}
                <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#0A3D62] via-[#FF6B35] to-[#2E8B57]" />

                <div className="flex flex-col lg:flex-row lg:items-center gap-8">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-4">
                      <span className="px-3 py-1 rounded-full bg-accent-green/10 text-accent-green text-sm font-semibold">
                        Última versión
                      </span>
                      <span className="text-text-muted text-sm">
                        {formatDate(latestVersion.created_at)}
                      </span>
                    </div>

                    <h2 className="text-2xl font-bold text-white mb-2">
                      Merqora v{latestVersion.version_name}
                    </h2>

                    <div className="flex flex-wrap gap-4 text-sm text-text-secondary mb-4">
                      {latestVersion.file_size_mb && (
                        <div className="flex items-center gap-1.5">
                          <HardDrive className="w-4 h-4" />
                          <span>{latestVersion.file_size_mb} MB</span>
                        </div>
                      )}
                      <div className="flex items-center gap-1.5">
                        <Smartphone className="w-4 h-4" />
                        <span>Android {latestVersion.min_android || '8.0'}+</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Download className="w-4 h-4" />
                        <span>{latestVersion.download_count || 0} descargas</span>
                      </div>
                    </div>

                    {latestVersion.changelog && (
                      <div className="text-text-secondary text-sm leading-relaxed whitespace-pre-line">
                        {latestVersion.changelog}
                      </div>
                    )}
                  </div>

                  <div className="flex-shrink-0">
                    <button
                      onClick={() => handleDownload(latestVersion)}
                      disabled={downloading}
                      className="btn-primary w-full lg:w-auto px-10 py-5 rounded-2xl text-white font-bold text-lg download-pulse disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span className="flex items-center justify-center gap-3">
                        {downloading ? (
                          <>
                            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                            Descargando...
                          </>
                        ) : (
                          <>
                            <Download className="w-6 h-6" />
                            Descargar APK
                          </>
                        )}
                      </span>
                    </button>
                    <p className="text-text-muted text-xs text-center mt-2">
                      100% Gratis · Sin malware
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Installation Instructions */}
            <div className="glass rounded-3xl p-8 mb-8">
              <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-3">
                <Info className="w-6 h-6 text-primary" />
                Cómo instalar
              </h3>
              <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {[
                  { step: '1', title: 'Descargá el APK', desc: 'Tocá el botón de descarga arriba', icon: Download },
                  { step: '2', title: 'Abrí el archivo', desc: 'Buscalo en tus descargas', icon: FileText },
                  { step: '3', title: 'Permití la instalación', desc: 'Habilitá "fuentes desconocidas" si te lo pide', icon: Shield },
                  { step: '4', title: '¡Listo!', desc: 'Abrí Merqora y creá tu cuenta', icon: CheckCircle },
                ].map((s, i) => (
                  <div key={i} className="bg-rendly-bg rounded-2xl p-4 text-center">
                    <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center mx-auto mb-3">
                      <s.icon className="w-6 h-6 text-primary" />
                    </div>
                    <div className="text-xs font-bold text-primary mb-1">Paso {s.step}</div>
                    <h4 className="font-semibold text-white text-sm mb-1">{s.title}</h4>
                    <p className="text-text-tertiary text-xs">{s.desc}</p>
                  </div>
                ))}
              </div>

              {/* Warning about Chrome download + unknown sources */}
              <div className="mt-6 space-y-3">
                <div className="bg-accent-gold/5 border border-accent-gold/20 rounded-2xl p-4 flex items-start gap-3">
                  <AlertTriangle className="w-5 h-5 text-accent-gold flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="text-white text-sm font-medium mb-1">¿Tu navegador dice "archivo dañino"?</p>
                    <p className="text-text-secondary text-xs leading-relaxed">
                      Es normal. Chrome muestra este aviso para <strong className="text-white">todas</strong> las apps que no vienen de Play Store. 
                      Tocá <strong className="text-white">"Descargar de todos modos"</strong> para continuar. 
                      Merqora es 100% segura y libre de virus.
                    </p>
                  </div>
                </div>
                <div className="bg-primary/5 border border-primary/20 rounded-2xl p-4 flex items-start gap-3">
                  <Shield className="w-5 h-5 text-primary flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="text-white text-sm font-medium mb-1">¿Te pide habilitar "fuentes desconocidas"?</p>
                    <p className="text-text-secondary text-xs leading-relaxed">
                      Andá a <strong className="text-white">Configuración → Seguridad → Fuentes desconocidas</strong> y habilitá 
                      la opción para tu navegador. Esto es necesario para instalar apps fuera de Play Store.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Version History */}
            {olderVersions.length > 0 && (
              <div className="glass rounded-3xl p-8">
                <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-3">
                  <Clock className="w-6 h-6 text-text-tertiary" />
                  Versiones anteriores
                </h3>
                <div className="space-y-3">
                  {olderVersions.map(v => (
                    <div key={v.id} className="bg-rendly-bg rounded-2xl overflow-hidden">
                      <button
                        onClick={() => setExpandedVersion(expandedVersion === v.id ? null : v.id)}
                        className="w-full flex items-center justify-between p-4 hover:bg-rendly-surface-elevated transition-colors"
                      >
                        <div className="flex items-center gap-4">
                          <span className="font-semibold text-white">v{v.version_name}</span>
                          <span className="text-text-muted text-sm">{formatDate(v.created_at)}</span>
                          {v.file_size_mb && <span className="text-text-muted text-sm">{v.file_size_mb} MB</span>}
                        </div>
                        {expandedVersion === v.id ? <ChevronUp className="w-5 h-5 text-text-muted" /> : <ChevronDown className="w-5 h-5 text-text-muted" />}
                      </button>
                      {expandedVersion === v.id && (
                        <div className="px-4 pb-4 border-t border-white/5">
                          {v.changelog && (
                            <p className="text-text-secondary text-sm whitespace-pre-line mt-3 mb-4">{v.changelog}</p>
                          )}
                          <button
                            onClick={() => handleDownload(v)}
                            className="btn-outline px-5 py-2 rounded-xl text-white text-sm font-medium flex items-center gap-2"
                          >
                            <Download className="w-4 h-4" /> Descargar v{v.version_name}
                          </button>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Requirements */}
            <div className="mt-8 text-center">
              <h4 className="text-white font-semibold mb-3">Requisitos del sistema</h4>
              <div className="flex flex-wrap justify-center gap-6 text-text-tertiary text-sm">
                <span>Android 8.0 (Oreo) o superior</span>
                <span>·</span>
                <span>~50 MB de espacio</span>
                <span>·</span>
                <span>Conexión a internet</span>
              </div>
            </div>
          </>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-white/5 py-6">
        <div className="max-w-5xl mx-auto px-4 flex flex-col sm:flex-row justify-between items-center gap-4">
          <p className="text-text-muted text-sm">© {new Date().getFullYear()} Merqora</p>
          <Link to="/" className="text-text-tertiary hover:text-white text-sm transition-colors">Volver al inicio</Link>
        </div>
      </footer>
    </div>
  )
}
