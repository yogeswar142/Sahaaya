package com.sahaaya.companion.core

import org.json.JSONArray
import org.json.JSONObject

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
        val list = mutableListOf<ProtocolChunk>()
        try {
            val array = JSONArray(jsonText)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val title = obj.optString("title", "")
                val language = obj.optString("language", "en")
                val text = obj.optString("text", "")
                val sourceDoc = obj.optString("source_document", obj.optString("sourceDocument", ""))
                val sourceUrl = obj.optString("source_url", obj.optString("sourceUrl", ""))
                val pageNum = obj.optInt("page_number", obj.optInt("pageNumber", 1))

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseDangerRules(jsonText: String): List<DangerRule> {
        val list = mutableListOf<DangerRule>()
        try {
            val array = JSONArray(jsonText)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val chunkId = obj.optString("chunk_id", "")
                val category = obj.optString("category", "")
                val dangerSign = obj.optString("danger_sign", "")
                val action = obj.optString("action", "REFER NOW")
                val citation = obj.optString("source_citation", "")
                val note = obj.optString("note", "")

                val phrasings = mutableMapOf<String, List<String>>()
                val phrasingsObj = obj.optJSONObject("phrasings")
                if (phrasingsObj != null) {
                    val keys = phrasingsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val arr = phrasingsObj.optJSONArray(key)
                        if (arr != null) {
                            val phrases = mutableListOf<String>()
                            for (j in 0 until arr.length()) {
                                phrases.add(arr.getString(j))
                            }
                            phrasings[key] = phrases
                        }
                    }
                }

                if (id.isNotBlank()) {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
