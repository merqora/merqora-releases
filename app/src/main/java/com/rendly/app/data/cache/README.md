# Sistema de Caching Cache-First para Rendly

## Resumen Ejecutivo

Sistema de caching profesional en capas inspirado en la arquitectura real de apps como Instagram y TikTok. Optimizado para Android (Kotlin) con backend Supabase.

**Principio fundamental:** La UI SIEMPRE renderiza desde cache inmediatamente. La red NUNCA bloquea la UI.

---

## Arquitectura de Capas

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI LAYER                                 │
│   Compose Screens → ViewModel → StateFlow                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│                    REPOSITORY LAYER                              │
│   CacheFirstRepository → Stale-While-Revalidate Pattern         │
└───────────────────────────┬─────────────────────────────────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
   │   MEMORY    │   │    DISK     │   │   NETWORK   │
   │   CACHE     │   │   CACHE     │   │             │
   │             │   │             │   │             │
   │  LruCache   │   │   Room      │   │  Supabase   │
   │  + TTL      │   │   SQLite    │   │  PostgREST  │
   │             │   │             │   │             │
   │  ~0ms       │   │  ~5-20ms    │   │ ~100-2000ms │
   └─────────────┘   └─────────────┘   └─────────────┘
```

---

## Componentes Principales

### 1. MemoryCache (`core/MemoryCache.kt`)
- **LruCache** con soporte TTL configurable
- Thread-safe con Mutex
- Invalidación por pattern matching
- Estadísticas de hit/miss para debugging

### 2. Room Database (`db/RendlyDatabase.kt`)
- Entities normalizadas con indices optimizados
- Campo `cached_at` para TTL
- Campo `version` para comparación de cambios
- Soporte para operaciones pendientes (offline-first)

### 3. CachePolicy (`core/CachePolicy.kt`)
- TTL configurable por tipo de dato
- Políticas predefinidas: FEED, PROFILE, MESSAGES, etc.
- Soporte stale-while-revalidate

### 4. CacheFirstRepository (`repository/CacheFirstRepository.kt`)
- Base abstracta para todos los repositorios
- Implementa patrón stale-while-revalidate
- Flow-based emissions para UI reactiva
- Error handling silencioso con fallback a cache

### 5. SupabaseDataSource (`network/SupabaseDataSource.kt`)
- Wrapper sobre Supabase client
- Request deduplication automática
- Logging estructurado

### 6. CacheOrchestrator (`CacheOrchestrator.kt`)
- Coordinación central de caches
- Cache warming al inicio
- Prefetch basado en navegación
- Background sync con WorkManager

---

## Flujo Stale-While-Revalidate

```kotlin
// El usuario ve datos INMEDIATAMENTE
repository.getFeed().collect { result ->
    when (result) {
        is Success -> {
            // Primera emisión: datos de cache (stale=true)
            // Segunda emisión: datos de red (stale=false)
            updateUI(result.data, animated = result.source == NETWORK)
        }
    }
}
```

**Secuencia:**
1. **T+0ms**: Emitir desde Memory Cache
2. **T+5ms**: Si miss, emitir desde Room
3. **T+background**: Fetch de red (no bloquea)
4. **T+network**: Comparar versiones
5. **T+network**: Si cambió, actualizar caches y emitir

---

## Políticas de TTL

| Tipo | Memory TTL | Disk TTL | Uso |
|------|------------|----------|-----|
| FEED | 30s | 5min | Posts, timeline |
| PROFILE | 1min | 10min | Datos de perfil |
| MESSAGES | 5s | 1min | Chat en tiempo real |
| RENDS | 30s | 5min | Videos cortos |
| STORIES | 15s | 2min | Stories efímeras |
| USER_DATA | 2min | 1h | Info de usuarios |
| STATIC | 1h | 24h | Categorías, config |

---

## Uso en ViewModel

```kotlin
class MyViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = CachedRendRepository.getInstance(app)
    
    private val _uiState = MutableStateFlow<UiState>(Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            repository.getFeed().collect { result ->
                _uiState.value = when (result) {
                    is DataResult.Success -> UiState.Success(
                        data = result.data,
                        isStale = result.isStale,
                        source = result.source
                    )
                    is DataResult.Empty -> UiState.Empty
                    is DataResult.Error -> UiState.Error(result.exception)
                    is DataResult.Loading -> UiState.Loading
                }
            }
        }
    }
}
```

---

## Optimizaciones Avanzadas

### Prefetch Inteligente
```kotlin
// Se activa automáticamente al acercarse al final del scroll
cacheOrchestrator.onScrollApproachingEnd(Screen.RendsFeed, currentPage)
```

### Request Deduplication
```kotlin
// Múltiples llamadas simultáneas comparten la misma request
val data = deduplicator.dedupe("feed:page:0") {
    supabase.from("posts").select().decodeList()
}
```

### Cache Warming
```kotlin
// Al iniciar la app, carga datos críticos en memoria
CacheOrchestrator.getInstance(context).initialize()
```

### Background Sync
```kotlin
// WorkManager sincroniza cada 15 minutos
CacheSyncWorker.schedulePeriodicSync(context)
```

---

## Decisiones Arquitectónicas

### 1. Room como Single Source of Truth
**Decisión:** Room es la fuente de verdad, no la red.

**Razón:** Garantiza consistencia y permite funcionamiento offline. La UI siempre observa Room, y la red solo actualiza Room.

### 2. Memory Cache como Optimization Layer
**Decisión:** Memory cache no es fuente de verdad, solo optimización.

**Razón:** Evita lecturas de disco frecuentes durante scroll rápido. Si se pierde, Room tiene los datos.

### 3. Never Block UI
**Decisión:** Todas las operaciones de red son background.

**Razón:** UX similar a Instagram: el usuario siempre ve contenido, aunque sea stale.

### 4. Version-based Comparison
**Decisión:** Usar `updated_at` de Supabase para detectar cambios.

**Razón:** Evita actualizar UI innecesariamente cuando los datos no cambiaron.

### 5. Graceful Degradation
**Decisión:** Errores de red son silenciosos si hay cache.

**Razón:** El usuario tiene datos para interactuar. Los errores se loguean pero no se muestran.

### 6. Repository Desacoplado de UI
**Decisión:** Toda lógica de cache está en Repository, no en ViewModel.

**Razón:** ViewModels limpios, lógica reutilizable, testing más fácil.

---

## Estructura de Archivos

```
data/cache/
├── CACHE_ARCHITECTURE.md     # Diagrama de flujo
├── README.md                  # Este archivo
├── CacheOrchestrator.kt      # Coordinador central
├── core/
│   ├── MemoryCache.kt        # Cache en RAM con TTL
│   └── CachePolicy.kt        # Políticas y tipos
├── db/
│   ├── CacheEntities.kt      # Room entities
│   ├── CacheDao.kt           # DAOs optimizados
│   └── RendlyDatabase.kt     # Database singleton
├── network/
│   ├── RequestDeduplicator.kt # Deduplicación de requests
│   └── SupabaseDataSource.kt  # Network layer
├── repository/
│   ├── CacheFirstRepository.kt # Base abstracta
│   └── CachedRendRepository.kt # Implementación para Rends
└── sync/
    └── CacheSyncWorker.kt     # Background sync
```

---

## Migración de Repositorios Existentes

Para migrar un repositorio existente al sistema cache-first:

1. **Crear Room Entity** en `CacheEntities.kt`
2. **Agregar DAO** en `CacheDao.kt`
3. **Extender `CacheFirstRepository`**
4. **Implementar métodos abstractos**
5. **Agregar a Hilt module** si corresponde

Ejemplo mínimo:
```kotlin
class CachedPostRepository(context: Context) 
    : CacheFirstRepository<List<Post>>("CachedPostRepo") {
    
    fun getFeed() = cacheFirst(
        cacheKey = "posts:feed",
        policy = CachePolicy.FEED,
        fetchFromNetwork = { SupabaseDataSource.fetchPosts() },
        getFromMemory = { memoryCache.get(key) },
        getFromDisk = { dao.getAll() },
        saveToMemory = { data, v -> memoryCache.put(key, data) },
        saveToDisk = { data, v -> dao.upsertAll(data) }
    )
}
```

---

## Métricas y Debugging

```kotlin
// Ver estadísticas de cache
val stats = memoryCache.stats()
Log.d("Cache", "Hit rate: ${stats.hitRate * 100}%")

// Ver health del sistema
cacheOrchestrator.cacheStats.collect { stats ->
    Log.d("CacheHealth", "Cached: ${stats.totalCached} items")
}
```

---

## Consideraciones de Performance

- **Memory footprint:** LruCache limita automáticamente el uso de RAM
- **Disk I/O:** Room usa transactions para batch operations
- **Network:** Deduplication previene requests redundantes
- **Battery:** WorkManager respeta Doze mode y restricciones

---

*Diseñado para escalar a millones de usuarios, priorizando experiencia del usuario sobre frescura absoluta de datos.*
