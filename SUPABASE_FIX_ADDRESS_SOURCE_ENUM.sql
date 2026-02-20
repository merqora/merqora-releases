-- ═══════════════════════════════════════════════════════════════════════════════════
-- FIX: Add 'map' to address_source enum
-- The Kotlin AddressSource enum has MAP but the DB only has: gps, manual, autocomplete
-- This causes INSERT failures when users pick addresses from the map
-- ═══════════════════════════════════════════════════════════════════════════════════

ALTER TYPE address_source ADD VALUE IF NOT EXISTS 'map';
