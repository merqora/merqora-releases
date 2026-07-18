import styles from './Skeleton.module.css'

export function SkeletonCard() {
  return (
    <div className={`card ${styles.card}`}>
      <div className={`${styles.block} ${styles.image}`} />
      <div className={styles.body}>
        <div className={`${styles.block} ${styles.title}`} />
        <div className={`${styles.block} ${styles.price}`} />
        <div className={`${styles.block} ${styles.cond}`} />
        <div className={styles.footer}>
          <div className={`${styles.block} ${styles.avatar}`} />
          <div className={`${styles.block} ${styles.meta}`} />
        </div>
      </div>
    </div>
  )
}

export function SkeletonFeedPost() {
  return (
    <div className={styles.feedPost}>
      <div className={styles.feedHead}>
        <div className={`${styles.block} ${styles.feedAvatar}`} />
        <div className={styles.feedHeadInfo}>
          <div className={`${styles.block} ${styles.feedName}`} />
          <div className={`${styles.block} ${styles.feedTime}`} />
        </div>
      </div>
      <div className={`${styles.block} ${styles.feedImage}`} />
      <div className={styles.feedActions}>
        <div className={`${styles.block} ${styles.feedAction}`} />
        <div className={`${styles.block} ${styles.feedAction}`} />
      </div>
    </div>
  )
}

export function SkeletonGrid({ count = 8 }: { count?: number }) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <SkeletonCard key={i} />
      ))}
    </>
  )
}
