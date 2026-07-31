import type { Metadata } from 'next'
import Link from 'next/link'
import { createServerSupabaseClient } from '@/lib/supabase/server'
import { getPostById, getUserById, getPostsByCategory, getRecentPosts } from '@/lib/posts'
import ProductCard from '@/components/ProductCard'
import ProductGallery from '@/components/ProductGallery'
import ProductDetailsClient from '@/components/ProductDetailsClient'
import styles from './page.module.css'

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>
}): Promise<Metadata> {
  const { id } = await params
  const supabase = await createServerSupabaseClient()

  const { data: post } = await supabase
    .from('posts')
    .select('title, description, price, images')
    .eq('id', id)
    .single()

  if (!post) {
    return { title: 'Producto no encontrado | Mercora' }
  }

  const title = post.title || 'Producto en Mercora'
  const description = post.description || 'Descubrí este Producto en Mercora'
  const image = post.images?.[0] || ''
  const siteUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://mercora.app'

  const ogImage = image || '/og-default.svg'

  return {
    title: `${title} | Mercora`,
    description,
    openGraph: {
      title: `${title} — Mercora`,
      description,
      url: `${siteUrl}/p/${id}`,
      siteName: 'Mercora',
      images: [{ url: ogImage, width: 1200, height: 630 }],
      type: 'website',
      ...(post.price != null && {
        priceAmount: post.price,
        priceCurrency: 'UYU' as const,
      }),
    },
    twitter: {
      card: 'summary_large_image',
      title: `${title} — Mercora`,
      description,
      images: [ogImage],
    },
  }
}

export default async function ProductPage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = await params
  const post = await getPostById(id)

  if (!post) {
    return (
      <div className={styles.notFound}>
        <h1>Producto no encontrado</h1>
        <p>El producto que buscas no existe o fue eliminado.</p>
        <Link href="/" className="btn-primary">
          Explorar productos
        </Link>
      </div>
    )
  }

  const [user, allPosts] = await Promise.all([
    post.userId ? getUserById(post.userId) : null,
    getRecentPosts(20),
  ])

  const filteredAll = allPosts
    .filter((p) => p.id !== post.id)
    .slice(0, 20)

  return (
    <div className={styles.page}>
      <div className="container">
        <nav className={styles.breadcrumb}>
          <Link href="/">Inicio</Link>
          <span>/</span>
          <span className={styles.current}>{post.title}</span>
        </nav>

        <div className={styles.layout}>
          <ProductGallery images={post.images} title={post.title} />
          <ProductDetailsClient post={post} user={user} />
        </div>

        {filteredAll.length > 0 && (
          <section className={styles.allPosts}>
            <h2 className={styles.allPostsTitle}>Todas las publicaciones</h2>
            <div className={styles.allPostsGrid}>
              {filteredAll.map((p) => (
                <ProductCard key={p.id} post={p} />
              ))}
            </div>
          </section>
        )}
      </div>
    </div>
  )
}
