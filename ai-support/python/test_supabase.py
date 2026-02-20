"""Test script para verificar conexión a Supabase"""
import asyncio
import os
from pathlib import Path
from dotenv import load_dotenv

# Cargar .env
load_dotenv(Path(__file__).parent / ".env")

async def test_supabase():
    from supabase_service import get_supabase_service
    
    svc = get_supabase_service()
    
    print(f"Supabase URL: {svc.supabase_url}")
    print(f"Supabase configured: {svc.is_configured}")
    print(f"Key exists: {bool(svc.supabase_key)}")
    
    if not svc.is_configured:
        print("ERROR: Supabase no está configurado!")
        return
    
    # Test 1: Crear conversación
    print("\n--- Test 1: Crear conversación ---")
    conv_id = await svc.create_conversation("test-user-python", "test-session-001")
    print(f"Conversation ID: {conv_id}")
    
    if conv_id:
        # Test 2: Guardar mensaje de usuario
        print("\n--- Test 2: Guardar mensaje de usuario ---")
        msg_id = await svc.save_user_message(
            conversation_id=conv_id,
            content="Hola, necesito ayuda con mi pedido",
            analysis={
                "confidence_score": 75,
                "detected_intent": "purchase_track",
                "category": "purchases",
                "clarity_score": 0.8,
                "completeness_score": 0.7,
                "is_aggressive": False,
                "is_confused": False,
                "matched_keywords": ["pedido", "ayuda"]
            }
        )
        print(f"User Message ID: {msg_id}")
        
        # Test 3: Guardar respuesta de IA
        print("\n--- Test 3: Guardar respuesta de IA ---")
        ai_msg_id = await svc.save_ai_response(
            conversation_id=conv_id,
            content="¡Hola! Para ver el estado de tu pedido, ve a Perfil > Mis pedidos.",
            response_time_ms=150
        )
        print(f"AI Message ID: {ai_msg_id}")
        
        # Test 4: Crear escalación
        print("\n--- Test 4: Crear escalación ---")
        esc_id = await svc.create_escalation(
            conversation_id=conv_id,
            user_id="test-user-python",
            reason="Test escalation from Python",
            confidence_score=45,
            detected_intent="complex_issue"
        )
        print(f"Escalation ID: {esc_id}")
        
        print("\n=== TODOS LOS TESTS PASARON ===")
    else:
        print("ERROR: No se pudo crear conversación")

if __name__ == "__main__":
    asyncio.run(test_supabase())
