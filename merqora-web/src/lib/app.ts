import { createServerSupabaseClient } from './supabase/server'

export interface AppVersion {
  id: string
  version_name: string
  version_code: number
  changelog: string | null
  file_url: string | null
  file_path: string | null
  file_size_mb: number | null
  min_android: string | null
  is_latest: boolean
  download_count: number
  created_at: string
}

export async function getLatestVersion(): Promise<AppVersion | null> {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('app_versions')
    .select('*')
    .eq('is_latest', true)
    .single()

  if (error || !data) return null
  return data as AppVersion
}

export async function getAllVersions(): Promise<AppVersion[]> {
  const supabase = await createServerSupabaseClient()

  const { data, error } = await supabase
    .from('app_versions')
    .select('*')
    .order('created_at', { ascending: false })

  if (error || !data) return []
  return data as AppVersion[]
}
