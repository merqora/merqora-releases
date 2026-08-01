'use client'

import { useState, useEffect, useCallback } from 'react'
import Link from 'next/link'
import type { Post } from '@/lib/types'
import styles from './HeroSlider.module.css'

const SLIDES = [
  {
    image: '/images/slider/imagen1.jpg',
    gradient: 'linear-gradient(135deg, rgba(18,6,42,0.92) 0%, rgba(74,26,138,0.7) 50%, rgba(245,158,11,0.12) 100%)',
    title: 'Descubrí productos únicos',
    subtitle: 'El marketplace donde cada artículo tiene su propia historia',
    cta: 'Ver productos',
  },
  {
    image: '/images/slider/imagen2.png',
    gradient: 'linear-gradient(135deg, rgba(18,6,42,0.92) 0%, rgba(107,33,192,0.65) 50%, rgba(168,85,247,0.12) 100%)',
    title: 'Encontrá lo que buscás',
    subtitle: 'Miles de productos increíbles te esperan',
    cta: 'Ver productos',
  },
  {
    image: '/images/slider/imagen3.png',
    gradient: 'linear-gradient(135deg, rgba(18,6,42,0.92) 0%, rgba(139,61,246,0.6) 50%, rgba(245,158,11,0.1) 100%)',
    title: 'Moda, tecnología y más',
    subtitle: 'Las mejores marcas y los precios más justos',
    cta: 'Ver productos',
  },
]

export default function HeroSlider({
  featuredPosts = [],
}: {
  featuredPosts?: Post[]
}) {
  const [current, setCurrent] = useState(0)

  const next = useCallback(() => {
    setCurrent((c) => (c + 1) % SLIDES.length)
  }, [])

  useEffect(() => {
    const timer = setInterval(next, 5000)
    return () => clearInterval(timer)
  }, [next])

  return (
    <section className={styles.hero}>
      <div className={styles.viewport}>
        <div
          className={styles.track}
          style={{ transform: `translateX(-${current * 100}%)` }}
        >
          {SLIDES.map((slide, i) => (
            <div key={i} className={styles.slide} style={{ background: slide.gradient }}>
              <div
                className={styles.bgImage}
                style={{ backgroundImage: `url(${slide.image})` }}
              />
              <div className={styles.overlay} />
              <div className="container">
                <div className={styles.content}>
                  <div className={styles.textSection}>
                    <h1 className={styles.title}>{slide.title}</h1>
                    <p className={styles.subtitle}>{slide.subtitle}</p>
                <a href="#productos" className={styles.cta}>
                  {slide.cta}
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M5 12h14M12 5l7 7-7 7" />
                  </svg>
                </a>
                  </div>

                  {i === 0 && featuredPosts.length > 0 && (
                    <div className={styles.featuredGrid}>
                      {featuredPosts.slice(0, 3).map((post) => (
                        <Link
                          key={post.id}
                          href={`/p/${post.id}`}
                          className={styles.featuredCard}
                        >
                          <div className={styles.featuredImageWrap}>
                            <img
                              src={post.images[0] || ''}
                              alt={post.title}
                              className={styles.featuredImage}
                            />
                          </div>
                          <div className={styles.featuredInfo}>
                            <span className={styles.featuredPrice}>
                              ${post.price.toLocaleString('es-UY')}
                            </span>
                            <span className={styles.featuredTitle}>
                              {post.title}
                            </span>
                          </div>
                        </Link>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className={styles.dots}>
        {SLIDES.map((_, i) => (
          <button
            key={i}
            className={`${styles.dot} ${i === current ? styles.dotActive : ''}`}
            onClick={() => setCurrent(i)}
            aria-label={`Slide ${i + 1}`}
          />
        ))}
      </div>
    </section>
  )
}
