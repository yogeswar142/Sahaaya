package com.sahaaya.companion.core

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class VisitListEngine {

    fun parseDate(raw: String?, defaultDate: LocalDate): LocalDate {
        if (raw.isNullOrBlank()) return defaultDate
        val clean = raw.trim()
        if (clean.equals("nan", ignoreCase = true) || clean.equals("null", ignoreCase = true) ||
            clean.equals("none", ignoreCase = true) || clean.equals("nat", ignoreCase = true)) {
            return defaultDate
        }
        try {
            val d = LocalDate.parse(clean)
            if (d.year in 1920..(defaultDate.year + 2)) return d
        } catch (_: Exception) {}

        val patterns = listOf("d/M/yyyy", "d-M-yyyy", "yyyy/M/d", "d.M.yyyy")
        for (pattern in patterns) {
            try {
                val d = LocalDate.parse(clean, DateTimeFormatter.ofPattern(pattern))
                if (d.year in 1920..(defaultDate.year + 2)) return d
            } catch (_: Exception) {}
        }
        return defaultDate
    }

    fun prioritizeVisits(
        members: List<RegisterMember>,
        today: LocalDate = LocalDate.now()
    ): List<PrioritizedVisit> {
        val prioritized = mutableListOf<PrioritizedVisit>()

        for (member in members) {
            val dueDate = parseDate(member.dueDate, today)

            val daysDiff = ChronoUnit.DAYS.between(today, dueDate)
            val hasDanger = member.dangerFlag.isNotBlank() && !member.dangerFlag.equals("NONE", ignoreCase = true)

            if (hasDanger) {
                prioritized.add(
                    PrioritizedVisit(
                        member = member,
                        priorityGroup = PriorityGroup.DANGER,
                        priorityRank = 1,
                        urgencyBadge = "URGENT DANGER",
                        reason = "Reported danger sign: ${member.dangerFlag}. Immediate hospital referral required.",
                        daysDiff = daysDiff
                    )
                )
            } else if (daysDiff < 0) {
                val overdueDays = -daysDiff
                prioritized.add(
                    PrioritizedVisit(
                        member = member,
                        priorityGroup = PriorityGroup.OVERDUE,
                        priorityRank = 2,
                        urgencyBadge = "OVERDUE (${overdueDays}d)",
                        reason = "${member.nextDueItem} was due on ${member.dueDate} (${overdueDays} days overdue).",
                        daysDiff = overdueDays
                    )
                )
            } else {
                val upcomingText = if (daysDiff == 0L) "TODAY" else "IN ${daysDiff}d"
                prioritized.add(
                    PrioritizedVisit(
                        member = member,
                        priorityGroup = PriorityGroup.UPCOMING,
                        priorityRank = 3,
                        urgencyBadge = "UPCOMING ($upcomingText)",
                        reason = "${member.nextDueItem} scheduled for ${member.dueDate}.",
                        daysDiff = daysDiff
                    )
                )
            }
        }

        // Sorting rule:
        // 1. Group rank: DANGER (1) -> OVERDUE (2) -> UPCOMING (3)
        // 2. Within OVERDUE: sort by days overdue descending (most overdue first)
        // 3. Within UPCOMING: sort by days until due ascending (nearest due date first)
        // 4. Within DANGER: stable ordering by name
        return prioritized.sortedWith(
            compareBy<PrioritizedVisit> { it.priorityRank }
                .thenBy { visit ->
                    when (visit.priorityGroup) {
                        PriorityGroup.OVERDUE -> -visit.daysDiff // higher overdue days first
                        PriorityGroup.UPCOMING -> visit.daysDiff  // soonest upcoming first
                        PriorityGroup.DANGER -> 0L
                    }
                }
                .thenBy { it.member.name }
        )
    }
}
