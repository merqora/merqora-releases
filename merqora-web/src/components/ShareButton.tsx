'use client'

import { useState } from 'react'

interface ShareButtonProps {
  postId: string
  className?: string
  title?: string
  description?: string
}

export default function ShareButton({ postId, className, title, description }: ShareButtonProps) {
  const [copied, setCopied] = useState(false)

  async function handleShare() {
    const url = `${window.location.origin}/p/${postId}`

    if (typeof navigator !== 'undefined' && navigator.share) {
      try {
        await navigator.share({
          title: title || 'Mirá esto en Mercora',
          text: description || '',
          url,
        })
        return
      } catch {
        // User cancelled or error - fallback to copy
      }
    }

    try {
      await navigator.clipboard.writeText(url)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {}
  }

  return (
    <button className={className} onClick={handleShare}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8" />
        <polyline points="16 6 12 2 8 6" />
        <line x1="12" y1="2" x2="12" y2="15" />
      </svg>
      {copied ? 'Copiado âœ“' : 'Compartir'}
    </button>
  )
}
