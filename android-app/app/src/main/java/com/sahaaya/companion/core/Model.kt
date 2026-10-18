package com.sahaaya.companion.core

data class ProtocolChunk(
    val id: String,
    val title: String,
    val language: String,
    val text: String,
    val sourceDocument: String,
    val sourceUrl: String,
    val pageNumber: Int
)

data class DangerRule(
    val id: String,
    val chunkId: String,
    val category: String,
    val dangerSign: String,
    val action: String,
    val sourceCitation: String,
    val note: String,
    val phrasings: Map<String, List<String>>
)

data class DangerMatchResult(
    val matched: Boolean,
    val rule: DangerRule? = null,
    val matchedPhrase: String? = null
)

data class GateResult(
    val isRefusal: Boolean,
    val refusalMessage: String? = null,
    val reason: String? = null
)

data class RetrievalResult(
    val chunk: ProtocolChunk,
    val score: Double
)

sealed class PipelineOutput {
    data class DangerEscalation(
        val rule: DangerRule,
        val matchedPhrase: String,
        val actionText: String = "REFER NOW",
        val citation: String
    ) : PipelineOutput()

    data class ScopeRefusal(
        val message: String,
        val reason: String
    ) : PipelineOutput()

    data class ProtocolAnswer(
        val chunk: ProtocolChunk,
        val score: Double,
        val citationText: String
    ) : PipelineOutput()

    data class OutOfCorpus(
        val message: String = "Not in my protocols, ask your ANM/doctor."
    ) : PipelineOutput()
}

data class RegisterMember(
    val familyId: String,
    val memberId: String,
    val name: String,
    val memberType: String,
    val targetDate: String,
    val lastVisitDate: String,
    val dangerFlag: String,
    val vaccinesGiven: String,
    val nextDueItem: String,
    val dueDate: String
)

enum class PriorityGroup {
    DANGER,
    OVERDUE,
    UPCOMING
}

data class PrioritizedVisit(
    val member: RegisterMember,
    val priorityGroup: PriorityGroup,
    val priorityRank: Int,
    val urgencyBadge: String,
    val reason: String,
    val daysDiff: Long
)
