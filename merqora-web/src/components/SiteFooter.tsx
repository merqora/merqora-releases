import Link from 'next/link'
import styles from './SiteFooter.module.css'

const COLUMNS = [
  {
    title: 'Comprá',
    links: [
      { label: 'Cómo funciona', href: '/#productos' },
      { label: 'Todos los productos', href: '/' },
      { label: 'Ofertas', href: '/?cat=todos' },
      { label: 'Categorías', href: '/' },
    ],
  },
  {
    title: 'Vendé',
    links: [
      { label: 'Publicá tu producto', href: '/' },
      { label: 'Consejos para vendedores', href: '/' },
      { label: 'Reputación y verificación', href: '/' },
    ],
  },
  {
    title: 'Ayuda',
    links: [
      { label: 'Centro de ayuda', href: '/' },
      { label: 'Compra protegida', href: '/' },
      { label: 'Envios', href: '/' },
      { label: 'Contáctanos', href: '/' },
    ],
  },
  {
    title: 'Mercora',
    links: [
      { label: 'Acerca de nosotros', href: '/' },
      { label: 'Términos y condiciones', href: '/' },
      { label: 'Política de privacidad', href: '/' },
      { label: 'Descargar la app', href: '/#descargar' },
    ],
  },
]

export default function SiteFooter() {
  return (
    <footer className={styles.footer}>
      <div className={`container ${styles.top}`}>
        <div className={styles.brand}>
          <Link href="/" className={styles.logo}>
            <span className={styles.logoMark}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
              </svg>
            </span>
            <span className={styles.logoText}>Mercora</span>
          </Link>
          <p className={styles.tagline}>
            El marketplace social donde comprás y vendés productos únicos en Uruguay y
            Latinoamérica.
          </p>
          <div className={styles.socials}>
            <a href="https://www.instagram.com" target="_blank" rel="noopener noreferrer" aria-label="Instagram" className={styles.social}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round">
                <rect x="2" y="2" width="20" height="20" rx="5" />
                <circle cx="12" cy="12" r="4.5" />
                <circle cx="17.5" cy="6.5" r="1" fill="currentColor" stroke="none" />
              </svg>
            </a>
            <a href="https://www.tiktok.com" target="_blank" rel="noopener noreferrer" aria-label="TikTok" className={styles.social}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19.59 6.69a4.83 4.83 0 0 1-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 0 1-5.2 1.74 2.89 2.89 0 0 1 2.31-4.64 2.93 2.93 0 0 1 .88.13V9.4a6.84 6.84 0 0 0-1-.05A6.33 6.33 0 0 0 5 20.1a6.34 6.34 0 0 0 10.86-4.43v-7a8.16 8.16 0 0 0 4.77 1.52v-3.4a4.85 4.85 0 0 1-1-.1z" />
              </svg>
            </a>
            <a href="https://www.x.com" target="_blank" rel="noopener noreferrer" aria-label="X" className={styles.social}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18.9 1.15h3.68l-8.04 9.19L24 22.85h-7.41l-5.8-7.58-6.64 7.58H.47l8.6-9.83L0 1.15h7.59l5.24 6.93 6.07-6.93zM17.61 20.64h2.04L6.49 3.24H4.3l13.31 17.4z" />
              </svg>
            </a>
          </div>
        </div>

        {COLUMNS.map((col) => (
          <div key={col.title} className={styles.column}>
            <h3 className={styles.colTitle}>{col.title}</h3>
            <ul className={styles.links}>
              {col.links.map((l) => (
                <li key={l.label}>
                  <Link href={l.href} className={styles.link}>
                    {l.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      <div className={`container ${styles.bottom}`}>
        <p>© {new Date().getFullYear()} Mercora. Todos los derechos reservados.</p>
        <p>Hecho con dedicación en Uruguay.</p>
      </div>
    </footer>
  )
}
