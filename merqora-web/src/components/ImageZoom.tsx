'use client'

import { useRef, useState } from 'react'
import styles from './ImageZoom.module.css'

interface ImageZoomProps {
  src: string
  alt: string
  className?: string
}

export default function ImageZoom({ src, alt, className }: ImageZoomProps) {
  const [zoomed, setZoomed] = useState(false)
  const [pos, setPos] = useState({ x: 50, y: 50 })
  const ref = useRef<HTMLDivElement>(null)

  function handleMouse(e: React.MouseEvent) {
    if (!ref.current) return
    const rect = ref.current.getBoundingClientRect()
    const x = ((e.clientX - rect.left) / rect.width) * 100
    const y = ((e.clientY - rect.top) / rect.height) * 100
    setPos({ x, y })
  }

  return (
    <div
      ref={ref}
      className={`${styles.wrap} ${className || ''}`}
      onMouseEnter={() => setZoomed(true)}
      onMouseLeave={() => setZoomed(false)}
      onMouseMove={handleMouse}
    >
      <img
        src={src}
        alt={alt}
        className={styles.img}
        style={{
          transform: zoomed ? 'scale(2)' : 'scale(1)',
          transformOrigin: `${pos.x}% ${pos.y}%`,
        }}
      />
    </div>
  )
}
