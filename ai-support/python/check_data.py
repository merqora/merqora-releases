"""Verificar datos en Supabase"""
import asyncio
import os
from pathlib import Path
from dotenv import load_dotenv
import httpx

load_dotenv(Path(__file__).parent / ".env")

async def check():
    url = os.getenv('SUPABASE_URL')
    key = os.getenv('SUPABASE_SERVICE_KEY')
    headers = {'apikey': key, 'Authorization': f'Bearer {key}'}
    
    async with httpx.AsyncClient() as client:
        # Conversations
        r = await client.get(f'{url}/rest/v1/support_conversations?select=*&order=created_at.desc&limit=5', headers=headers)
        print('=== CONVERSACIONES ===')
        for c in r.json():
            print(f"  {c['id'][:8]}... | user: {c['user_id']} | session: {c['session_id']}")
        
        # Messages
        r = await client.get(f'{url}/rest/v1/support_messages?select=*&order=created_at.desc&limit=10', headers=headers)
        print('\n=== MENSAJES ===')
        for m in r.json():
            content = m['content'][:50] if m['content'] else 'N/A'
            print(f"  {m['role']}: {content}...")
        
        # Escalations
        r = await client.get(f'{url}/rest/v1/ai_escalations?select=*&order=created_at.desc&limit=5', headers=headers)
        print('\n=== ESCALACIONES ===')
        for e in r.json():
            print(f"  {e['id'][:8]}... | reason: {e['reason']} | status: {e['status']}")
        
        # Stats
        r = await client.get(f'{url}/rest/v1/ai_stats_daily?select=*&order=date.desc&limit=5', headers=headers)
        print('\n=== STATS DIARIAS ===')
        for s in r.json():
            print(f"  {s['date']} | msgs: {s['total_messages']} | resolved: {s['ai_resolved']} | escalated: {s['escalated']}")

if __name__ == "__main__":
    asyncio.run(check())
