import type { Metadata } from 'next'
import { Geist, Geist_Mono } from 'next/font/google'
import AnnouncementBanner from '@/components/AnnouncementBanner'
import './globals.css'

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
})

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
})

const siteUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://vinzay.app'

export const metadata: Metadata = {
  title: {
    default: 'Vinzay — Descubrí productos únicos',
    template: '%s | Vinzay',
  },
  description:
    'Vinzay es el marketplace donde encontrar productos únicos. Explorá, conectá con vendedores y descubrí lo que buscas.',
  metadataBase: new URL(siteUrl),
  openGraph: {
    type: 'website',
    siteName: 'Vinzay',
    title: 'Vinzay — Descubrí productos únicos',
    description:
      'Explorá productos únicos en Vinzay. Todo lo que buscás, en un solo lugar.',
    url: siteUrl,
    locale: 'es_UY',
    images: [{ url: '/og-default.svg', width: 1200, height: 630 }],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Vinzay — Descubrí productos únicos',
    description:
      'Explorá productos únicos en Vinzay.',
    images: ['/og-default.svg'],
  },
  robots: {
    index: true,
    follow: true,
  },
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="es" className={`${geistSans.variable} ${geistMono.variable}`}>
      <body>
        <AnnouncementBanner />
        {children}
      </body>
    </html>
  )
}
