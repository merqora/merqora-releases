import Link from 'next/link'
import styles from './CategoryNav.module.css'

const CATEGORY_ICONS: Record<string, string> = {
  ropa: 'M7 21l5-16 5 16M4 16h16M8 8h8',
  zapatos: 'M3 18l1.5-9h15L21 18a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM7 12h10M9 9v3M15 9v3',
  accesorios: 'M12 6a3 3 0 1 0 3 3M5 21l4.5-9.5L14 16l6-5.5',
  electronica: 'M9 2h6M10 2v6l-3 6a3 3 0 0 0 3 5h4a3 3 0 0 0 3-5l-3-6V2M7 20h10',
  hogar: 'M3 11l9-8 9 8M5 10v10h14V10M9 20v-6h6v6',
  deportes: 'M12 2a10 10 0 1 0 10 10M2 12h20M12 2c2.5 3 4 6.5 4 10s-1.5 7-4 10c-2.5-3-4-6.5-4-10s1.5-7 4-10z',
}

export default function CategoryNav({
  counts,
  activeCategory = 'todos',
}: {
  counts: Record<string, number>
  activeCategory?: string
}) {
  const total = Object.values(counts).reduce((a, b) => a + b, 0)

  const categories = [
    { id: 'todos', name: 'Todos', count: total },
    { id: 'ropa', name: 'Ropa y Accesorios' },
    { id: 'zapatos', name: 'Zapatos' },
    { id: 'accesorios', name: 'Accesorios' },
    { id: 'electronica', name: 'Electrónica' },
    { id: 'hogar', name: 'Hogar' },
    { id: 'deportes', name: 'Deportes' },
  ].map((c) => ({ ...c, count: counts[c.id] || 0 }))

  return (
    <nav className={styles.nav} aria-label="Categorías">
      <div className={`container ${styles.inner}`}>
        {categories.map((cat) => {
          const active = activeCategory === cat.id
          return (
            <Link
              key={cat.id}
              href={cat.id === 'todos' ? '/' : `/?cat=${cat.id}`}
              className={`${styles.item} ${active ? styles.active : ''}`}
            >
              {cat.id !== 'todos' && (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                  <path d={CATEGORY_ICONS[cat.id] || ''} />
                </svg>
              )}
              <span>{cat.name}</span>
              <small>{cat.count}</small>
            </Link>
          )
        })}
      </div>
    </nav>
  )
}
