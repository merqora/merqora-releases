import ProductCard from '@/components/ProductCard'
import HeroSlider from '@/components/HeroSlider'
import AppDownload from '@/components/AppDownload'
import { getRecentPosts, getFeaturedPosts } from '@/lib/posts'
import { getLatestVersion } from '@/lib/app'
import styles from './page.module.css'

export default async function Home() {
  const [allPosts, featuredPosts, latestVersion] = await Promise.all([
    getRecentPosts(50),
    getFeaturedPosts(3),
    getLatestVersion(),
  ])

  return (
    <div className={styles.page}>
      <HeroSlider featuredPosts={featuredPosts} />

      <AppDownload version={latestVersion} />

      <section id="productos" className={styles.section}>
        <div className="container">
          <h2 className={styles.title}>Todos los productos</h2>
          <div className={styles.grid}>
            {allPosts.map((post) => (
              <ProductCard key={post.id} post={post} />
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
