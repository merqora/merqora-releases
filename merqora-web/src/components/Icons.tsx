import type { SVGProps } from 'react'

type IconProps = SVGProps<SVGSVGElement> & { size?: number }

function createIcon(path: string, viewBox = '0 0 24 24') {
  return ({ size = 24, ...props }: IconProps) => (
    <svg width={size} height={size} viewBox={viewBox} fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" {...props}>
      {typeof path === 'string' ? <path d={path} /> : path}
    </svg>
  )
}

export const Icons = {
  Ropa: createIcon('M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4zM3 6h18M16 10a4 4 0 01-8 0'),
  Zapatos: createIcon('M2 16l4-4 4 4M2 20h20M22 16l-4-4-4 4M18 12V4M6 12V4'),
  Accesorios: createIcon('M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z'),
  Electronica: createIcon('M4 7a2 2 0 012-2h12a2 2 0 012 2v10a2 2 0 01-2 2H6a2 2 0 01-2-2V7zM8 3v4M16 3v4M8 13h8M8 17h4'),
  Hogar: createIcon('M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2zM9 22V12h6v10'),
  Deportes: createIcon('M6 9l6-6 6 6-6 6-6-6zM3 21l6-6M21 21l-6-6M9 15l-6 6M15 9l6-6'),
  Vestidos: createIcon('M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4zM12 2v20'),
  Blusas: createIcon('M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4zM6 2h12'),
  Pantalones: createIcon('M6 2L4 22M18 2l2 20M6 2h12M10 2v7a2 2 0 004 0V2'),
  Faldas: createIcon('M12 2C8 2 4 5 4 10v4l8 8 8-8v-4c0-5-4-8-8-8z'),
  Abrigos: createIcon('M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4M6 2h12M6 2v4M18 2v4'),
  Camisas: createIcon('M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4M6 2h12M9 12h6'),
  Bolsos: createIcon('M4 7l1.5 14h13L20 7H4zM8 7V4a2 2 0 014 0v3'),
  Joyeria: createIcon('M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z'),
  Relojes: createIcon('M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10zM12 6v6l4 2'),
  Gafas: createIcon('M2 12c0-2 1.5-4 4-4 2 0 3 1 4 3l2 2 2-2c1-2 2-3 4-3 2.5 0 4 2 4 4s-1.5 4-4 4c-2 0-3-1-4-3l-2-2-2 2c-1 2-2 3-4 3-2.5 0-4-2-4-4z'),
  Smartphones: createIcon('M7 2h10a2 2 0 012 2v16a2 2 0 01-2 2H7a2 2 0 01-2-2V4a2 2 0 012-2zM12 18h.01'),
  Laptops: createIcon('M2 3h20v14H2zM2 21h20M6 17v4M18 17v4M9 7h6'),
  Audio: createIcon('M9 18V5l12-2v13M9 18a3 3 0 01-6 0c0-1.66 1.34-3 6-3z'),
  Gaming: createIcon('M6 12h4M8 10v4M15 13h.01M18 11h.01M17.32 5H6.68a4 4 0 00-3.978 3.59c-.006.052-.01.101-.017.152C2.604 9.416 2 14.456 2 16a3 3 0 003 3c1 0 1.5-.5 2-1l1.414-1.414A2 2 0 019.828 16h4.344a2 2 0 011.414.586L17 18c.5.5 1 1 2 1a3 3 0 003-3c0-1.545-.604-6.584-.685-7.258-.007-.05-.011-.1-.017-.151A4 4 0 0017.32 5z'),
  Muebles: createIcon('M2 4h20v6H2zM2 14h20v6H2zM6 10v4M18 10v4'),
  Decoracion: createIcon('M12 2l9 4.5v11L12 22l-9-4.5v-11L12 2zM12 2v20M2.5 6.5l9 4.5M21.5 6.5l-9 4.5'),
  Iluminacion: createIcon('M9 18h6M12 2v2M12 10v2M10 18v3a1 1 0 001 1h2a1 1 0 001-1v-3M8 12a4 4 0 118 0c0 1.5-.8 2.8-2 3.5V18h-4v-2.5A4.005 4.005 0 018 12z'),
  Sombreros: createIcon('M12 2C7 2 3 5 3 9c0 2.5 1.5 4.5 3 5.5V20a2 2 0 002 2h8a2 2 0 002-2v-5.5c1.5-1 3-3 3-5.5 0-4-4-7-9-7z'),
  ZapatosM: createIcon('M3 17c1.5-1 3.5-2 5-2 4 0 7 2 9 2 2 0 4-1 5-2v-2c-1 1-3 2-5 2-2 0-5-2-9-2-1.5 0-3.5 1-5 2v2z'),
  Chaquetas: createIcon('M4 7l2 13h12l2-13M6 7V4a2 2 0 014 0v3M14 4a2 2 0 014 0v3'),
  Trajes: createIcon('M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4M6 2h12M6 2v3a3 3 0 006 0V2M12 5a3 3 0 006 0V2'),
  Plantas: createIcon('M12 22v-9M12 13c-2.5 0-5-2-5-5 0-3 2-5 5-5s5 2 5 5c0 3-2.5 5-5 5zM7 8c-2 2-3 4-3 6 0 3 2 5 5 5'),
  Seguidores: createIcon('M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2M9 7a4 4 0 100-8 4 4 0 000 8zM23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75'),
  Reputacion: createIcon('M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z'),
  Ubicacion: createIcon('M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0zM12 13a3 3 0 100-6 3 3 0 000 6z'),
  Products: createIcon('M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 002 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16zM3.27 6.96L12 12.01l8.73-5.05M12 22.08V12'),
  Search: createIcon('M21 21l-4.35-4.35M11 19a8 8 0 100-16 8 8 0 000 16z'),
  Cart: createIcon('M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4zM3 6h18M16 10a4 4 0 01-8 0'),
  User: createIcon('M20 21a8 8 0 10-16 0M16 7a4 4 0 11-8 0 4 4 0 018 0z'),
  ChevronRight: createIcon('M9 18l6-6-6-6'),
  Heart: createIcon('M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0016.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 002 8.5c0 2.3 1.5 4.05 3 5.5l7 7z'),
  Share: createIcon('M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8M16 6l-4-4-4 4M12 2v13'),
  Menu: createIcon('M3 6h18M3 12h18M3 18h18'),
  Close: createIcon('M18 6L6 18M6 6l12 12'),
  ArrowLeft: createIcon('M19 12H5M12 19l-7-7 7-7'),
  ArrowRight: createIcon('M5 12h14M12 5l7 7-7 7'),
  Star: createIcon('M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z'),
  Lightning: createIcon('M13 2L3 14h9l-1 8 10-12h-9l1-8z'),
  Truck: createIcon('M1 17h2M3 17a3 3 0 106 0M9 17a3 3 0 106 0M15 17h2M1 11V5a2 2 0 012-2h11a2 2 0 012 2v8M21 9h-5M17 5l4 4-4 4'),
  Verified: createIcon('M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z', '0 0 24 24'),
  Copy: createIcon('M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z'),
} as const
