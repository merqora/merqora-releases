# Rendly Cache-First Architecture

## Diagrama de Flujo de Datos

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              UI LAYER                                        │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                      │
│  │  HomeScreen │    │ ProfileScreen│    │  FeedScreen │                      │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘                      │
│         │                  │                   │                             │
│         └──────────────────┼───────────────────┘                             │
│                            ▼                                                 │
│                    ┌───────────────┐                                         │
│                    │   ViewModel   │  ← Collect StateFlow                    │
│                    └───────┬───────┘                                         │
└────────────────────────────┼─────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         REPOSITORY LAYER                                     │
│                                                                              │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                    CacheFirstRepository<T>                             │  │
│  │                                                                        │  │
│  │   1. emit(MemoryCache) ──────► UI renderiza inmediatamente             │  │
│  │          │                                                             │  │
│  │          ▼                                                             │  │
│  │   2. emit(DiskCache/Room) ──► UI actualiza si hay cambios              │  │
│  │          │                                                             │  │
│  │          ▼                                                             │  │
│  │   3. fetch(Network) ─────────► Background (no bloquea UI)              │  │
│  │          │                                                             │  │
│  │          ▼                                                             │  │
│  │   4. compare(version/hash)                                             │  │
│  │          │                                                             │  │
│  │     ┌────┴────┐                                                        │  │
│  │     │ Changed?│                                                        │  │
│  │     └────┬────┘                                                        │  │
│  │    YES   │   NO                                                        │  │
│  │     ▼    │    ▼                                                        │  │
│  │  Update  │  Done                                                       │  │
│  │  Caches  │                                                             │  │
│  │     │    │                                                             │  │
│  │     ▼    │                                                             │  │
│  │   emit() │  ← Animación suave en UI                                    │  │
│  └──────────┴────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          ▼                  ▼                  ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   MEMORY CACHE  │ │   DISK CACHE    │ │    NETWORK      │
│                 │ │                 │ │                 │
│  ┌───────────┐  │ │  ┌───────────┐  │ │  ┌───────────┐  │
│  │  LruCache │  │ │  │   Room    │  │ │  │ Supabase  │  │
│  │  + TTL    │  │ │  │  SQLite   │  │ │  │ PostgREST │  │
│  └───────────┘  │ │  └───────────┘  │ │  └───────────┘  │
│                 │ │                 │ │                 │
│  • 5-30 seg TTL │ │  • Persistent   │ │  • Fetch only   │
│  • Per context  │ │  • Normalized   │ │    when needed  │
│  • Hot data     │ │  • Indexed      │ │  • Deduped      │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

## Orden de Prioridad (Cache Layers)

| Capa | Latencia | Persistencia | Uso |
|------|----------|--------------|-----|
| 1. Memory (LruCache) | ~0ms | Session | Hot data, scroll rápido |
| 2. Disk (Room) | ~5-20ms | Permanent | Fuente de verdad local |
| 3. Network (Supabase) | ~100-2000ms | Remote | Sync & revalidación |

## Políticas de Cache (TTL)

```kotlin
enum class CachePolicy(val memoryTtlMs: Long, val diskTtlMs: Long) {
    FEED(30_000, 300_000),           // 30s memory, 5min disk
    PROFILE(60_000, 600_000),        // 1min memory, 10min disk  
    MESSAGES(5_000, 60_000),         // 5s memory, 1min disk
    RENDS(30_000, 300_000),          // 30s memory, 5min disk
    STORIES(15_000, 120_000),        // 15s memory, 2min disk
    USER_DATA(120_000, 3600_000),    // 2min memory, 1h disk
    STATIC(3600_000, 86400_000)      // 1h memory, 24h disk
}
```

## Stale-While-Revalidate Flow

```
Request ──► Memory Hit? ──YES──► Emit ──► Revalidate in BG
                │                              │
                NO                             ▼
                │                    Network Fetch
                ▼                              │
           Room Hit? ──YES──► Emit ──► Compare Version
                │                              │
                NO                        Changed?
                │                         │     │
                ▼                        YES    NO
         Network Fetch                    │     │
                │                         ▼     ▼
                ▼                    Update    Done
         Store in Room ──► Store in Memory ──► Emit
```

## Componentes Principales

### 1. MemoryCache<K, V>
- LruCache con soporte TTL
- Genérico y thread-safe
- Invalidación selectiva por key/pattern

### 2. Room Database (RendlyDatabase)
- Entities normalizadas con indices
- Campo `cached_at` para TTL
- Campo `version` para comparación

### 3. CacheFirstRepository<T>
- Implementa stale-while-revalidate
- Flow-based emissions
- Error handling silencioso

### 4. NetworkDataSource
- Supabase client wrapper
- Request deduplication
- Retry logic

### 5. CacheOrchestrator
- Coordina cache warming
- Background sync (WorkManager)
- Prefetch basado en navegación

## Decisiones Arquitectónicas

1. **Room como Single Source of Truth**: Toda UI observa Room via Flow. La red solo actualiza Room.

2. **Memory Cache es optimization layer**: Evita lecturas de disco frecuentes pero no es fuente de verdad.

3. **Never block UI**: La red siempre corre en background. UI siempre muestra algo (cache o skeleton).

4. **Version-based comparison**: Usar `updated_at` de Supabase para detectar cambios.

5. **Graceful degradation**: Si la red falla, cache es suficiente. Logging pero sin crashear.
