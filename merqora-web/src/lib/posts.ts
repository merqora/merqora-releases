import { createServerSupabaseClient } from './supabase/server'
import type { Post, PostDB, UsuarioDB } from './types'

function postToUI(p: PostDB, user: UsuarioDB | null): Post {
  const daysOld = p.created_at
    ? Math.floor(
        (Date.now() - new Date(p.created_at).getTime()) / (1000 * 60 * 60 * 24)
      )
    : 999

  return {
    id: p.id,
    userId: p.user_id,
    title: p.title,
    description: p.description,
    price: p.price,
    previousPrice: p.previous_price,
    category: p.category || '',
    condition: p.condition || '',
    images: p.images,
    likesCount: p.likes_count,
    reviewsCount: p.reviews_count,
    viewsCount: p.views_count,
    sharesCount: p.shares_count,
    savesCount: p.saves_count,
    createdAt: p.created_at,
    username: user?.username || 'Usuario',
    userAvatar: user?.avatar_url || '',
    userStoreName: user?.nombre_tienda || user?.nombre || null,
    isUserVerified: user?.is_verified || false,
    stock: p.stock,
    freeShipping: p.free_shipping || false,
    allowOffers: p.allow_offers || false,
    isNew: daysOld <= 7,
  }
}

async function attachUsers(posts: PostDB[]): Promise<Post[]> {
  if (posts.length === 0) return []
  const supabase = await createServerSupabaseClient()
  const userIds = [...new Set(posts.map((p) => p.user_id))]

  const { data: users } = await supabase
    .from('usuarios')
    .select('*')
    .in('user_id', userIds)

  const userMap = new Map<string, UsuarioDB>()
  if (users) {
    for (const u of users as UsuarioDB[]) {
      userMap.set(u.user_id, u)
    }
  }

  return posts.map((p) => postToUI(p, userMap.get(p.user_id) || null))
}

export async function getRecentPosts(limit = 20, offset = 0) {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('posts')
    .select('*')
    .eq('status', 'active')
    .order('created_at', { ascending: false })
    .range(offset, offset + limit - 1)

  if (error) {
    console.error('getRecentPosts error:', error)
    return []
  }
  if (!data || data.length === 0) return []

  return attachUsers(data as PostDB[])
}

export async function getPostById(id: string) {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('posts')
    .select('*')
    .eq('id', id)
    .single()

  if (error || !data) return null

  const posts = await attachUsers([data as PostDB])
  return posts[0] || null
}

export async function getPostsByCategory(
  category: string,
  limit = 20,
  offset = 0
) {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('posts')
    .select('*')
    .eq('status', 'active')
    .eq('category', category)
    .order('created_at', { ascending: false })
    .range(offset, offset + limit - 1)

  if (error || !data) return []
  return attachUsers(data as PostDB[])
}

export async function getPostsByUserId(userId: string, limit = 20) {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('posts')
    .select('*')
    .eq('status', 'active')
    .eq('user_id', userId)
    .order('created_at', { ascending: false })
    .limit(limit)

  if (error || !data) return []
  return attachUsers(data as PostDB[])
}

export async function getUserById(userId: string) {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('usuarios')
    .select('*')
    .eq('user_id', userId)
    .single()

  if (error || !data) return null
  return data as UsuarioDB
}

export async function getPopularPosts(limit = 10) {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('posts')
    .select('*')
    .eq('status', 'active')
    .order('likes_count', { ascending: false })
    .limit(limit)

  if (error || !data) return []
  return attachUsers(data as PostDB[])
}

export async function getDeals(limit = 10) {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('posts')
    .select('*')
    .eq('status', 'active')
    .not('previous_price', 'is', null)
    .order('created_at', { ascending: false })
    .limit(limit)

  if (error || !data) return []
  return attachUsers(data as PostDB[])
}

export async function searchPosts(
  query: string,
  category: string,
  limit = 50,
  offset = 0
) {
  const supabase = await createServerSupabaseClient()

  let builder = supabase
    .from('posts')
    .select('*')
    .eq('status', 'active')

  if (query) {
    builder = builder.or(`title.ilike.%${query}%,description.ilike.%${query}%`)
  }

  if (category && category !== 'todos') {
    builder = builder.eq('category', category)
  }

  const { data, error } = await builder
    .order('created_at', { ascending: false })
    .range(offset, offset + limit - 1)

  if (error || !data) return []
  return attachUsers(data as PostDB[])
}

export async function getFeaturedPosts(limit = 6) {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('posts')
    .select('*')
    .eq('status', 'active')
    .not('images', 'eq', '{}')
    .order('views_count', { ascending: false })
    .limit(limit)

  if (error || !data) return []
  return attachUsers(data as PostDB[])
}

export async function getAllCategoriesWithCounts() {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('posts')
    .select('category')
    .eq('status', 'active')

  if (error || !data) return {}

  const counts: Record<string, number> = {}
  for (const row of data as { category: string | null }[]) {
    const cat = row.category || 'otros'
    counts[cat] = (counts[cat] || 0) + 1
  }
  return counts
}
