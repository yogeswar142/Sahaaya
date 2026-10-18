package com.sahaaya.companion.core

import kotlin.math.ln

class Bm25Retriever(
    private val chunks: List<ProtocolChunk>,
    private val k1: Double = 1.5,
    private val b: Double = 0.75,
    private val scoreThreshold: Double = 0.8
) {

    private val stopwords = setOf(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "as", "at",
        "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "did", "do",
        "does", "doing", "down", "during", "each", "few", "for", "from", "further", "had", "has", "have", "having",
        "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "it",
        "its", "itself", "just", "me", "more", "most", "my", "myself", "no", "nor", "not", "of", "off", "on", "once",
        "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "s", "same", "she", "should", "so",
        "some", "such", "than", "that", "the", "their", "theirs", "them", "themselves", "then", "there", "these",
        "they", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "were", "what",
        "when", "where", "which", "while", "who", "whom", "why", "will", "with", "you", "your", "yours",
        // Hindi/Telugu function words
        "ka", "ki", "ke", "ko", "me", "mein", "par", "se", "aur", "ya", "tha", "the", "thi", "hai", "hain", "kya", "hota", "hote", "hoti",
        "lo", "ki", "tho", "ga", "lo", "undali", "ivvali", "cheyali", "ela", "eppudu", "em", "ani", "mariyu"
    )

    // Code-mixed semantic synonym expansions for BM25 matching
    private val synonymMap = mapOf(
        "dast" to listOf("diarrhoea", "loose", "stool"),
        "virochanalu" to listOf("diarrhoea", "loose", "stool"),
        "virochanam" to listOf("diarrhoea", "loose", "stool"),
        "bukhar" to listOf("fever", "temperature"),
        "jwaram" to listOf("fever", "temperature"),
        "bachhe" to listOf("child", "children"),
        "baccha" to listOf("child", "infant"),
        "bacche" to listOf("child", "infant"),
        "pillalu" to listOf("children", "child"),
        "pillalaku" to listOf("children", "child"),
        "pillalalo" to listOf("children", "child"),
        "paalu" to listOf("milk", "breastfeed", "breastfeeding"),
        "doodh" to listOf("milk", "breastfeed", "breastfeeding"),
        "sthanyam" to listOf("breastfeeding", "milk"),
        "garbhwati" to listOf("pregnant", "pregnancy", "antenatal"),
        "garbhavathi" to listOf("pregnant", "pregnancy", "antenatal"),
        "kadupulo" to listOf("pregnancy", "womb", "foetus"),
        "rakthaheenatha" to listOf("anaemia", "pallor", "iron"),
        "khoon" to listOf("blood", "iron", "anaemia"),
        "shishu" to listOf("newborn", "baby", "infant"),
        "puttina" to listOf("born", "newborn", "birth"),
        "navjaat" to listOf("newborn", "birth")
    )

    private val docTokens = mutableListOf<List<String>>()
    private val docLengths = mutableListOf<Int>()
    private val termDocFreq = mutableMapOf<String, Int>()
    private val numDocs = chunks.size
    private var avgDocLength = 0.0

    init {
        var totalLength = 0
        for (chunk in chunks) {
            val content = "${chunk.title} ${chunk.title} ${chunk.text}"
            val tokens = tokenizeAndExpand(content)
            docTokens.add(tokens)
            docLengths.add(tokens.size)
            totalLength += tokens.size

            val uniqueTerms = tokens.toSet()
            for (term in uniqueTerms) {
                termDocFreq[term] = termDocFreq.getOrDefault(term, 0) + 1
            }
        }
        avgDocLength = if (numDocs > 0) totalLength.toDouble() / numDocs else 1.0
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(" ")
            .filter { it.isNotBlank() && it.length > 1 && !stopwords.contains(it) }
    }

    private fun tokenizeAndExpand(text: String): List<String> {
        val baseTokens = tokenize(text)
        val expanded = mutableListOf<String>()
        for (token in baseTokens) {
            expanded.add(token)
            synonymMap[token]?.let { syns ->
                expanded.addAll(syns)
            }
        }
        return expanded
    }

    fun search(query: String): RetrievalResult? {
        val queryTerms = tokenizeAndExpand(query)
        if (queryTerms.isEmpty()) return null

        var bestScore = -1.0
        var bestIndex = -1

        for (i in 0 until numDocs) {
            val tokens = docTokens[i]
            val docLen = docLengths[i]
            val tfMap = mutableMapOf<String, Int>()
            for (t in tokens) {
                tfMap[t] = tfMap.getOrDefault(t, 0) + 1
            }

            var score = 0.0
            for (term in queryTerms) {
                val tf = tfMap[term] ?: 0
                if (tf == 0) continue
                val df = termDocFreq[term] ?: 1

                // Robertson-Spärck Jones IDF
                val idf = ln(1.0 + (numDocs - df + 0.5) / (df + 0.5))

                // BM25 term weight
                val num = tf * (k1 + 1.0)
                val denom = tf + k1 * (1.0 - b + b * (docLen / avgDocLength))
                score += idf * (num / denom)
            }

            if (score > bestScore) {
                bestScore = score
                bestIndex = i
            }
        }

        return if (bestScore >= scoreThreshold && bestIndex >= 0) {
            RetrievalResult(chunks[bestIndex], bestScore)
        } else {
            null
        }
    }
}
