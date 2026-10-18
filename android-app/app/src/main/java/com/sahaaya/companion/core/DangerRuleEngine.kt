package com.sahaaya.companion.core

class DangerRuleEngine(private val rules: List<DangerRule>) {

    data class PhraseEntry(
        val normalizedPhrase: String,
        val tokens: List<String>,
        val originalPhrase: String,
        val rule: DangerRule
    )

    private val phraseEntries: List<PhraseEntry>

    init {
        val entries = mutableListOf<PhraseEntry>()
        for (rule in rules) {
            for ((_, phrases) in rule.phrasings) {
                for (phrase in phrases) {
                    val norm = normalize(phrase)
                    if (norm.isNotBlank()) {
                        val tokens = norm.split(" ").filter { it.isNotBlank() }
                        entries.add(PhraseEntry(norm, tokens, phrase, rule))
                    }
                }
            }
        }
        // Sort by token count descending, then character length descending, so more specific patterns match first
        phraseEntries = entries.sortedWith(
            compareByDescending<PhraseEntry> { it.tokens.size }
                .thenByDescending { it.normalizedPhrase.length }
        )
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun evaluate(query: String): DangerMatchResult {
        val normQuery = normalize(query)
        if (normQuery.isBlank()) {
            return DangerMatchResult(matched = false)
        }

        val queryTokens = normQuery.split(" ").filter { it.isNotBlank() }.toSet()

        for (entry in phraseEntries) {
            // 1. Direct substring match
            if (normQuery.contains(entry.normalizedPhrase)) {
                return DangerMatchResult(
                    matched = true,
                    rule = entry.rule,
                    matchedPhrase = entry.originalPhrase
                )
            }

            // 2. All tokens of multi-word phrase present in query
            if (entry.tokens.size >= 2 && entry.tokens.all { queryTokens.contains(it) }) {
                return DangerMatchResult(
                    matched = true,
                    rule = entry.rule,
                    matchedPhrase = entry.originalPhrase
                )
            }
        }

        return DangerMatchResult(matched = false)
    }
}
