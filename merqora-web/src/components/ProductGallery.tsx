'use client'

import { useState } from 'react'
import ImageZoom from './ImageZoom'
import ImageLightbox from './ImageLightbox'
import styles from '@/app/p/[id]/page.module.css'

interface ProductGalleryProps {
  images: string[]
  title: string
}

export default function ProductGallery({ images, title }: ProductGalleryProps) {
  const [lightboxOpen, setLightboxOpen] = useState(false)
  const [currentIndex, setCurrentIndex] = useState(0)
  const [selectedThumb, setSelectedThumb] = useState(0)

  function openLightbox(index: number) {
    setCurrentIndex(index)
    setLightboxOpen(true)
  }

  function handlePrev() {
    setCurrentIndex((i) => (i > 0 ? i - 1 : images.length - 1))
  }

  function handleNext() {
    setCurrentIndex((i) => (i < images.length - 1 ? i + 1 : 0))
  }

  return (
    <>
      <div className={styles.gallery}>
        <div
          className={styles.mainImageWrap}
          onClick={() => openLightbox(selectedThumb)}
          style={{ cursor: 'zoom-in' }}
        >
          {images.length > 0 ? (
            <ImageZoom src={images[selectedThumb]} alt={title} className={styles.mainImage} />
          ) : (
            <div className={styles.placeholder} />
          )}
        </div>
        {images.length > 1 && (
          <div className={styles.thumbs}>
            {images.map((img, i) => (
              <div
                key={i}
                className={`${styles.thumbWrap} ${i === selectedThumb ? styles.thumbActive : ''}`}
                onClick={() => setSelectedThumb(i)}
              >
                <img src={img} alt="" className={styles.thumb} loading="lazy" />
              </div>
            ))}
          </div>
        )}
      </div>

      {lightboxOpen && (
        <ImageLightbox
          images={images}
          currentIndex={currentIndex}
          onClose={() => setLightboxOpen(false)}
          onPrev={handlePrev}
          onNext={handleNext}
        />
      )}
    </>
  )
}
