'use client'

import { useState, useCallback, useEffect, useRef } from 'react'
import styles from './ShimmerImage.module.css'

interface ShimmerImageProps {
  src: string
  alt: string
  className?: string
  aspectRatio?: string
}

export default function ShimmerImage({ src, alt, className, aspectRatio = '1' }: ShimmerImageProps) {
  const [loaded, setLoaded] = useState(false)
  const [error, setError] = useState(false)
  const imgRef = useRef<HTMLImageElement>(null)

  const onLoad = useCallback(() => setLoaded(true), [])
  const onError = useCallback(() => {
    setError(true)
    setLoaded(true)
  }, [])

  useEffect(() => {
    const img = imgRef.current
    if (img && img.complete) {
      setLoaded(true)
      if (!img.naturalWidth) setError(true)
    }
  }, [])

  return (
    <div className={`${styles.wrap} ${className || ''}`} style={{ aspectRatio }}>
      {!loaded && !error && <div className={styles.shimmer} />}
      {error && (
        <div className={styles.error}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" opacity="0.3">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <circle cx="8.5" cy="8.5" r="1.5" />
            <polyline points="21 15 16 10 5 21" />
          </svg>
        </div>
      )}
      <img
        ref={imgRef}
        src={src}
        alt={alt}
        className={`${styles.img} ${loaded ? styles.loaded : ''}`}
        onLoad={onLoad}
        onError={onError}
        loading="lazy"
      />
    </div>
  )
}
