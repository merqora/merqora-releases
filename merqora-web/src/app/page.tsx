import ProductCard from '@/components/ProductCard'
import HeroSlider from '@/components/HeroSlider'
import AppDownload from '@/components/AppDownload'
import SiteHeader from '@/components/SiteHeader'
import CategoryNav from '@/components/CategoryNav'
import ProductRow from '@/components/ProductRow'
import TrustStrip from '@/components/TrustStrip'
import SiteFooter from '@/components/SiteFooter'
import {
  getRecentPosts,
  getFeaturedPosts,
  getPopularPosts,
  getDeals,
  searchPosts,
  getAllCategoriesWithCounts,
} from '@/lib/posts'
import { getLatestVersion } from '@/lib/app'
import styles from './page.module.css'

export default async function Home({
  searchParams,
}: {
  searchParams: Promise<{ q?: string | string[]; cat?: string | string[] }>
}) {
  const sp = await searchParams
  const query = typeof sp.q === 'string' ? sp.q.trim() : ''
  const category = typeof sp.cat === 'string' ? sp.cat.trim() : 'todos'
  const isFiltering = Boolean(query) || (category !== '' && category !== 'todos')

  const [latestVersion, featuredPosts, popularPosts, dealPosts, recentPosts, categoryCounts, results] =
    await Promise.all([
      getLatestVersion(),
      getFeaturedPosts(3),
      getPopularPosts(10),
      getDeals(10),
      getRecentPosts(12),
      getAllCategoriesWithCounts(),
      isFiltering ? searchPosts(query, category, 60) : Promise.resolve(null),
    ])

  const activeCategory = isFiltering && category !== 'todos' ? category : 'todos'

  return (
    <div className={styles.page}>
      <SiteHeader version={latestVersion} />
      <CategoryNav counts={categoryCounts} activeCategory={activeCategory} />
      <HeroSlider featuredPosts={featuredPosts} />

      {isFiltering ? (
        <section className={styles.section}>
          <div className="container">
            <div className={styles.searchHead}>
              <h2 className={styles.title}>
                {query ? (
                  <>
                    Resultados para <span className={styles.query}>"{query}"</span>
                  </>
                ) : (
                  <>Todos los productos</>
                )}
              </h2>
              <p className={styles.resultCount}>
                {results?.length || 0} producto{(results?.length ?? 0) === 1 ? '' : 's'}
              </p>
            </div>
            {results && results.length > 0 ? (
              <div className={styles.grid}>
                {results.map((post) => (
                  <ProductCard key={post.id} post={post} />
                ))}
              </div>
            ) : (
              <div className={styles.empty}>
                <p>No encontramos productos para tu búsqueda.</p>
                <a href="/" className={styles.emptyLink}>
                  Ver todos los productos
                </a>
              </div>
            )}
          </div>
        </section>
      ) : (
        <>
          <ProductRow title="Lo más vendido" posts={popularPosts} id="populares" />
          <ProductRow title="Ofertas del día" posts={dealPosts} id="ofertas" seeAllHref="/?cat=todos" />
          <ProductRow title="Novedades" posts={recentPosts} id="novedades" />
        </>
      )}

      <AppDownload version={latestVersion} />
      <TrustStrip />
      <SiteFooter />
    </div>
  )
}
