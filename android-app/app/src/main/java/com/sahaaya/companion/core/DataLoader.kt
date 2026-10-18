package com.sahaaya.companion.core

object DataLoader {

    fun parseCsv(csvText: String): List<Map<String, String>> {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList()

        val header = parseCsvLine(lines[0])
        val results = mutableListOf<Map<String, String>>()

        for (i in 1 until lines.size) {
            val values = parseCsvLine(lines[i])
            val row = mutableMapOf<String, String>()
            for (j in header.indices) {
                row[header[j]] = if (j < values.size) values[j] else ""
            }
            results.add(row)
        }
        return results
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var insideQuotes = false

        for (c in line) {
            when {
                c == '"' -> insideQuotes = !insideQuotes
                c == ',' && !insideQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    fun parseRegister(csvText: String): List<RegisterMember> {
        val rows = parseCsv(csvText)
        return rows.map { row ->
            RegisterMember(
                familyId = row["family_id"] ?: "",
                memberId = row["member_id"] ?: "",
                name = row["name"] ?: "",
                memberType = row["member_type"] ?: "",
                targetDate = row["target_date"] ?: "",
                lastVisitDate = row["last_visit_date"] ?: "",
                dangerFlag = row["danger_flag"] ?: "NONE",
                vaccinesGiven = row["vaccines_given"] ?: "",
                nextDueItem = row["next_due_item"] ?: "",
                dueDate = row["due_date"] ?: ""
            )
        }
    }

    fun parseProtocolChunks(jsonText: String): List<ProtocolChunk> {
        // Robust lightweight regex-based parser for protocol_chunks.json
        val chunkBlocks = jsonText.split(Regex("\\},\\s*\\{"))
        val list = mutableListOf<ProtocolChunk>()

        for (block in chunkBlocks) {
            val id = extractJsonString(block, "id")
            val title = extractJsonString(block, "title")
            val language = extractJsonString(block, "language")
            val text = extractJsonString(block, "text")
            val sourceDoc = extractJsonString(block, "source_document")
            val sourceUrl = extractJsonString(block, "source_url")
            val pageNum = extractJsonInt(block, "page_number")

            if (id.isNotBlank()) {
                list.add(
                    ProtocolChunk(
                        id = id,
                        title = title,
                        language = language,
                        text = text,
                        sourceDocument = sourceDoc,
                        sourceUrl = sourceUrl,
                        pageNumber = pageNum
                    )
                )
            }
        }
        return list
    }

    fun parseDangerRules(jsonText: String): List<DangerRule> {
        // Danger rules have nested phrasings: "phrasings": { "en": [...], "hinglish": [...], "telugu_latin": [...] }
        val ruleBlocks = jsonText.split(Regex("\\},\\s*\\{\\s*\"id\":"))
        val list = mutableListOf<DangerRule>()

        for (rawBlock in ruleBlocks) {
            val block = if (!rawBlock.trim().startsWith("\"id\"")) "\"id\":" + rawBlock else rawBlock
            val id = extractJsonString(block, "id")
            val chunkId = extractJsonString(block, "chunk_id")
            val category = extractJsonString(block, "category")
            val dangerSign = extractJsonString(block, "danger_sign")
            val action = extractJsonString(block, "action")
            val citation = extractJsonString(block, "source_citation")
            val note = extractJsonString(block, "note")

            val enPhrases = extractJsonArray(block, "en")
            val hinglishPhrases = extractJsonArray(block, "hinglish")
            val teluguPhrases = extractJsonArray(block, "telugu_latin")

            if (id.isNotBlank()) {
                val phrasings = mapOf(
                    "en" to enPhrases,
                    "hinglish" to hinglishPhrases,
                    "telugu_latin" to teluguPhrases
                )
                list.add(
                    DangerRule(
                        id = id,
                        chunkId = chunkId,
                        category = category,
                        dangerSign = dangerSign,
                        action = action,
                        sourceCitation = citation,
                        note = note,
                        phrasings = phrasings
                    )
                )
            }
        }
        return list
    }

    private fun extractJsonString(block: String, key: String): String {
        val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"")
        val match = pattern.find(block)
        return match?.groupValues?.get(1)?.replace("\\n", "\n") ?: ""
    }

    private fun extractJsonInt(block: String, key: String): Int {
        val pattern = Regex("\"$key\"\\s*:\\s*([0-9]+)")
        val match = pattern.find(block)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun extractJsonArray(block: String, arrayKey: String): List<String> {
        val pattern = Regex("\"$arrayKey\"\\s*:\\s*\\[([^\\]]*)\\]")
        val match = pattern.find(block) ?: return emptyList()
        val arrayContent = match.groupValues[1]
        val itemPattern = Regex("\"([^\"]*)\"")
        return itemPattern.findAll(arrayContent).map { it.groupValues[1] }.toList()
    }
}
