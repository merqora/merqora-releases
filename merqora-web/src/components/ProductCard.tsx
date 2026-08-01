import Link from 'next/link'
import type { Post } from '@/lib/types'
import ShimmerImage from './ShimmerImage'
import StarRating from './StarRating'
import styles from './ProductCard.module.css'

export default function ProductCard({ post }: { post: Post }) {
  const cover = post.images[0] || ''
  const priceFormatted = new Intl.NumberFormat('es-UY', {
    style: 'currency',
    currency: 'UYU',
    maximumFractionDigits: 0,
  }).format(post.price)

  const previousPriceFormatted = post.previousPrice
    ? new Intl.NumberFormat('es-UY', {
        style: 'currency',
        currency: 'UYU',
        maximumFractionDigits: 0,
      }).format(post.previousPrice)
    : null

  const discount = post.previousPrice
    ? Math.round((1 - post.price / post.previousPrice) * 100)
    : 0

  return (
    <Link href={`/p/${post.id}`} className={`${styles.card} card`}>
      <div className={styles.imageWrap}>
        {cover ? (
          <ShimmerImage src={cover} alt={post.title} />
        ) : (
          <div className={styles.placeholder} />
        )}
        <span className={styles.badgeRow}>
          {post.isNew && <span className={styles.badge}>Nuevo</span>}
          {post.freeShipping && (
            <span className={`${styles.badge} ${styles.badgeShipping}`}>
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M1 3h15v13H1zM16 8h4l3 3v5h-7zM5.5 21a2 2 0 1 0 0-4 2 2 0 0 0 0 4zM18.5 21a2 2 0 1 0 0-4 2 2 0 0 0 0 4z" />
              </svg>
              Envío gratis
            </span>
          )}
        </span>
        {discount > 0 && (
          <span className={`${styles.badge} ${styles.badgeDisc}`}>-{discount}%</span>
        )}
      </div>

      <div className={styles.body}>
        <h3 className={styles.title}>{post.title}</h3>

        <div className={styles.priceRow}>
          <span className={styles.price}>{priceFormatted}</span>
          {previousPriceFormatted && (
            <span className={styles.oldPrice}>{previousPriceFormatted}</span>
          )}
        </div>

        {post.ratingAvg && post.ratingAvg > 0 && (
          <div className={styles.ratingRow}>
            <StarRating rating={post.ratingAvg} size={12} />
            <span className={styles.ratingCount}>{post.ratingAvg.toFixed(1)}</span>
          </div>
        )}

        {post.condition && (
          <span className={`${styles.condition} ${
            post.condition === 'new'
              ? styles.conditionNew
              : post.condition === 'like_new'
                ? styles.conditionLikeNew
                : styles.conditionUsed
          }`}>
            {post.condition === 'new'
              ? 'Nuevo'
              : post.condition === 'like_new'
                ? 'Como nuevo'
                : 'Usado'}
          </span>
        )}

        <div className={styles.footer}>
          <div className={styles.seller}>
            {post.userAvatar ? (
              <img src={post.userAvatar} alt="" className={styles.avatar} />
            ) : (
              <div className={styles.avatarPlaceholder}>
                {post.username[0]?.toUpperCase() || '?'}
              </div>
            )}
            <span className={styles.sellerName}>
              {post.userStoreName || post.username}
              {post.isUserVerified && (
                <svg className={styles.verifiedIcon} width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" aria-label="Vendedor verificado">
                  <path d="M12 2l2.4 1.8 3-.1 1 2.8 2.6 1.5-.7 2.9.7 2.9-2.6 1.5-1 2.8-3-.1L12 22l-2.4-1.8-3 .1-1-2.8L3 16l.7-2.9L3 10.2l2.6-1.5 1-2.8 3 .1L12 2z" />
                  <path d="M9 12l2 2 4-4" />
                </svg>
              )}
            </span>
          </div>
          <div className={styles.metaRight}>
            {post.viewsCount > 0 && (
              <span className={styles.views}>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                {post.viewsCount > 999
                  ? `${(post.viewsCount / 1000).toFixed(1)}k`
                  : post.viewsCount}
              </span>
            )}
            {post.likesCount > 0 && (
              <span className={styles.likesCount}>
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" />
                </svg>
                {post.likesCount}
              </span>
            )}
          </div>
        </div>
      </div>
    </Link>
  )
}
