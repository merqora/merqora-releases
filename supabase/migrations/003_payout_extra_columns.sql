-- Add bank transfer and Prex columns to payout_methods

ALTER TABLE payout_methods
ADD COLUMN IF NOT EXISTS prex_phone TEXT,
ADD COLUMN IF NOT EXISTS prex_account TEXT,
ADD COLUMN IF NOT EXISTS prex_alias TEXT,
ADD COLUMN IF NOT EXISTS bank_name TEXT,
ADD COLUMN IF NOT EXISTS account_type TEXT DEFAULT 'savings',
ADD COLUMN IF NOT EXISTS account_number TEXT,
ADD COLUMN IF NOT EXISTS holder_name TEXT,
ADD COLUMN IF NOT EXISTS holder_document TEXT;
