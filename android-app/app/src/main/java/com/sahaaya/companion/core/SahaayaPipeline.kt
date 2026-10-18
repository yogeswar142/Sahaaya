package com.sahaaya.companion.core

class SahaayaPipeline(
    private val dangerRuleEngine: DangerRuleEngine,
    private val intentGate: IntentGate,
    private val bm25Retriever: Bm25Retriever
) {

    fun process(query: String): PipelineOutput {
        // Step 1: Hard-coded danger-sign escalation layer (ALWAYS FIRES FIRST)
        val dangerMatch = dangerRuleEngine.evaluate(query)
        if (dangerMatch.matched && dangerMatch.rule != null) {
            val rule = dangerMatch.rule
            return PipelineOutput.DangerEscalation(
                rule = rule,
                matchedPhrase = dangerMatch.matchedPhrase ?: query,
                actionText = "REFER NOW",
                citation = "Source: ${rule.chunkId}, ${rule.sourceCitation}"
            )
        }

        // Step 2: Intent Gate (Refuses out-of-scope dosage, prescription, and diagnosis)
        val gateResult = intentGate.evaluate(query)
        if (gateResult.isRefusal) {
            return PipelineOutput.ScopeRefusal(
                message = gateResult.refusalMessage ?: "This request is outside ASHA protocol. Please consult a doctor.",
                reason = gateResult.reason ?: "OUT_OF_SCOPE"
            )
        }

        // Step 3 & 4: BM25 Retrieval over grounded official protocol chunks
        val retrieval = bm25Retriever.search(query)
        if (retrieval == null) {
            return PipelineOutput.OutOfCorpus(
                message = "Not in my protocols, ask your ANM/doctor."
            )
        }

        val chunk = retrieval.chunk
        val citation = "Source: ${chunk.id}, ${chunk.sourceDocument}, p.${chunk.pageNumber}"
        return PipelineOutput.ProtocolAnswer(
            chunk = chunk,
            score = retrieval.score,
            citationText = citation
        )
    }
}
