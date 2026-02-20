import { useState, useEffect, useRef } from 'react'
import {
  Upload, Download, Trash2, CheckCircle, AlertCircle,
  Package, FileText, Clock, Eye, Plus, X, Save,
  Smartphone, HardDrive, TrendingUp, RefreshCw
} from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function AdminAppManager() {
  const [versions, setVersions] = useState([])
  const [loading, setLoading] = useState(true)
  const [showUploadModal, setShowUploadModal] = useState(false)
  const [stats, setStats] = useState({ totalDownloads: 0, totalVersions: 0, latestVersion: '-' })

  useEffect(() => { loadVersions() }, [])

  async function loadVersions() {
    setLoading(true)
    try {
      const { data, error } = await supabase
        .from('app_versions')
        .select('*')
        .order('created_at', { ascending: false })

      if (error) throw error
      const v = data || []
      setVersions(v)
      setStats({
        totalDownloads: v.reduce((sum, ver) => sum + (ver.download_count || 0), 0),
        totalVersions: v.length,
        latestVersion: v.find(ver => ver.is_latest)?.version_name || v[0]?.version_name || '-'
      })
    } catch (err) {
      console.error('Error loading versions:', err)
    } finally {
      setLoading(false)
    }
  }

  async function deleteVersion(id) {
    if (!confirm('¿Seguro que querés eliminar esta versión?')) return
    try {
      const version = versions.find(v => v.id === id)
      // Delete from storage if exists
      if (version?.file_path) {
        await supabase.storage.from('app-releases').remove([version.file_path])
      }
      const { error } = await supabase.from('app_versions').delete().eq('id', id)
      if (error) throw error
      await loadVersions()
    } catch (err) {
      alert('Error al eliminar: ' + err.message)
    }
  }

  async function setAsLatest(id) {
    try {
      // Unset all as latest
      await supabase.from('app_versions').update({ is_latest: false }).neq('id', 'none')
      // Set this one as latest
      const { error } = await supabase.from('app_versions').update({ is_latest: true }).eq('id', id)
      if (error) throw error
      await loadVersions()
    } catch (err) {
      alert('Error: ' + err.message)
    }
  }

  function formatDate(dateStr) {
    try {
      return new Date(dateStr).toLocaleDateString('es-AR', {
        year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
      })
    } catch { return dateStr }
  }

  return (
    <div className="fade-in space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-3">
            <Package className="w-7 h-7 text-primary" />
            App Manager
          </h1>
          <p className="text-text-tertiary text-sm mt-1">Subí y gestioná las versiones de la app Merqora</p>
        </div>
        <div className="flex gap-3">
          <button
            onClick={loadVersions}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-rendly-surface-elevated text-text-secondary hover:text-text-primary transition-colors"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} /> Recargar
          </button>
          <button
            onClick={() => setShowUploadModal(true)}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary hover:bg-primary-dark text-white font-semibold transition-colors"
          >
            <Plus className="w-5 h-5" /> Nueva Versión
          </button>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[
          { label: 'Total Descargas', value: stats.totalDownloads, icon: Download, color: 'text-accent-green' },
          { label: 'Versiones Publicadas', value: stats.totalVersions, icon: Package, color: 'text-primary' },
          { label: 'Versión Actual', value: `v${stats.latestVersion}`, icon: Smartphone, color: 'text-accent-gold' },
        ].map((s, i) => (
          <div key={i} className="bg-rendly-surface rounded-2xl p-5 border border-primary/5">
            <div className="flex items-center gap-3 mb-2">
              <s.icon className={`w-5 h-5 ${s.color}`} />
              <span className="text-text-tertiary text-sm">{s.label}</span>
            </div>
            <div className="text-2xl font-bold text-text-primary">{s.value}</div>
          </div>
        ))}
      </div>

      {/* Versions List */}
      <div className="bg-rendly-surface rounded-2xl border border-primary/5 overflow-hidden">
        <div className="p-5 border-b border-primary/5">
          <h2 className="text-lg font-semibold text-text-primary">Todas las versiones</h2>
        </div>

        {loading ? (
          <div className="flex justify-center py-16">
            <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          </div>
        ) : versions.length === 0 ? (
          <div className="text-center py-16">
            <Package className="w-12 h-12 text-text-muted mx-auto mb-4" />
            <p className="text-text-secondary font-medium">No hay versiones publicadas</p>
            <p className="text-text-muted text-sm mt-1">Subí tu primer APK haciendo click en "Nueva Versión"</p>
          </div>
        ) : (
          <div className="divide-y divide-primary/5">
            {versions.map(v => (
              <div key={v.id} className="p-5 hover:bg-rendly-surface-elevated/50 transition-colors">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className={`w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0 ${
                      v.is_latest ? 'bg-accent-green/10' : 'bg-rendly-surface-elevated'
                    }`}>
                      <Package className={`w-6 h-6 ${v.is_latest ? 'text-accent-green' : 'text-text-muted'}`} />
                    </div>
                    <div>
                      <div className="flex items-center gap-3 flex-wrap">
                        <span className="font-bold text-text-primary text-lg">v{v.version_name}</span>
                        {v.is_latest && (
                          <span className="px-2 py-0.5 rounded-full bg-accent-green/10 text-accent-green text-xs font-semibold">
                            ACTUAL
                          </span>
                        )}
                        <span className="text-text-muted text-sm">build {v.version_code}</span>
                      </div>
                      <div className="flex flex-wrap gap-4 mt-1 text-sm text-text-tertiary">
                        <span className="flex items-center gap-1"><Clock className="w-3.5 h-3.5" /> {formatDate(v.created_at)}</span>
                        {v.file_size_mb && <span className="flex items-center gap-1"><HardDrive className="w-3.5 h-3.5" /> {v.file_size_mb} MB</span>}
                        <span className="flex items-center gap-1"><Download className="w-3.5 h-3.5" /> {v.download_count || 0} descargas</span>
                      </div>
                      {v.changelog && (
                        <p className="text-text-secondary text-sm mt-2 whitespace-pre-line line-clamp-2">{v.changelog}</p>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-2 flex-shrink-0">
                    {!v.is_latest && (
                      <button
                        onClick={() => setAsLatest(v.id)}
                        className="px-3 py-2 rounded-lg bg-accent-green/10 text-accent-green text-sm font-medium hover:bg-accent-green/20 transition-colors"
                        title="Marcar como versión actual"
                      >
                        <CheckCircle className="w-4 h-4" />
                      </button>
                    )}
                    <button
                      onClick={() => deleteVersion(v.id)}
                      className="px-3 py-2 rounded-lg bg-red-500/10 text-red-400 text-sm font-medium hover:bg-red-500/20 transition-colors"
                      title="Eliminar versión"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Upload Modal */}
      {showUploadModal && (
        <UploadModal
          onClose={() => setShowUploadModal(false)}
          onUploaded={() => { setShowUploadModal(false); loadVersions() }}
        />
      )}
    </div>
  )
}

function UploadModal({ onClose, onUploaded }) {
  const fileRef = useRef(null)
  const [uploadMode, setUploadMode] = useState('external') // 'external' or 'local'
  const [file, setFile] = useState(null)
  const [externalUrl, setExternalUrl] = useState('')
  const [fileSizeMb, setFileSizeMb] = useState('')
  const [versionName, setVersionName] = useState('')
  const [versionCode, setVersionCode] = useState('')
  const [changelog, setChangelog] = useState('')
  const [minAndroid, setMinAndroid] = useState('8.0')
  const [isLatest, setIsLatest] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [progress, setProgress] = useState('')

  function handleFileChange(e) {
    const f = e.target.files[0]
    if (f) {
      setFile(f)
      // Try to extract version from filename like "merqora-v1.2.0.apk"
      const match = f.name.match(/v?(\d+\.\d+\.\d+)/)
      if (match && !versionName) setVersionName(match[1])
    }
  }

  async function handleUpload() {
    if (!versionName || !versionCode) {
      alert('Completá la versión y el código de build')
      return
    }

    if (uploadMode === 'external' && !externalUrl) {
      alert('Pegá el link directo de descarga del APK')
      return
    }

    if (uploadMode === 'local' && !file) {
      alert('Seleccioná un archivo APK')
      return
    }

    setUploading(true)
    try {
      let fileUrl = null
      let finalFileSizeMb = null
      let filePath = null

      if (uploadMode === 'external') {
        // Use external URL (GitHub Releases, etc.)
        setProgress('Guardando versión...')
        fileUrl = externalUrl
        finalFileSizeMb = fileSizeMb ? parseFloat(fileSizeMb) : null
        filePath = null // No storage path for external
      } else if (file) {
        // Upload to Supabase Storage (max 50MB)
        setProgress('Subiendo APK...')
        filePath = `merqora-v${versionName}.apk`
        finalFileSizeMb = Math.round(file.size / 1024 / 1024 * 10) / 10

        const { error: uploadError } = await supabase.storage
          .from('app-releases')
          .upload(filePath, file, { upsert: true, contentType: 'application/vnd.android.package-archive' })

        if (uploadError) throw uploadError

        const { data: urlData } = supabase.storage.from('app-releases').getPublicUrl(filePath)
        fileUrl = urlData.publicUrl
      }

      setProgress('Guardando metadata...')

      // If marking as latest, unset others
      if (isLatest) {
        await supabase.from('app_versions').update({ is_latest: false }).neq('id', 'none')
      }

      const { error: insertError } = await supabase.from('app_versions').insert({
        version_name: versionName,
        version_code: parseInt(versionCode),
        changelog: changelog || null,
        file_url: fileUrl,
        file_path: filePath,
        file_size_mb: finalFileSizeMb,
        min_android: minAndroid,
        is_latest: isLatest,
        download_count: 0
      })

      if (insertError) throw insertError

      setProgress('¡Listo!')
      setTimeout(() => onUploaded(), 500)
    } catch (err) {
      console.error('Upload error:', err)
      alert('Error al subir: ' + err.message)
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-rendly-surface rounded-3xl w-full max-w-lg max-h-[90vh] overflow-y-auto border border-primary/10">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-primary/5">
          <h2 className="text-xl font-bold text-text-primary flex items-center gap-3">
            <Upload className="w-6 h-6 text-primary" />
            Nueva Versión
          </h2>
          <button onClick={onClose} className="p-2 text-text-muted hover:text-text-primary transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-5">
          {/* Mode Toggle */}
          <div>
            <label className="text-text-secondary text-sm font-medium mb-2 block">Origen del APK</label>
            <div className="flex gap-2 p-1 bg-rendly-bg rounded-xl">
              <button
                onClick={() => setUploadMode('external')}
                className={`flex-1 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  uploadMode === 'external'
                    ? 'bg-primary text-white'
                    : 'text-text-secondary hover:text-text-primary'
                }`}
              >
                Link Externo (GitHub Releases)
              </button>
              <button
                onClick={() => setUploadMode('local')}
                className={`flex-1 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  uploadMode === 'local'
                    ? 'bg-primary text-white'
                    : 'text-text-secondary hover:text-text-primary'
                }`}
              >
                Upload Local (max 50MB)
              </button>
            </div>
          </div>

          {/* External URL Input */}
          {uploadMode === 'external' && (
            <>
              <div>
                <label className="text-text-secondary text-sm font-medium mb-2 block">Link directo de descarga *</label>
                <input
                  type="url"
                  value={externalUrl}
                  onChange={(e) => setExternalUrl(e.target.value)}
                  placeholder="https://github.com/.../releases/.../merqora.apk"
                  className="w-full px-4 py-3 rounded-xl bg-rendly-bg border border-primary/10 text-text-primary placeholder-text-muted focus:border-primary/30 focus:outline-none transition-colors"
                />
                <p className="text-text-muted text-xs mt-2">💡 Tip: Subí el APK a GitHub Releases y copiá el link directo aquí</p>
              </div>
              <div>
                <label className="text-text-secondary text-sm font-medium mb-2 block">Tamaño (MB)</label>
                <input
                  type="number"
                  step="0.1"
                  value={fileSizeMb}
                  onChange={(e) => setFileSizeMb(e.target.value)}
                  placeholder="110.5"
                  className="w-full px-4 py-3 rounded-xl bg-rendly-bg border border-primary/10 text-text-primary placeholder-text-muted focus:border-primary/30 focus:outline-none transition-colors"
                />
              </div>
            </>
          )}

          {/* File Upload */}
          {uploadMode === 'local' && (
            <div>
              <label className="text-text-secondary text-sm font-medium mb-2 block">Archivo APK</label>
              <div
                onClick={() => fileRef.current?.click()}
                className="border-2 border-dashed border-primary/20 rounded-2xl p-6 text-center cursor-pointer hover:border-primary/40 transition-colors"
              >
                <input ref={fileRef} type="file" accept=".apk" className="hidden" onChange={handleFileChange} />
                {file ? (
                  <div>
                    <CheckCircle className="w-8 h-8 text-accent-green mx-auto mb-2" />
                    <p className="text-text-primary font-medium">{file.name}</p>
                    <p className="text-text-muted text-sm">{(file.size / 1024 / 1024).toFixed(1)} MB</p>
                  </div>
                ) : (
                  <div>
                    <Upload className="w-8 h-8 text-text-muted mx-auto mb-2" />
                    <p className="text-text-secondary text-sm">Click para seleccionar APK</p>
                    <p className="text-text-muted text-xs mt-1">o arrastrá el archivo aquí (máx 50MB)</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Version Info */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-text-secondary text-sm font-medium mb-2 block">Versión *</label>
              <input
                type="text"
                value={versionName}
                onChange={e => setVersionName(e.target.value)}
                placeholder="1.0.0"
                className="w-full px-4 py-2.5 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
              />
            </div>
            <div>
              <label className="text-text-secondary text-sm font-medium mb-2 block">Build Code *</label>
              <input
                type="number"
                value={versionCode}
                onChange={e => setVersionCode(e.target.value)}
                placeholder="1"
                className="w-full px-4 py-2.5 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
              />
            </div>
          </div>

          {/* Min Android */}
          <div>
            <label className="text-text-secondary text-sm font-medium mb-2 block">Android mínimo</label>
            <select
              value={minAndroid}
              onChange={e => setMinAndroid(e.target.value)}
              className="w-full px-4 py-2.5 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary focus:outline-none focus:border-primary transition-colors"
            >
              <option value="7.0">Android 7.0 (Nougat)</option>
              <option value="8.0">Android 8.0 (Oreo)</option>
              <option value="9.0">Android 9.0 (Pie)</option>
              <option value="10.0">Android 10</option>
              <option value="11.0">Android 11</option>
              <option value="12.0">Android 12</option>
            </select>
          </div>

          {/* Changelog */}
          <div>
            <label className="text-text-secondary text-sm font-medium mb-2 block">Changelog</label>
            <textarea
              value={changelog}
              onChange={e => setChangelog(e.target.value)}
              placeholder="¿Qué hay de nuevo en esta versión?"
              rows={4}
              className="w-full px-4 py-2.5 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors resize-none"
            />
          </div>

          {/* Is Latest */}
          <label className="flex items-center gap-3 cursor-pointer">
            <div className={`w-5 h-5 rounded-md border-2 flex items-center justify-center transition-colors ${
              isLatest ? 'bg-primary border-primary' : 'border-text-muted'
            }`}
              onClick={() => setIsLatest(!isLatest)}
            >
              {isLatest && <CheckCircle className="w-3.5 h-3.5 text-white" />}
            </div>
            <span className="text-text-secondary text-sm" onClick={() => setIsLatest(!isLatest)}>Marcar como versión actual (la que se muestra para descargar)</span>
          </label>
        </div>

        {/* Footer */}
        <div className="p-6 border-t border-primary/5 flex items-center justify-between">
          {progress ? (
            <div className="flex items-center gap-2 text-text-secondary text-sm">
              <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />
              {progress}
            </div>
          ) : (
            <div />
          )}
          <div className="flex gap-3">
            <button
              onClick={onClose}
              disabled={uploading}
              className="px-5 py-2.5 rounded-xl text-text-secondary hover:text-text-primary transition-colors disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              onClick={handleUpload}
              disabled={uploading || !versionName || !versionCode}
              className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-primary hover:bg-primary-dark text-white font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {uploading ? (
                <><div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> Subiendo...</>
              ) : (
                <><Save className="w-4 h-4" /> Publicar</>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
