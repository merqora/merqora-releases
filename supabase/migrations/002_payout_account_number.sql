ALTER TABLE payout_methods
ADD COLUMN IF NOT EXISTS mp_account_number TEXT,
ADD COLUMN IF NOT EXISTS mp_account_type TEXT DEFAULT 'cvu';

ALTER TABLE disbursements
ADD COLUMN IF NOT EXISTS seller_mp_account TEXT,
ADD COLUMN IF NOT EXISTS seller_mp_account_type TEXT DEFAULT 'cvu';
