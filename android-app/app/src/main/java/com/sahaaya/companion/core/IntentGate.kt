package com.sahaaya.companion.core

class IntentGate {

    private val prescriptionKeywords = listOf(
        "antibiotic", "amoxicillin", "azithromycin", "metronidazole",
        "ciprofloxacin", "cefixime", "telmisartan", "amlodipine",
        "metformin", "insulin", "steroid", "prescribe", "prescription",
        "give medicine", "prescribe cheyyocha"
    )

    private val diagnosisKeywords = listOf(
        "diagnose", "diagnosis", "confirm disease", "differential diagnosis",
        "measles or chickenpox", "test result diagnosis"
    )

    private val clinicalDosePhrases = listOf(
        "dosage of", "dose of", "kitna dose", "how much dose", "prescribe dose",
        "drops calculation", "syrup dosage", "mg dose", "mg tablet kitna"
    )

    fun evaluate(query: String): GateResult {
        val lower = query.lowercase()

        // Explicit exceptions for standard ASHA kit protocols:
        val isAshaKitProtocol = (lower.contains("ifa") || lower.contains("iron tablet") || lower.contains("folic acid")) ||
                (lower.contains("zinc") && (lower.contains("tablet") || lower.contains("strip") || lower.contains("dast") || lower.contains("diarrhoea"))) ||
                (lower.contains("ors") && (lower.contains("how much") || lower.contains("volume") || lower.contains("packet") || lower.contains("sachet"))) ||
                (lower.contains("tepid") || lower.contains("sponge") || lower.contains("sponging"))

        // 1. Check prescription drugs
        for (kw in prescriptionKeywords) {
            if (lower.contains(kw)) {
                return GateResult(
                    isRefusal = true,
                    refusalMessage = "Out-of-scope clinical request: ASHA/ANM protocol does not allow independent prescription of prescription medications or antibiotics ($kw). Please refer the patient to a Medical Officer or Doctor at the PHC/CHC.",
                    reason = "PRESCRIPTION_OUT_OF_SCOPE"
                )
            }
        }

        // 2. Check clinical diagnosis
        for (kw in diagnosisKeywords) {
            if (lower.contains(kw)) {
                return GateResult(
                    isRefusal = true,
                    refusalMessage = "Out-of-scope diagnostic request: Medical diagnosis must be conducted by a qualified Medical Officer or Doctor with laboratory evaluation. Please refer the patient to the nearest PHC.",
                    reason = "DIAGNOSIS_OUT_OF_SCOPE"
                )
            }
        }

        // 3. Check clinical dosage requests (excluding standard kit items)
        if (!isAshaKitProtocol) {
            for (kw in clinicalDosePhrases) {
                if (lower.contains(kw)) {
                    return GateResult(
                        isRefusal = true,
                        refusalMessage = "Out-of-scope dosage request: Calculating non-kit pharmaceutical dosages requires a physician prescription. Please consult a Medical Officer.",
                        reason = "DOSAGE_OUT_OF_SCOPE"
                    )
                }
            }
        }

        return GateResult(isRefusal = false)
    }
}
