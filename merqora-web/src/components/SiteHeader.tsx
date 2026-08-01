'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import type { AppVersion } from '@/lib/app'
import styles from './SiteHeader.module.css'

export default function SiteHeader({
  version,
}: {
  version: AppVersion | null
}) {
  const pathname = usePathname()
  const downloadUrl =
    version?.file_url || (version ? `/api/download-apk/${version.id}` : '#descargar')

  return (
    <header className={styles.header}>
      <div className={`container ${styles.inner}`}>
        <Link href="/" className={styles.logo} aria-label="Mercora — Inicio">
          <span className={styles.logoMark}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
            </svg>
          </span>
          <span className={styles.logoText}>Mercora</span>
        </Link>

        <form
          action="/"
          method="get"
          className={styles.searchForm}
          role="search"
        >
          <select
            name="cat"
            className={styles.categorySelect}
            defaultValue="todos"
            aria-label="Categoría"
            onChange={(e) => e.currentTarget.form?.submit()}
          >
            <option value="todos">Todas las categorías</option>
            <option value="ropa">Ropa y Accesorios</option>
            <option value="zapatos">Zapatos</option>
            <option value="accesorios">Accesorios</option>
            <option value="electronica">Electrónica</option>
            <option value="hogar">Hogar y Decoración</option>
            <option value="deportes">Deportes</option>
          </select>
          <input
            type="search"
            name="q"
            className={styles.searchInput}
            placeholder="Buscá productos, marcas y más..."
            aria-label="Buscar productos"
            autoComplete="off"
          />
          <button type="submit" className={styles.searchButton} aria-label="Buscar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
              <circle cx="11" cy="11" r="7" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
          </button>
        </form>

        <nav className={styles.actions} aria-label="Navegación principal">
          <a href={downloadUrl} className={styles.downloadLink} target="_blank" rel="noopener noreferrer">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="7" y="2" width="10" height="20" rx="2" />
              <path d="M11 18h2" />
            </svg>
            <span>
              <small>Descargá la</small>
              <strong>App</strong>
            </span>
          </a>
          <Link
            href={pathname === '/' ? '/#descargar' : '/'}
            className={styles.sellLink}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 5v14M5 12h14" />
            </svg>
            Vender
          </Link>
        </nav>
      </div>
    </header>
  )
}
