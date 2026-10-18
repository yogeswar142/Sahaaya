import pymupdf
import os

def build_pdf():
    src_pdf = "Round1_Template submission.pdf"
    out_pdf = "Round1_Submission_Filled.pdf"
    
    doc = pymupdf.open(src_pdf)
    print(f"Loaded {src_pdf} with {len(doc)} pages.")

    # Colors
    c_white = (1, 1, 1)
    c_slide1_bg = (0.1216, 0.2745, 0.5294) # Exact background RGB of slide 1 (31, 70, 135)
    c_dark_blue = (0.1216, 0.3059, 0.5490) # #1f4e8c
    c_text_dark = (0.15, 0.15, 0.15)
    c_green = (0.18, 0.49, 0.20)

    def draw_bullet_block(page, x, y, max_w, title, desc, title_color=c_dark_blue, font_size=8.4, line_height=10.4, spacing=3.0):
        page.insert_text((x, y), title, fontname="hebo", fontsize=font_size, color=title_color)
        y += line_height
        
        desc_x = x + 10
        desc_max_w = max_w - 10
        words = desc.strip().split()
        line = ""
        for w in words:
            test_line = (line + " " + w) if line else w
            if pymupdf.get_text_length(test_line, fontname="helv", fontsize=font_size) < desc_max_w:
                line = test_line
            else:
                if line:
                    page.insert_text((desc_x, y), line, fontname="helv", fontsize=font_size, color=c_text_dark)
                    y += line_height
                    line = w
                else:
                    page.insert_text((desc_x, y), test_line, fontname="helv", fontsize=font_size, color=c_text_dark)
                    y += line_height
                    line = ""
        if line:
            page.insert_text((desc_x, y), line, fontname="helv", fontsize=font_size, color=c_text_dark)
            y += line_height
        return y + spacing

    # -------------------------------------------------------------
    # PAGE 1: TITLE SLIDE
    # -------------------------------------------------------------
    p1 = doc[0]
    p1.add_redact_annot(pymupdf.Rect(75, 230, 320, 255), fill=c_slide1_bg)
    p1.add_redact_annot(pymupdf.Rect(400, 230, 640, 255), fill=c_slide1_bg)
    p1.add_redact_annot(pymupdf.Rect(75, 295, 395, 320), fill=c_slide1_bg)
    p1.add_redact_annot(pymupdf.Rect(400, 295, 640, 320), fill=c_slide1_bg)
    p1.apply_redactions()

    p1.insert_text((79.3, 246), "Vizag Mavericks (TEAM-001245)", fontsize=13, color=c_white, fontname="helv")
    p1.insert_text((403.6, 246), "Amara Yogeswararao", fontsize=13, color=c_white, fontname="helv")
    p1.insert_text((79.3, 310), "H5. Frontline Health-Worker Companion for Low-Connectivity Areas", fontsize=9.6, color=c_white, fontname="helv")
    p1.insert_text((403.6, 310), "18/10/2026", fontsize=13, color=c_white, fontname="helv")

    # -------------------------------------------------------------
    # PAGE 2: USE CASE (Problem Statement & Market Opportunity)
    # -------------------------------------------------------------
    p2 = doc[1]
    p2.add_redact_annot(pymupdf.Rect(45, 110, 700, 230), fill=c_white)
    p2.add_redact_annot(pymupdf.Rect(45, 260, 700, 395), fill=c_white)
    p2.apply_redactions()

    prob_bullets = [
        ("• Frontline Burden & Zero Cellular Connectivity:", "Over 10.29 lakh rural ASHA & ANM workers navigate 1,700+ remote AP villages with zero mobile reception, juggling bulky flipbooks and 20+ disconnected apps."),
        ("• Preventable Maternal & Neonatal Deaths:", "Obstetric haemorrhage causes 47% of maternal deaths; missed newborn danger signs (hypothermia, convulsions, chest indrawing) lead to fatal referral delays."),
        ("• Cloud AI Hallucinations & Malpractice Risks:", "Generic cloud LLMs hallucinate fatal antibiotic dosages, violate non-prescribing ASHA boundaries, require constant 4G data, and fail on low-end smartphones.")
    ]
    y = 122
    for title, desc in prob_bullets:
        y = draw_bullet_block(p2, 55, y, 615, title, desc, font_size=8.4, line_height=10.5, spacing=3.0)

    mkt_bullets = [
        ("• Frontline Scale & National Need:", "10.29 lakh ASHAs and ~2.5 lakh Sub-Centre ANMs across India serving 1,000 population norms, requiring an intuitive, reliable paramedical companion."),
        ("• Critical Gap in Existing Systems:", "State tools (ANMOL, mSakhi, Kilkari) suffer from cloud latency, sync failures, or lack deterministic emergency danger escalation and scope gating."),
        ("• Sahaaya High-Impact Value:", "100% offline, protocol-grounded companion: deterministic danger-sign escalation, doctor redirects, prioritized visits, emergency referral slips, and 2G SMS field sync.")
    ]
    y = 270
    for title, desc in mkt_bullets:
        y = draw_bullet_block(p2, 55, y, 615, title, desc, font_size=8.4, line_height=10.5, spacing=3.0)

    # -------------------------------------------------------------
    # PAGE 3: PROPOSED SOLUTION (Overview & Mockup Image)
    # -------------------------------------------------------------
    p3 = doc[2]
    p3.add_redact_annot(pymupdf.Rect(45, 110, 700, 205), fill=c_white)
    p3.add_redact_annot(pymupdf.Rect(36, 204, 684, 395), fill=c_white)
    p3.apply_redactions()

    sol_bullets = [
        ("• Deterministic Danger Escalation + Referral Slip:", "Scans queries across English, Hinglish & Telugu-Latin; triggers red screen (100% recall on 367 phrasings) + 1-tap 108 Emergency Referral Slip."),
        ("• Strict Non-Prescribing Scope Gate:", "Hard-blocks antibiotic & dosage questions with physician redirects, ensuring paramedical workers operate strictly within ASHA guidelines."),
        ("• Extractive BM25 Protocol Retrieval:", "Pure Kotlin BM25 matches questions to official MoHFW/NHM manuals with exact document & page citations (0.53 ms latency, 0% hallucination)."),
        ("• Prioritised Daily Household Action Plan & 2G Sync:", "Triage ranking: Danger Flags > Overdue Follow-ups > Routine Due Dates + 1-Tap End-of-Day PHC Summary Report (transmissible via 2G SMS).")
    ]
    y = 118
    for title, desc in sol_bullets:
        y = draw_bullet_block(p3, 55, y, 615, title, desc, font_size=7.8, line_height=9.8, spacing=2.0)

    mockup_img = "docs/app_mockup.png"
    if os.path.exists(mockup_img):
        p3.draw_rect(pymupdf.Rect(45, 204, 675, 395), fill=c_white, color=(0.82, 0.82, 0.82), width=1)
        p3.insert_image(pymupdf.Rect(48, 206, 672, 393), filename=mockup_img)

    # -------------------------------------------------------------
    # PAGE 4: DESIGN STEPS & ARCHITECTURE
    # -------------------------------------------------------------
    p4 = doc[3]
    p4.add_redact_annot(pymupdf.Rect(45, 110, 375, 395), fill=c_white)
    p4.add_redact_annot(pymupdf.Rect(380, 110, 690, 395), fill=c_white)
    p4.apply_redactions()

    steps = [
        ("1. Input & Offline Voice Recognition:", " Supports speech and text queries in English, Hindi & Telugu (Latin). Trilingual instant switching and sunlight-readable dark/light modes."),
        ("2. Layer 1: Deterministic Danger Rule Engine:", " Hard-coded regex & phrase matching across 27 danger signs (100% recall). Immediately generates Red Alert + 108 Emergency Referral Slip."),
        ("3. Layer 2: Intent Safety Gate:", " Blocks clinical prescription/antibiotic queries with fixed doctor redirect, protecting workers against medical malpractice."),
        ("4. Layer 3: Pure Kotlin BM25 Retriever:", " Tokenises with code-mixed synonym expansion, scores against grounded MoHFW chunks with confidence thresholding (0% hallucination)."),
        ("5. Dynamic Visit-List Engine & 2G Sync:", " Ranks 30+ family records by clinical urgency: Danger Flags -> Overdue -> Upcoming. 1-Tap generates daily PHC SMS field summaries.")
    ]
    y = 120
    for title, desc in steps:
        p4.insert_text((50, y), title, fontsize=8.6, fontname="hebo", color=c_dark_blue)
        y += 11.5
        words = desc.strip().split()
        line = ""
        for w in words:
            test_line = line + " " + w if line else w
            if pymupdf.get_text_length(test_line, fontname="helv", fontsize=8.0) < 320:
                line = test_line
            else:
                p4.insert_text((55, y), line, fontsize=8.0, fontname="helv", color=c_text_dark)
                y += 10.0
                line = w
        if line:
            p4.insert_text((55, y), line, fontsize=8.0, fontname="helv", color=c_text_dark)
            y += 12.5

    arch_img = "docs/architecture_diagram.png"
    if os.path.exists(arch_img):
        p4.draw_rect(pymupdf.Rect(385, 112, 680, 390), fill=c_white, color=(0.82, 0.82, 0.82), width=1)
        p4.insert_image(pymupdf.Rect(388, 115, 677, 387), filename=arch_img)

    # -------------------------------------------------------------
    # PAGE 5: CHALLENGES & MITIGATION
    # -------------------------------------------------------------
    p5 = doc[4]
    p5.add_redact_annot(pymupdf.Rect(45, 105, 700, 160), fill=c_white)
    p5.add_redact_annot(pymupdf.Rect(45, 190, 700, 245), fill=c_white)
    p5.add_redact_annot(pymupdf.Rect(45, 275, 700, 350), fill=c_white)
    p5.apply_redactions()

    def draw_challenge_block(page, y_start, prob_desc, mit_desc):
        page.insert_text((55, y_start), "Problem: ", fontname="hebo", fontsize=8.8, color=c_dark_blue)
        words = prob_desc.strip().split()
        line = ""
        y = y_start
        cur_x = 105
        for w in words:
            test = line + " " + w if line else w
            if cur_x + pymupdf.get_text_length(test, fontname="helv", fontsize=8.8) < 670:
                line = test
            else:
                page.insert_text((cur_x, y), line, fontname="helv", fontsize=8.8, color=c_text_dark)
                y += 11
                cur_x = 65
                line = w
        if line:
            page.insert_text((cur_x, y), line, fontname="helv", fontsize=8.8, color=c_text_dark)
            y += 13

        page.insert_text((55, y), "Mitigation: ", fontname="hebo", fontsize=8.8, color=c_green)
        cur_x = 112
        words = mit_desc.strip().split()
        line = ""
        for w in words:
            test = line + " " + w if line else w
            if cur_x + pymupdf.get_text_length(test, fontname="helv", fontsize=8.8) < 670:
                line = test
            else:
                page.insert_text((cur_x, y), line, fontname="helv", fontsize=8.8, color=c_text_dark)
                y += 11
                cur_x = 65
                line = w
        if line:
            page.insert_text((cur_x, y), line, fontname="helv", fontsize=8.8, color=c_text_dark)

    draw_challenge_block(
        p5, 118,
        "Zero mobile reception in rural hamlets and severe RAM/battery constraints on low-end ASHA smartphones.",
        "100% on-device architecture with ZERO network permissions. Measured APK size is 16.9 MB, median response latency is 0.53 ms, and peak heap RAM footprint is < 1 MB."
    )
    draw_challenge_block(
        p5, 204,
        "Medical hallucination risks and catastrophic delays from missed pregnancy and newborn danger signs.",
        "Deterministic danger rules execute BEFORE any retrieval logic (achieving 100% recall on 367 verified multilingual phrasings). Answers strictly cite section and page."
    )
    draw_challenge_block(
        p5, 290,
        "Code-mixed vernacular queries (English, Hinglish, Telugu in Latin script) with spelling variations and typos.",
        "Engineered robust phonetic tokenisation, code-mixed synonym mapping, offline voice mic, and 1-tap referral/daily sync. On-device SLM planned for finale using organizer models."
    )

    # -------------------------------------------------------------
    # PAGE 6: UNIQUE SELLING PROPOSITION (USP)
    # -------------------------------------------------------------
    p6 = doc[5]
    p6.add_redact_annot(pymupdf.Rect(45, 120, 255, 245), fill=c_white)
    p6.add_redact_annot(pymupdf.Rect(270, 120, 480, 245), fill=c_white)
    p6.add_redact_annot(pymupdf.Rect(495, 120, 705, 245), fill=c_white)
    p6.add_redact_annot(pymupdf.Rect(45, 290, 700, 395), fill=c_white)
    p6.apply_redactions()

    d1_lines = [
        "Deterministic Safety-First Layer:",
        "Hard-coded emergency rule engine",
        "fires BEFORE any AI logic, achieving",
        "100% recall (367/367 tested phrasings)",
        "with 0% hallucination + 1-tap 108",
        "Emergency Referral Slip generation."
    ]
    y = 135
    for l in d1_lines:
        p6.insert_text((55, y), l, fontsize=8.5, fontname="helv", color=c_text_dark)
        y += 13

    d2_lines = [
        "100% Offline & Measured Speed:",
        "Zero network permissions required.",
        "Measured on JVM & physical phone:",
        "• Median Latency: 0.53 ms",
        "• P95 Latency: 5.38 ms",
        "• APK Size: 16.9 MB | RAM: < 1 MB",
        "Runs natively in Airplane Mode."
    ]
    y = 135
    for l in d2_lines:
        p6.insert_text((280, y), l, fontsize=8.5, fontname="helv", color=c_text_dark)
        y += 13

    d3_lines = [
        "Prioritised Household Action Plan:",
        "Converts static paper registers into",
        "an automated daily workflow:",
        "1. URGENT: Danger Flag Cases",
        "2. OVERDUE: Ranked by days overdue",
        "3. UPCOMING: Nearest scheduled care",
        "Includes 1-Tap 2G SMS Daily Sync."
    ]
    y = 135
    for l in d3_lines:
        p6.insert_text((505, y), l, fontsize=8.5, fontname="helv", color=c_text_dark)
        y += 13

    adv_bullets = [
        ("• Clinically Grounded Trust:", "Unlike generic LLM chatbots that hallucinate dosages, Sahaaya hard-refuses prescriptions, cites official MoHFW protocols, and produces actionable 108 ambulance referral slips."),
        ("• Workflow Unification:", "Replaces fragmented flipbooks, complex checklists, and multi-app fatigue with a clean two-screen trilingual interface (EN/HI/TE) with offline voice input tailored for village health workers.")
    ]
    y = 300
    for title, desc in adv_bullets:
        y = draw_bullet_block(p6, 55, y, 615, title, desc, font_size=8.5, line_height=10.5, spacing=3.0)

    # -------------------------------------------------------------
    # PAGE 7: DEMO & POC VIDEO
    # -------------------------------------------------------------
    p7 = doc[6]
    p7.add_redact_annot(pymupdf.Rect(180, 345, 540, 385), fill=c_white)
    p7.apply_redactions()

    p7.insert_text((260, 362), "Demo video link: to be added", fontsize=11, fontname="hebo", color=c_dark_blue)
    poc_points = "Working Offline Android POC (app-debug.apk, 16.9 MB) | 100% Danger Recall (367 phrasings) | 0.53 ms Median Latency | Zero Cloud Permissions"
    p7.insert_text((48, 388), poc_points, fontsize=8, fontname="helv", color=(0.3, 0.3, 0.3))

    doc.save(out_pdf)
    doc.close()
    print(f"Successfully generated {out_pdf}.")

if __name__ == "__main__":
    build_pdf()
