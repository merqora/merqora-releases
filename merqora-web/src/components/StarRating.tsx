import styles from './StarRating.module.css'

export default function StarRating({ rating = 0, size = 14 }: { rating?: number; size?: number }) {
  const stars = Math.round(rating)
  return (
    <div className={styles.stars} style={{ gap: size > 14 ? '3px' : '2px' }}>
      {[1, 2, 3, 4, 5].map((s) => (
        <svg
          key={s}
          width={size}
          height={size}
          viewBox="0 0 24 24"
          fill={s <= stars ? 'var(--amber-400)' : 'none'}
          stroke={s <= stars ? 'var(--amber-400)' : 'var(--border-default)'}
          strokeWidth="1.5"
        >
          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
        </svg>
      ))}
    </div>
  )
}
