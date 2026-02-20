#include <pybind11/pybind11.h>
#include <pybind11/stl.h>
#include "scoring_engine.hpp"

namespace py = pybind11;

PYBIND11_MODULE(scoring_engine, m) {
    m.doc() = "Rendly AI Support - Scoring Engine (C++)";

    // AnalysisResult struct
    py::class_<rendly::ai::AnalysisResult>(m, "AnalysisResult")
        .def(py::init<>())
        .def_readwrite("confidence_score", &rendly::ai::AnalysisResult::confidence_score)
        .def_readwrite("detected_intent", &rendly::ai::AnalysisResult::detected_intent)
        .def_readwrite("clarity_score", &rendly::ai::AnalysisResult::clarity_score)
        .def_readwrite("completeness_score", &rendly::ai::AnalysisResult::completeness_score)
        .def_readwrite("is_aggressive", &rendly::ai::AnalysisResult::is_aggressive)
        .def_readwrite("is_confused", &rendly::ai::AnalysisResult::is_confused)
        .def_readwrite("is_spam", &rendly::ai::AnalysisResult::is_spam)
        .def_readwrite("matched_keywords", &rendly::ai::AnalysisResult::matched_keywords)
        .def_readwrite("recommendation", &rendly::ai::AnalysisResult::recommendation)
        .def("to_dict", [](const rendly::ai::AnalysisResult& self) {
            py::dict d;
            d["confidence_score"] = self.confidence_score;
            d["detected_intent"] = self.detected_intent;
            d["clarity_score"] = self.clarity_score;
            d["completeness_score"] = self.completeness_score;
            d["is_aggressive"] = self.is_aggressive;
            d["is_confused"] = self.is_confused;
            d["is_spam"] = self.is_spam;
            d["matched_keywords"] = self.matched_keywords;
            d["recommendation"] = self.recommendation;
            return d;
        });

    // IntentPattern struct
    py::class_<rendly::ai::IntentPattern>(m, "IntentPattern")
        .def(py::init<>())
        .def_readwrite("intent_id", &rendly::ai::IntentPattern::intent_id)
        .def_readwrite("category", &rendly::ai::IntentPattern::category)
        .def_readwrite("keywords", &rendly::ai::IntentPattern::keywords)
        .def_readwrite("patterns", &rendly::ai::IntentPattern::patterns)
        .def_readwrite("base_confidence", &rendly::ai::IntentPattern::base_confidence);

    // ScoringEngine class
    py::class_<rendly::ai::ScoringEngine>(m, "ScoringEngine")
        .def_static("instance", &rendly::ai::ScoringEngine::instance, 
            py::return_value_policy::reference)
        .def("initialize", &rendly::ai::ScoringEngine::initialize)
        .def("analyze", &rendly::ai::ScoringEngine::analyze,
            py::arg("user_message"),
            "Analyze a user message and return detailed results")
        .def("calculate_confidence_score", &rendly::ai::ScoringEngine::calculate_confidence_score,
            py::arg("user_message"),
            "Calculate confidence score (0-100) for a user message")
        .def("load_intent_patterns", &rendly::ai::ScoringEngine::load_intent_patterns,
            py::arg("patterns"),
            "Load custom intent patterns");

    // Convenience function
    m.def("analyze_message", [](const std::string& message) {
        return rendly::ai::ScoringEngine::instance().analyze(message);
    }, py::arg("message"), "Quick analyze a message");

    m.def("get_confidence", [](const std::string& message) {
        return rendly::ai::ScoringEngine::instance().calculate_confidence_score(message);
    }, py::arg("message"), "Quick get confidence score");
}
