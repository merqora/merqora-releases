import httpx
import os
from pathlib import Path
from dotenv import load_dotenv
load_dotenv(Path(__file__).parent / ".env")

url = os.getenv('SUPABASE_URL', '')
key = os.getenv('SUPABASE_SERVICE_KEY', '')
headers = {'apikey': key, 'Authorization': f'Bearer {key}'}

# Get escalations
esc = httpx.get(f'{url}/rest/v1/ai_escalations?select=id,conversation_id,status&order=created_at.desc&limit=10', headers=headers).json()
print('=== ESCALACIONES ===')
for e in esc:
    print(f"  {e['id'][:12]}... | conv: {e['conversation_id'][:12] if e.get('conversation_id') else 'NULL'}... | status: {e['status']}")

# Get conversations
convs = httpx.get(f'{url}/rest/v1/support_conversations?select=id,status&limit=10', headers=headers).json()
print('\n=== CONVERSACIONES ===')
conv_ids = set()
for c in convs:
    conv_ids.add(c['id'])
    print(f"  {c['id'][:12]}... | status: {c['status']}")

# Check if escalation conversation_ids exist
print('\n=== VERIFICACION ===')
for e in esc:
    cid = e.get('conversation_id')
    if cid:
        exists = cid in conv_ids
        print(f"  Esc {e['id'][:8]}... -> Conv {cid[:8]}... exists: {exists}")
    else:
        print(f"  Esc {e['id'][:8]}... -> Conv NULL")
