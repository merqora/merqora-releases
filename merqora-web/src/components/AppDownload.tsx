import type { AppVersion } from '@/lib/app'
import styles from './AppDownload.module.css'

export default function AppDownload({ version }: { version: AppVersion | null }) {
  if (!version) return null

  const downloadUrl = version.file_url || `/api/download-apk/${version.id}`

  return (
    <section className={styles.section}>
      <div className="container">
        <div className={styles.card}>
          <div className={styles.badge}>Vinzay App</div>
          <h2 className={styles.title}>Descargá la app</h2>
          <p className={styles.subtitle}>
            Comprá y vendé productos únicos desde tu celular.
          </p>
          <div className={styles.versionInfo}>
            <span className={styles.version}>v{version.version_name}</span>
            <span className={styles.size}>
              {version.file_size_mb ? `${version.file_size_mb} MB` : ''}
            </span>
            <span className={styles.android}>Android {version.min_android || '8.0'}+</span>
            <span className={styles.downloads}>
              {version.download_count > 0
                ? `${version.download_count.toLocaleString()} descargas`
                : ''}
            </span>
          </div>
          {version.changelog && (
            <p className={styles.changelog}>{version.changelog}</p>
          )}
          <a
            href={downloadUrl}
            className={styles.downloadBtn}
            target="_blank"
            rel="noopener noreferrer"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
            </svg>
            Descargar APK
          </a>
          <p className={styles.trust}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/>
            </svg>
            APK firmado digitalmente. No requiere Google Play.
          </p>
        </div>
      </div>
    </section>
  )
}
