import Link from 'next/link'
import type { Post } from '@/lib/types'
import ProductCard from './ProductCard'
import styles from './ProductRow.module.css'

export default function ProductRow({
  title,
  posts,
  seeAllHref,
  id,
}: {
  title: string
  posts: Post[]
  seeAllHref?: string
  id?: string
}) {
  if (posts.length === 0) return null

  return (
    <section id={id} className={styles.section}>
      <div className="container">
        <div className={styles.head}>
          <h2 className={styles.title}>{title}</h2>
          {seeAllHref && (
            <Link href={seeAllHref} className={styles.seeAll}>
              Ver todo
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                <path d="M5 12h14M12 5l7 7-7 7" />
              </svg>
            </Link>
          )}
        </div>
        <div className={styles.row}>
          {posts.map((post) => (
            <div key={post.id} className={styles.slot}>
              <ProductCard post={post} />
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
