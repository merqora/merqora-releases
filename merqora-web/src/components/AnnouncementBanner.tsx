'use client'

import styles from './AnnouncementBanner.module.css'

const messages = [
  '📦  Encontrá productos únicos en Vinzay',
  '🔥  Los mejores precios están acá',
  '🚚  Conectá con vendedores de toda Latinoamérica',
  '⭐  Compra segura desde la app',
  '💡  Nuevos productos todos los días',
  '✨  Descubrí lo que buscás en Vinzay',
]

export default function AnnouncementBanner() {
  return (
    <div className={styles.banner}>
      <div className={styles.shine} />
      <div className={styles.track}>
        {messages.map((msg, i) => (
          <span key={i} className={styles.message}>{msg}</span>
        ))}
        {messages.map((msg, i) => (
          <span key={`dup-${i}`} className={styles.message}>{msg}</span>
        ))}
      </div>
    </div>
  )
}
