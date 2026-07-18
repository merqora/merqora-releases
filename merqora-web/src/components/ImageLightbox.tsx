'use client'

import { useEffect, useCallback } from 'react'
import styles from './ImageLightbox.module.css'

interface ImageLightboxProps {
  images: string[]
  currentIndex: number
  onClose: () => void
  onPrev: () => void
  onNext: () => void
}

export default function ImageLightbox({ images, currentIndex, onClose, onPrev, onNext }: ImageLightboxProps) {
  const handleKeyDown = useCallback((e: KeyboardEvent) => {
    if (e.key === 'Escape') onClose()
    if (e.key === 'ArrowLeft') onPrev()
    if (e.key === 'ArrowRight') onNext()
  }, [onClose, onPrev, onNext])

  useEffect(() => {
    document.addEventListener('keydown', handleKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.style.overflow = ''
    }
  }, [handleKeyDown])

  if (!images.length) return null

  return (
    <div className={styles.overlay} onClick={onClose}>
      <button className={styles.closeBtn} onClick={onClose}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M18 6 6 18M6 6l12 12" />
        </svg>
      </button>

      <div className={styles.counter}>
        {currentIndex + 1} / {images.length}
      </div>

      <button
        className={`${styles.nav} ${styles.prev}`}
        onClick={(e) => { e.stopPropagation(); onPrev() }}
        disabled={currentIndex === 0}
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M15 18l-6-6 6-6" />
        </svg>
      </button>

      <div className={styles.imageWrap} onClick={(e) => e.stopPropagation()}>
        <img src={images[currentIndex]} alt="" className={styles.image} />
      </div>

      <button
        className={`${styles.nav} ${styles.next}`}
        onClick={(e) => { e.stopPropagation(); onNext() }}
        disabled={currentIndex === images.length - 1}
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M9 18l6-6-6-6" />
        </svg>
      </button>
    </div>
  )
}
