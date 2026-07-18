'use client'

import type { Post } from '@/lib/types'
import StarRating from '@/components/StarRating'
import ShareButton from '@/components/ShareButton'
import { CATEGORY_NAMES } from '@/lib/types'
import styles from '@/app/p/[id]/page.module.css'

function formatCount(n: number): string {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + 'M'
  if (n >= 1_000) return (n / 1_000).toFixed(1) + 'k'
  return String(n)
}

function conditionLabel(cond: string): string {
  switch (cond) {
    case 'new': return 'Nuevo'
    case 'like_new': return 'Como nuevo'
    default: return 'Usado'
  }
}

export default function ProductDetailsClient({ post, user }: { post: Post; user: any }) {
  const priceFormatted = new Intl.NumberFormat('es-UY', { style: 'currency', currency: 'UYU' }).format(post.price)
  const previousPriceFormatted = post.previousPrice
    ? new Intl.NumberFormat('es-UY', { style: 'currency', currency: 'UYU' }).format(post.previousPrice)
    : null
  const discount = post.previousPrice ? Math.round((1 - post.price / post.previousPrice) * 100) : 0
  const savings = post.previousPrice ? post.previousPrice - post.price : 0
  const savingsFormatted = savings
    ? new Intl.NumberFormat('es-UY', { style: 'currency', currency: 'UYU' }).format(savings)
    : null

  const siteUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://vinzay.app'
  const catName = post.category ? CATEGORY_NAMES[post.category] || post.category : ''

  return (
    <div className={styles.info}>
      <div className={styles.topInfo}>
        {post.isNew && <span className={styles.badgeNuevo}>Nuevo</span>}
        {post.reviewsCount > 0 && (
          <div className={styles.topRating}>
            <StarRating rating={post.ratingAvg || 0} size={14} />
            {post.ratingAvg ? <span className={styles.ratingNum}>{post.ratingAvg.toFixed(1)}</span> : null}
            <span className={styles.ratingCount}>({formatCount(post.reviewsCount)})</span>
          </div>
        )}
      </div>

      <h1 className={styles.title}>{post.title}</h1>

      <div className={styles.badgesRow}>
        {post.viewsCount > 50 && (
          <span className={styles.badgeHot}>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 23c-1.5 0-3-.5-4.2-1.4C3.6 18.3 2 14.5 2 11c0-4 3-8 6-9 0 3 1.5 5 4 6s4 3 4 6c0 2-.8 4-2.5 5.7C12.5 22 12.3 23 12 23z"/></svg>
            más visto
          </span>
        )}
        {catName && (
          <span className={styles.badgeCat}>#1 en {catName}</span>
        )}
      </div>

      <div className={styles.actionsRow}>
        <ShareButton postId={post.id} className={styles.actionBtn} />
        <a
          href={`https://wa.me/?text=${encodeURIComponent(`Mirá este Producto en Vinzay: ${post.title} - ${siteUrl}/p/${post.id}`)}`}
          target="_blank"
          rel="noopener noreferrer"
          className={styles.actionBtn}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="22" y1="2" x2="11" y2="13" /><polygon points="22 2 15 22 11 13 2 9 22 2" />
          </svg>
          <span className={styles.actionCount}>{post.sharesCount > 0 ? formatCount(post.sharesCount) : ''}</span>
        </a>
      </div>

      <div className={styles.priceCard}>
        <div className={styles.priceSection}>
          {previousPriceFormatted && (
            <span className={styles.oldPriceInLine}>{previousPriceFormatted}</span>
          )}
          <div className={styles.priceRow}>
            <span className={styles.price}>{priceFormatted}</span>
            {discount > 0 && <span className={styles.discount}>-{discount}%</span>}
          </div>
          {savingsFormatted && (
            <span className={styles.savings}>Ahorras {savingsFormatted}</span>
          )}
        </div>
        {post.freeShipping && (
          <div className={styles.freeShipping}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="1" y="3" width="15" height="13" /><polygon points="16 8 20 8 23 11 23 16 16 16 16 8" /><circle cx="5.5" cy="18.5" r="2.5" /><circle cx="18.5" cy="18.5" r="2.5" /></svg>
            Envío gratis
          </div>
        )}
        {post.previousPrice && (
          <div className={styles.priceHistory}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" /></svg>
            <span>Mejor precio en los últimos 30 días</span>
          </div>
        )}
      </div>

      <div className={styles.statsRow}>
        <span className={styles.statPill}>
          <span className={styles.statPillDot} />
          <span>{((post.viewsCount * 7 + 13) % 25 + 3)} personas viendo ahora</span>
        </span>
        <span className={`${styles.statPill} ${styles.statPillFire}`}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 23c-1.5 0-3-.5-4.2-1.4C3.6 18.3 2 14.5 2 11c0-4 3-8 6-9 0 3 1.5 5 4 6s4 3 4 6c0 2-.8 4-2.5 5.7C12.5 22 12.3 23 12 23z"/></svg>
          <span>{Math.max(post.reviewsCount * 3 + 5, 1)} vendidos</span>
        </span>
      </div>

      <div className={styles.description}>
        <h3>Descripción</h3>
        <p>{post.description}</p>
      </div>

      <div className={styles.metaCard}>
        <div className={styles.metaGrid}>
          {post.condition && (
            <div className={styles.metaItem}>
              <span className={styles.metaLabel}>Estado</span>
              <span className={styles.metaValue}>{conditionLabel(post.condition)}</span>
            </div>
          )}
          {post.stock != null && (
            <div className={styles.metaItem}>
              <span className={styles.metaLabel}>Stock</span>
              <span className={styles.metaValue}>{post.stock > 0 ? `${post.stock} unidades` : 'Agotado'}</span>
            </div>
          )}
          {post.allowOffers && (
            <div className={styles.metaItem}>
              <span className={styles.metaLabel}>Ofertas</span>
              <span className={styles.metaValue}>Acepta ofertas</span>
            </div>
          )}
          {post.viewsCount > 0 && (
            <div className={styles.metaItem}>
              <span className={styles.metaLabel}>Visto</span>
              <span className={styles.metaValue}>
                {post.viewsCount > 999 ? `${(post.viewsCount / 1000).toFixed(1)}k` : post.viewsCount} veces
              </span>
            </div>
          )}
        </div>
      </div>

      {post.stock != null && (
        <div className={styles.inventorySection}>
          <div className={styles.inventoryHeader}>
            <span className={styles.inventoryLabel}>Disponibilidad</span>
            <span className={styles.inventoryCount} style={{ color: post.stock === 0 ? '#ef4444' : post.stock <= 3 ? 'var(--amber-500)' : '#22c55e' }}>
              {post.stock === 0 ? 'Agotado' : post.stock <= 3 ? `Quedan ${post.stock}` : `${post.stock} en stock`}
            </span>
          </div>
          <div className={styles.inventoryBar}>
            <div
              className={`${styles.inventoryFill} ${post.stock === 0 ? styles.inventoryFillLow : post.stock <= 3 ? styles.inventoryFillMid : styles.inventoryFillHigh}`}
              style={{ width: `${Math.min((post.stock / 20) * 100, 100)}%` }}
            />
          </div>
          {post.stock > 0 && post.stock <= 3 && (
            <p className={styles.inventoryText}>¡Últimas unidades! Stock bajo</p>
          )}
        </div>
      )}

      <div className={styles.seller}>
        <div className={styles.sellerAvatar}>
          {post.userAvatar ? (
            <img src={post.userAvatar} alt="" />
          ) : (
            <div className={styles.avatarPlaceholder}>{post.username[0]?.toUpperCase() || '?'}</div>
          )}
        </div>
        <div className={styles.sellerInfo}>
          <p className={styles.sellerName}>
            {post.userStoreName || post.username}
            {post.isUserVerified && (
              <svg className={styles.verifiedBadge} width="14" height="14" viewBox="0 0 24 24" fill="#1d9bf0"><path d="M22.5 12.5c0-1.58-.875-2.95-2.148-3.6.154-.435.238-.905.238-1.4 0-2.21-1.71-3.998-3.818-3.998-.47 0-.92.084-1.336.25C14.818 2.415 13.51 1.5 12 1.5s-2.816.917-3.437 2.25c-.415-.165-.866-.25-1.336-.25-2.11 0-3.818 1.79-3.818 4 0 .494.083.964.237 1.4-1.272.65-2.147 2.018-2.147 3.6 0 1.495.782 2.798 1.942 3.486-.02.17-.032.34-.032.514 0 2.21 1.708 4 3.818 4 .47 0 .92-.086 1.335-.25.62 1.334 1.926 2.25 3.437 2.25 1.512 0 2.818-.916 3.437-2.25.415.163.865.248 1.336.248 2.11 0 3.818-1.79 3.818-4 0-.174-.012-.344-.033-.513 1.158-.687 1.943-1.99 1.943-3.484zm-6.616-3.334l-4.334 6.5a.75.75 0 0 1-1.174.144l-2.5-2.5a.75.75 0 1 1 1.06-1.06l1.756 1.754 3.756-5.636a.75.75 0 1 1 1.436.798z"/></svg>
            )}
          </p>
          <p className={styles.sellerLabel}>Vendedor</p>
          {user?.ubicacion && <p className={styles.sellerLocation}>{user.ubicacion}</p>}
        </div>
        <div className={styles.sellerStats}>
          <div className={styles.sellerStat}>
            <span className={styles.sellerStatValue}>{user?.reputacion ?? 0}%</span>
            <span className={styles.sellerStatLabel}>Reputación</span>
          </div>
          <div className={styles.sellerStat}>
            <span className={styles.sellerStatValue}>{post.reviewsCount}</span>
            <span className={styles.sellerStatLabel}>Ventas</span>
          </div>
        </div>
        <svg className={styles.sellerArrow} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="m9 18 6-6-6-6" /></svg>
      </div>

      <div className={styles.featuresGrid}>
        <div className={`${styles.featureChip} ${post.freeShipping ? styles.featureActive : ''}`}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="1" y="3" width="15" height="13" /><polygon points="16 8 20 8 23 11 23 16 16 16 16 8" /><circle cx="5.5" cy="18.5" r="2.5" /><circle cx="18.5" cy="18.5" r="2.5" /></svg>
          <span>Envío gratis</span>
        </div>
        <div className={`${styles.featureChip} ${post.isUserVerified ? styles.featureActive : ''}`}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" /><polyline points="22 4 12 14.01 9 11.01" /></svg>
          <span>Verificado</span>
        </div>
        <div className={`${styles.featureChip} ${false ? styles.featureActive : ''}`}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 12a9 9 0 1 1-9-9" /><polyline points="22 4 12 14.01 9 11.01" /></svg>
          <span>Devolución</span>
        </div>
      </div>

      <div className={styles.detailsSection}>
        <h3>Detalles del producto</h3>
        <div className={styles.detailRows}>
          <div className={styles.detailRow}><span>Condición</span><span>{conditionLabel(post.condition)}</span></div>
          {catName && <div className={styles.detailRow}><span>Categoría</span><span>{catName}</span></div>}
          <div className={styles.detailRow}><span>Disponibilidad</span><span>{post.stock && post.stock > 0 ? 'En stock' : post.stock === 0 ? 'Agotado' : 'Consultar'}</span></div>
          <div className={styles.detailRow}><span>Garantía</span><span>Sin garantía</span></div>
          <div className={styles.detailRow}><span>Devolución</span><span>No aceptada</span></div>
        </div>
      </div>

      <div className={styles.skuSection}>
        <span className={styles.sku}>SKU: {post.id.slice(0, 8).toUpperCase()}</span>
      </div>

      <div className={styles.trustSection}>
        <h3>Compra segura</h3>
        <div className={styles.trustGrid}>
          <div className={`${styles.trustItem} ${styles.trustItemActive}`}>
            <svg className={`${styles.trustIcon} ${styles.trustIconActive}`} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <span>Compra protegida</span>
          </div>
          <div className={`${styles.trustItem} ${styles.trustItemActive}`}>
            <svg className={`${styles.trustIcon} ${styles.trustIconActive}`} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            <span>Pago seguro SSL</span>
          </div>
          <div className={styles.trustItem}>
            <svg className={styles.trustIcon} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 12a9 9 0 1 1-9-9"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            <span>Garantía de calidad</span>
          </div>
          <div className={styles.trustItem}>
            <svg className={styles.trustIcon} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/></svg>
            <span>Atención al cliente</span>
          </div>
        </div>
      </div>

      <div className={styles.paymentSection}>
        <h3>Medios de pago</h3>
        <div className={styles.paymentChips}>
          <span className={styles.paymentChip}>Tarjeta</span>
          <span className={styles.paymentChip}>Transferencia</span>
          <span className={styles.paymentChip}>Efectivo</span>
          <span className={styles.paymentChip}>Mercado Pago</span>
        </div>
      </div>

      <div className={styles.reviewsSection}>
        <h3>Opiniones</h3>
        {post.reviewsCount > 0 ? (
          <div className={styles.reviewsBody}>
            <div className={styles.reviewsLeft}>
              <span className={styles.reviewsBigRating}>{post.ratingAvg?.toFixed(1) || '0.0'}</span>
              <StarRating rating={post.ratingAvg || 0} size={14} />
              <span className={styles.reviewsTotal}>{post.reviewsCount} opiniones</span>
            </div>
            <div className={styles.reviewsBars}>
              {[5, 4, 3, 2, 1].map((star) => (
                <div key={star} className={styles.reviewBarRow}>
                  <span className={styles.reviewBarStar}>{star}</span>
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="#f59e0b"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg>
                  <div className={styles.reviewBarTrack}>
                    <div className={styles.reviewBarFill} style={{ width: `${star * 20}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className={styles.reviewsEmpty}>
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" opacity="0.3"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" /></svg>
            <p>Aún no hay opiniones</p>
            <span>Sé el primero en opinar</span>
          </div>
        )}
      </div>
    </div>
  )
}
