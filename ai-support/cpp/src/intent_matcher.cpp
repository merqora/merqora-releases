#include "scoring_engine.hpp"
#include <algorithm>
#include <cmath>

namespace rendly {
namespace ai {

IntentMatcher::IntentMatcher() {
    // Load default Rendly support intents
    patterns_ = {
        // Compras
        {
            "purchase_status",
            "compras",
            {"pedido", "compra", "orden", "estado", "tracking", "seguimiento", "llega", "entrega"},
            {"donde esta mi pedido", "cuando llega", "estado de mi compra", "rastrear pedido"},
            0.8f
        },
        {
            "purchase_problem",
            "compras",
            {"problema", "error", "fallo", "no llego", "perdido", "dañado", "roto", "incorrecto"},
            {"mi pedido no llego", "producto dañado", "me enviaron otro", "no es lo que pedi"},
            0.75f
        },
        {
            "purchase_cancel",
            "compras",
            {"cancelar", "devolver", "reembolso", "anular", "deshacer"},
            {"quiero cancelar", "como cancelo", "devolver producto", "quiero reembolso"},
            0.85f
        },
        // Pagos
        {
            "payment_methods",
            "pagos",
            {"pago", "tarjeta", "metodo", "agregar", "cambiar", "eliminar"},
            {"agregar tarjeta", "metodos de pago", "cambiar forma de pago"},
            0.8f
        },
        {
            "payment_problem",
            "pagos",
            {"rechazado", "fallo", "error", "cobro", "doble", "no paso"},
            {"pago rechazado", "me cobraron dos veces", "error en pago"},
            0.7f
        },
        {
            "refund",
            "pagos",
            {"reembolso", "devolucion", "dinero", "regreso", "credito"},
            {"cuando llega reembolso", "no me devolvieron", "quiero mi dinero"},
            0.75f
        },
        // Cuenta
        {
            "account_access",
            "cuenta",
            {"contraseña", "password", "acceso", "entrar", "login", "sesion", "recuperar"},
            {"olvide contraseña", "no puedo entrar", "recuperar cuenta", "cambiar password"},
            0.85f
        },
        {
            "account_settings",
            "cuenta",
            {"perfil", "configuracion", "datos", "nombre", "foto", "email", "telefono"},
            {"cambiar nombre", "actualizar email", "editar perfil"},
            0.8f
        },
        {
            "account_delete",
            "cuenta",
            {"eliminar", "borrar", "cerrar", "desactivar", "cuenta"},
            {"eliminar cuenta", "cerrar mi cuenta", "borrar perfil"},
            0.9f
        },
        // Envíos
        {
            "shipping_info",
            "envios",
            {"envio", "direccion", "domicilio", "entregar", "destino"},
            {"cambiar direccion", "agregar direccion", "a donde envian"},
            0.8f
        },
        {
            "shipping_problem",
            "envios",
            {"no llego", "perdido", "demora", "tarde", "retraso"},
            {"mi paquete no llega", "demora mucho", "paquete perdido"},
            0.7f
        },
        // Ventas
        {
            "sell_how",
            "ventas",
            {"vender", "publicar", "producto", "anuncio", "listado"},
            {"como vendo", "quiero vender", "publicar producto"},
            0.85f
        },
        {
            "sell_payment",
            "ventas",
            {"cobrar", "pago", "comision", "retiro", "transferencia", "venta"},
            {"cuando me pagan", "retirar dinero", "comision por venta"},
            0.8f
        },
        // Seguridad
        {
            "security_report",
            "seguridad",
            {"reportar", "denuncia", "fraude", "estafa", "sospechoso", "falso"},
            {"reportar usuario", "me estafaron", "cuenta falsa"},
            0.75f
        },
        {
            "security_verify",
            "seguridad",
            {"verificar", "verificacion", "identidad", "2fa", "autenticacion"},
            {"verificar cuenta", "activar 2fa", "codigo verificacion"},
            0.85f
        },
        // App general
        {
            "app_bug",
            "app",
            {"error", "bug", "falla", "crash", "cierra", "lento", "no funciona", "no carga"},
            {"la app no funciona", "se cierra sola", "esta muy lenta", "no carga"},
            0.7f
        },
        {
            "app_help",
            "app",
            {"ayuda", "como", "donde", "tutorial", "usar", "funciona"},
            {"como funciona", "como uso", "donde encuentro", "necesito ayuda"},
            0.6f
        },
        // Greeting / Generic
        {
            "greeting",
            "general",
            {"hola", "buenos", "buenas", "hey", "saludos"},
            {"hola", "buenos dias", "buenas tardes", "buenas noches"},
            0.95f
        }
    };
}

IntentMatcher::~IntentMatcher() = default;

void IntentMatcher::load_patterns(const std::vector<IntentPattern>& patterns) {
    patterns_ = patterns;
}

float IntentMatcher::calculate_pattern_match(
    const std::string& text,
    const IntentPattern& pattern,
    const std::vector<std::string>& keywords
) const {
    float score = 0.0f;
    int matches = 0;
    
    // Check keyword matches
    for (const auto& kw : pattern.keywords) {
        if (text.find(kw) != std::string::npos) {
            matches++;
        }
        // Also check extracted keywords
        for (const auto& user_kw : keywords) {
            if (user_kw.find(kw) != std::string::npos || kw.find(user_kw) != std::string::npos) {
                matches++;
            }
        }
    }
    
    if (!pattern.keywords.empty()) {
        score += 0.5f * (static_cast<float>(matches) / pattern.keywords.size());
    }
    
    // Check pattern matches (exact phrases)
    for (const auto& p : pattern.patterns) {
        if (text.find(p) != std::string::npos) {
            score += 0.3f;
            break;
        }
    }
    
    // Apply base confidence
    score *= pattern.base_confidence;
    
    return std::min(1.0f, score);
}

std::pair<std::string, float> IntentMatcher::match_intent(
    const std::string& text,
    const std::vector<std::string>& keywords
) const {
    std::string best_intent = "unknown";
    float best_score = 0.0f;
    
    for (const auto& pattern : patterns_) {
        float score = calculate_pattern_match(text, pattern, keywords);
        
        if (score > best_score) {
            best_score = score;
            best_intent = pattern.intent_id;
        }
    }
    
    return {best_intent, best_score};
}

} // namespace ai
} // namespace rendly
