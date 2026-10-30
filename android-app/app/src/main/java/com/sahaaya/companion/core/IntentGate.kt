package com.sahaaya.companion.core

import java.util.regex.Pattern

class IntentGate {

    fun evaluate(query: String): GateResult {
        val lower = query.lowercase().replace(Regex("[^a-z0-9\\s]"), " ")

        // Explicit exceptions for standard ASHA kit & immunization protocols:
        val isAshaKitOrVaccine = (lower.contains("ifa") || lower.contains("iron") || lower.contains("folic acid") || lower.contains("lal goli")) ||
                (lower.contains("zinc") && (lower.contains("tablet") || lower.contains("strip") || lower.contains("dast") || lower.contains("diarrhoea"))) ||
                (lower.contains("ors") && (lower.contains("how much") || lower.contains("volume") || lower.contains("packet") || lower.contains("sachet") || lower.contains("jeevan jal") || lower.contains("pani"))) ||
                (lower.contains("calcium")) ||
                (lower.contains("albendazole") || lower.contains("keede")) ||
                (lower.contains("tepid") || lower.contains("sponge") || lower.contains("sponging")) ||
                (lower.contains("tika") || lower.contains("teeka") || lower.contains("vaccin") || lower.contains("bcg") || lower.contains("penta") || lower.contains("opv") || lower.contains("mr") || lower.contains("fipv") || lower.contains("rotavirus") || lower.contains("td") || lower.contains("booster"))

        // 1. PC-PNDT Sex determination
        if (lower.contains("ladka") && (lower.contains("ladki") || lower.contains("sex") || lower.contains("gender") || lower.contains("jaanch"))) {
            return GateResult(
                isRefusal = true,
                refusalMessage = "Sex determination is strictly prohibited by law under the PC-PNDT Act. Every pregnant woman must receive complete antenatal care (SCP-1.2).",
                reason = "PCPNDT_PROHIBITED",
                citations = listOf("SCP-1.2")
            )
        }

        // 2. Administrative queries (salary, app issues, login, portal)
        // Word boundary regex prevents false positives like 'operation' matching 'ration'
        val adminPattern = Pattern.compile("\\b(salary|incentive|manthan|login|otp|u-?win|uwin|anmol|techo|poshan|app me|ration|rashan|ayushman|praman|certificate)\\b")
        if (adminPattern.matcher(lower).find()) {
            return GateResult(
                isRefusal = true,
                refusalMessage = "Administrative and application support queries are outside clinical protocol scope. Please contact your Block Program Manager (SCP-1.2).",
                reason = "ADMINISTRATIVE_OUT_OF_SCOPE",
                citations = listOf("SCP-1.2")
            )
        }

        // 3. Uncovered conditions & specialized lab diagnostics
        val uncoveredPattern = Pattern.compile("\\b(hiv|aids|covid|corona|sars|dengue|typhoid|widal|malaria report|cancer|tumor|thyroid|tsh|tuberculosis|tb|fracture|broken bone|haddi toot|dog bite|kutta kata|rabies|cataract|motiyabind|inhaler|nebulizer|ultrasound|usg|x-?ray|ct scan|mri|bistar geela|bedwetting|enuresis)\\b")
        val uncMatch = uncoveredPattern.matcher(lower)
        if (uncMatch.find()) {
            val matched = uncMatch.group(0)
            return GateResult(
                isRefusal = true,
                refusalMessage = "Diagnosis and treatment of $matched is outside frontline ASHA protocol scope. Please refer the patient to a Medical Officer at the PHC (SCP-1.2).",
                reason = "DIAGNOSIS_OUT_OF_SCOPE",
                citations = listOf("SCP-1.2")
            )
        }

        // 4. Non-kit prescription medications & pharmaceutical dosing/usage (Dolo 650, Crocin, Antibiotics, IV fluids, etc.)
        val rxPattern = Pattern.compile("\\b(dolo|dolo\\s*650|crocin|calpol|paracetamol|pcm|paracip|combiflam|ibuprofen|meftal|amoxicillin|augmentin|cefixime|antibiotic|azithromycin|azithral|metformin|amlodipine|telmisartan|insulin|cough syrup|syrup|tablet|capsule|dosage|dose kitna|kitna dena|kitni goli|kitna ml|usage|use kaise|how to use|prescribe|prescription|drops calculation|drip|saline|injection)\\b")
        if (!isAshaKitOrVaccine) {
            val rxMatch = rxPattern.matcher(lower)
            if (rxMatch.find()) {
                val matchedDrug = rxMatch.group(0)
                return GateResult(
                    isRefusal = true,
                    refusalMessage = "Medicine prescription and usage ($matchedDrug) is outside frontline protocol scope. ASHAs/ANMs cannot prescribe non-kit pharmaceuticals. Please refer to a qualified Medical Officer (SCP-1.2).",
                    reason = "PRESCRIPTION_OUT_OF_SCOPE",
                    citations = listOf("SCP-1.2")
                )
            }
        }

        return GateResult(isRefusal = false)
    }
}
