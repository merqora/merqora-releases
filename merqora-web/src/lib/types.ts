export interface PostDB {
  id: string
  user_id: string
  title: string
  description: string | null
  price: number
  category: string | null
  condition: string | null
  tags: string[] | null
  images: string[]
  status: string
  likes_count: number
  reviews_count: number
  views_count: number
  shares_count: number
  saves_count: number
  created_at: string
  updated_at: string
  is_collection: boolean
  cover_type: string | null
  cover_image_index: number | null
  cover_url: string | null
  stock: number | null
  previous_price: number | null
  allow_offers: boolean | null
  free_shipping: boolean | null
}

export interface UsuarioDB {
  id: string
  user_id: string
  username: string
  nombre: string | null
  nombre_tienda: string | null
  descripcion: string | null
  avatar_url: string | null
  banner_url: string | null
  ubicacion: string | null
  reputacion: number
  seguidores: number
  is_online: boolean
  is_verified: boolean
  tiene_tienda: boolean
  created_at: string
  ultima_actividad: string | null
}

export interface PostWithUser extends PostDB {
  usuarios: UsuarioDB | UsuarioDB[] | null
}

export interface Post {
  id: string
  userId: string
  title: string
  description: string | null
  price: number
  previousPrice: number | null
  category: string
  condition: string
  images: string[]
  likesCount: number
  reviewsCount: number
  viewsCount: number
  sharesCount: number
  savesCount: number
  ratingAvg?: number
  createdAt: string
  username: string
  userAvatar: string
  userStoreName: string | null
  isUserVerified: boolean
  stock: number | null
  freeShipping: boolean
  allowOffers: boolean
  isNew: boolean
}

export interface CategoryGroup {
  id: string
  name: string
  subcategories: { id: string; name: string }[]
}

export const CATEGORY_GROUPS: CategoryGroup[] = [
  {
    id: 'ropa',
    name: 'Ropa y Accesorios',
    subcategories: [
      { id: 'vestidos', name: 'Vestidos' },
      { id: 'blusas', name: 'Blusas y Tops' },
      { id: 'pantalones', name: 'Pantalones' },
      { id: 'faldas', name: 'Faldas' },
      { id: 'abrigos', name: 'Abrigos y Chaquetas' },
      { id: 'camisas', name: 'Camisas' },
      { id: 'trajes', name: 'Trajes' },
    ],
  },
  {
    id: 'zapatos',
    name: 'Zapatos',
    subcategories: [
      { id: 'zapatos_m', name: 'Zapatos de Mujer' },
      { id: 'zapatos_h', name: 'Zapatos de Hombre' },
    ],
  },
  {
    id: 'accesorios',
    name: 'Accesorios',
    subcategories: [
      { id: 'bolsos', name: 'Bolsos y Carteras' },
      { id: 'joyeria', name: 'Joyería' },
      { id: 'relojes', name: 'Relojes' },
      { id: 'gafas', name: 'Gafas de Sol' },
      { id: 'sombreros', name: 'Sombreros' },
    ],
  },
  {
    id: 'electronica',
    name: 'Electrónica',
    subcategories: [
      { id: 'smartphones', name: 'Smartphones' },
      { id: 'laptops', name: 'Laptops' },
      { id: 'audio', name: 'Audio' },
      { id: 'gaming', name: 'Gaming' },
    ],
  },
  {
    id: 'hogar',
    name: 'Hogar y Decoración',
    subcategories: [
      { id: 'muebles', name: 'Muebles' },
      { id: 'decoracion', name: 'Decoración' },
      { id: 'plantas', name: 'Plantas' },
      { id: 'iluminacion', name: 'Iluminación' },
    ],
  },
  {
    id: 'deportes',
    name: 'Deportes',
    subcategories: [
      { id: 'fitness', name: 'Fitness' },
      { id: 'running', name: 'Running' },
      { id: 'cycling', name: 'Ciclismo' },
      { id: 'natacion', name: 'Natación' },
    ],
  },
]

export const CATEGORIES = CATEGORY_GROUPS.map((g) => ({
  id: g.id,
  name: g.name,
  icon: g.id,
}))

export interface HighlightDB {
  id: string
  user_id: string
  title: string
  cover_url: string | null
  category: string
  frame_style: string
  frame_color: string
  background_color: string
  icon: string
  stories_count: number
  is_new: boolean
  created_at: string
}

export interface HighlightStoryDB {
  id: string
  highlight_id: string
  story_id: string | null
  media_url: string
  media_type: string
  position: number
  created_at: string
}

export const CATEGORY_NAMES: Record<string, string> = {}
for (const group of CATEGORY_GROUPS) {
  CATEGORY_NAMES[group.id] = group.name
  for (const sub of group.subcategories) {
    CATEGORY_NAMES[sub.id] = sub.name
  }
}
