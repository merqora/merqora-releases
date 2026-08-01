import styles from './TrustStrip.module.css'

const FEATURES = [
  {
    icon: 'M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zM9 12l2 2 4-4',
    title: 'Compra protegida',
    text: 'Publicaciones verificadas y reputación visible de cada vendedor.',
  },
  {
    icon: 'M21 12a9 9 0 1 1-9-9M12 6v6l3 2M3 3l18 18',
    title: 'Pagos seguros',
    text: 'Transacciones cifradas de extremo a extremo con Stripe.',
  },
  {
    icon: 'M2 6h20v12H2zM2 6l10 7L22 6M22 10v4M2 10v4',
    title: 'Envíos en Uruguay',
    text: 'Conectá con vendedores y coordiná tu entrega directa.',
  },
  {
    icon: 'M4 4h16a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H9l-5 4V6a2 2 0 0 1 2-2zM8 9h8M8 13h5',
    title: 'Soporte real',
    text: 'Chat y asistencia IA para resolver tus dudas al instante.',
  },
  {
    icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
    title: 'Comunidad',
    text: 'Comprá y vendé entre personas de toda Latinoamérica.',
  },
]

export default function TrustStrip() {
  return (
    <section className={styles.section}>
      <div className={`container ${styles.grid}`}>
        {FEATURES.map((f) => (
          <div key={f.title} className={styles.item}>
            <div className={styles.iconWrap}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d={f.icon} />
              </svg>
            </div>
            <div>
              <h3 className={styles.title}>{f.title}</h3>
              <p className={styles.text}>{f.text}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
