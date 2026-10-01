package com.example.data.ai

import java.util.Locale

/**
 * Intelligent local knowledge base and formula generator for everyday work and Excel formulas
 * when offline or when internet connection is lost.
 */
object OfflineAssistantEngine {

    data class OfflineResult(
        val reply: String,
        val formula: String? = null
    )

    fun processQuery(query: String): OfflineResult {
        val q = query.lowercase(Locale.ROOT).trim()

        // 1. Excel / Sheets formula requests
        if (q.contains("xlookup") || (q.contains("lookup") && q.contains("modern"))) {
            val formula = "=XLOOKUP(A2, Products!A:A, Products!B:B, \"Not Found\", 0)"
            val reply = "Here is the modern XLOOKUP formula for Sheets/Excel:\n\n" +
                    "Formula: $formula\n\n" +
                    "• A2: The lookup value you are searching for.\n" +
                    "• Products!A:A: Lookup array (column where key exists).\n" +
                    "• Products!B:B: Return array (column with values to return).\n" +
                    "• \"Not Found\": Value returned if no match is found.\n" +
                    "• 0: Exact match mode (default in XLOOKUP)."
            return OfflineResult(reply, formula)
        }

        if (q.contains("vlookup")) {
            val formula = "=VLOOKUP(A2, Products!A:D, 3, FALSE)"
            val reply = "Here is the classic VLOOKUP formula:\n\n" +
                    "Formula: $formula\n\n" +
                    "• A2: The lookup value.\n" +
                    "• Products!A:D: The table range containing the data.\n" +
                    "• 3: The 3rd column from which to return data.\n" +
                    "• FALSE: Enforces exact match."
            return OfflineResult(reply, formula)
        }

        if (q.contains("index") && q.contains("match")) {
            val formula = "=INDEX(Sales!C:C, MATCH(A2, Sales!A:A, 0))"
            val reply = "Here is the two-way dynamic INDEX + MATCH formula:\n\n" +
                    "Formula: $formula\n\n" +
                    "• Sales!C:C: The range containing the result you want.\n" +
                    "• MATCH(A2, Sales!A:A, 0): Finds the row number of A2 in column A with exact match (0).\n" +
                    "• Faster and more flexible than VLOOKUP because lookup column can be anywhere."
            return OfflineResult(reply, formula)
        }

        if (q.contains("sumif") || q.contains("sum if") || q.contains("conditional sum")) {
            val formula = "=SUMIFS(C2:C100, A2:A100, \"Electronics\", B2:B100, \">100\")"
            val reply = "Here is the multi-condition SUMIFS formula:\n\n" +
                    "Formula: $formula\n\n" +
                    "• C2:C100: Sum range (the numbers to add).\n" +
                    "• A2:A100, \"Electronics\": Condition 1 (category).\n" +
                    "• B2:B100, \">100\": Condition 2 (unit price greater than 100)."
            return OfflineResult(reply, formula)
        }

        if (q.contains("countif") || q.contains("count if")) {
            val formula = "=COUNTIFS(A2:A100, \"Completed\", B2:B100, \">=\"&TODAY())"
            val reply = "Here is the COUNTIFS formula for conditional counts:\n\n" +
                    "Formula: $formula\n\n" +
                    "• Counts rows where status is \"Completed\" and date is on or after today."
            return OfflineResult(reply, formula)
        }

        if (q.contains("iferror") || q.contains("#n/a") || q.contains("fix error") || q.contains("error")) {
            val formula = "=IFERROR(VLOOKUP(A2, Data!A:C, 2, FALSE), \"-\")"
            val reply = "Here is how to wrap any formula in IFERROR to cleanly prevent `#N/A` or `#DIV/0!` errors:\n\n" +
                    "Formula: $formula\n\n" +
                    "• If the inner formula succeeds, it displays the calculation.\n" +
                    "• If an error occurs, it displays a neat dash \"-\" instead of ugly error codes."
            return OfflineResult(reply, formula)
        }

        if (q.contains("cagr") || q.contains("growth rate") || q.contains("growth")) {
            val formula = "=(End_Value/Start_Value)^(1/Years)-1"
            val reply = "Here is the Compound Annual Growth Rate (CAGR) formula for Excel/Sheets:\n\n" +
                    "Formula: =(B10/B2)^(1/5)-1\n\n" +
                    "• B10: Final period value.\n" +
                    "• B2: Initial period value.\n" +
                    "• 5: Total number of compounding years.\n" +
                    "• Format the result cell as Percentage (%) with 2 decimals."
            return OfflineResult(reply, "=(B10/B2)^(1/5)-1")
        }

        if (q.contains("profit margin") || q.contains("margin")) {
            val formula = "=(B2-C2)/B2"
            val reply = "Here is the Profit Margin formula:\n\n" +
                    "Formula: =(Revenue - Cost) / Revenue\n" +
                    "Example: $formula\n\n" +
                    "• B2: Gross Revenue.\n" +
                    "• C2: Total Costs / COGS.\n" +
                    "• Format as Percentage."
            return OfflineResult(reply, formula)
        }

        if (q.contains("pmt") || q.contains("loan") || q.contains("mortgage") || q.contains("payment")) {
            val formula = "=PMT(5%/12, 360, -350000)"
            val reply = "Here is the loan payment PMT formula in Excel/Sheets:\n\n" +
                    "Formula: $formula\n\n" +
                    "• 5%/12: Monthly interest rate (annual rate divided by 12).\n" +
                    "• 360: Number of monthly payments (30 years * 12).\n" +
                    "• -350000: Loan amount (negative to return positive payment)."
            return OfflineResult(reply, formula)
        }

        if (q.contains("datedif") || q.contains("age") || q.contains("date difference") || q.contains("days between")) {
            val formula = "=DATEDIF(A2, TODAY(), \"Y\")"
            val reply = "Here is the DATEDIF formula for calculating years or days between dates:\n\n" +
                    "Formula: $formula\n\n" +
                    "• A2: Start date.\n" +
                    "• TODAY(): Current date.\n" +
                    "• \"Y\" for complete years, \"M\" for months, or \"D\" for days."
            return OfflineResult(reply, formula)
        }

        if (q.contains("average") || q.contains("mean")) {
            val formula = "=AVERAGE(B2:B50)"
            val reply = "Here is the AVERAGE formula:\n\n" +
                    "Formula: $formula\n\n" +
                    "• Calculates the arithmetic mean across range B2 through B50."
            return OfflineResult(reply, formula)
        }

        if (q.contains("sum") || q.contains("total")) {
            val formula = "=SUM(B2:B20)"
            val reply = "Here is the SUM formula:\n\n" +
                    "Formula: $formula\n\n" +
                    "• Automatically calculates the sum across cells B2 through B20."
            return OfflineResult(reply, formula)
        }

        // 2. Everyday Work Email Drafting
        if (q.contains("email") || q.contains("draft") || q.contains("follow up") || q.contains("reply")) {
            val reply = "Subject: Quick Follow-up: Project Deliverables & Next Steps\n\n" +
                    "Hi Team,\n\n" +
                    "Hope you're having a productive week. I'm following up on our recent discussion regarding the upcoming milestones.\n\n" +
                    "Could you please share a quick status update on your respective deliverables by Friday EOD? Let me know if there are any roadblocks or if you need additional resources.\n\n" +
                    "Looking forward to hearing from you.\n\n" +
                    "Best regards,\n" +
                    "[Your Name]\nArc 360 Enterprise"
            return OfflineResult(reply)
        }

        // 3. Summarization & Meeting Notes
        if (q.contains("meeting") || q.contains("notes") || q.contains("agenda")) {
            val reply = "📋 Executive Meeting Agenda & Action Items\n\n" +
                    "1. Objective: Q3 Strategic Review & Resource Allocation\n" +
                    "2. Key Discussion Topics:\n" +
                    "   • Current sprint velocity and deliverable deadlines\n" +
                    "   • Budget review and enterprise cloud quotas\n" +
                    "   • Risk mitigation for upcoming client releases\n" +
                    "3. Action Items:\n" +
                    "   • [ ] Finalize spreadsheet forecasts (Assigned: Lead Analyst)\n" +
                    "   • [ ] Submit revised statement of work by Tuesday\n" +
                    "   • [ ] Schedule technical review sync for next Thursday"
            return OfflineResult(reply)
        }

        // General work assistant response
        val reply = "💡 Arc 360 Work & Formula Assistant (Offline Local Mode)\n\n" +
                "I can help you with:\n" +
                "1. Excel / Sheets Formulas: XLOOKUP, VLOOKUP, INDEX+MATCH, SUMIFS, COUNTIF, IFERROR, CAGR, PMT, DATEDIF.\n" +
                "2. Everyday Work: Drafting professional emails, meeting summaries, agendas, and report polishing.\n" +
                "3. Offline Room Continuity: All your documents and emails are automatically cached in Room SQLite and sync to cloud when online.\n\n" +
                "Try asking: 'Write an XLOOKUP formula for product prices' or 'Draft a follow-up email about deadlines'."
        return OfflineResult(reply)
    }
}
