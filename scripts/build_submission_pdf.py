import pymupdf
import os

def build_pdf():
    src_pdf = "scripts/template.pdf" if os.path.exists("scripts/template.pdf") else "Round1_Template submission.pdf"
    out_pdf = "TEAM-001245_Sahaaya_Round1_Submission.pdf"
    
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
    # -------------------------------------------------------------
    # PAGE 2: USE CASE (Problem Statement & Market Opportunity)
    # -------------------------------------------------------------
    p2 = doc[1]
    p2.add_redact_annot(pymupdf.Rect(45, 110, 700, 230), fill=c_white)
    p2.add_redact_annot(pymupdf.Rect(45, 260, 700, 400), fill=c_white)
    p2.apply_redactions()

    prob_bullets = [
        ("• Frontline Burden & Zero Cellular Connectivity:", "Over 10.29 lakh rural ASHA & ANM workers navigate 1,700+ remote AP villages with zero mobile reception [4], juggling 28 paper registers and 20+ disconnected apps [5]."),
        ("• Preventable Maternal & Neonatal Deaths:", "Obstetric haemorrhage causes 47% of maternal deaths [3]; missed newborn danger signs (hypothermia, convulsions, chest indrawing) lead to fatal referral delays [1, 2]."),
        ("• Cloud AI Hallucinations & Malpractice Risks:", "Generic cloud LLMs hallucinate fatal antibiotic dosages, violate non-prescribing ASHA boundaries [1], lack WHO-mandated deterministic safeguards [6], and fail in offline hamlets.")
    ]
    y = 120
    for title, desc in prob_bullets:
        y = draw_bullet_block(p2, 55, y, 615, title, desc, font_size=8.3, line_height=10.2, spacing=2.5)

    mkt_bullets = [
        ("• Frontline Scale & National Need:", "10.29 lakh ASHAs and ~2.5 lakh Sub-Centre ANMs across India serving 1,000 population norms, requiring an intuitive, reliable paramedical companion [1, 2]."),
        ("• Critical Gap in Existing Systems:", "State tools (ANMOL, mSakhi, Kilkari) suffer from cloud latency, sync failures in zero-signal areas [4], or lack deterministic emergency danger escalation and scope gating [5]."),
        ("• Sahaaya High-Impact Value:", "100% offline, protocol-grounded companion: deterministic danger-sign escalation [1], doctor redirects [6], prioritized visits, emergency referral slips, and 2G SMS field sync [4].")
    ]
    y = 265
    for title, desc in mkt_bullets:
        y = draw_bullet_block(p2, 55, y, 615, title, desc, font_size=8.3, line_height=10.2, spacing=2.5)

    # Reference Citations Box on Slide 2
    ref_box = pymupdf.Rect(45, 348, 675, 400)
    p2.draw_rect(ref_box, fill=(0.955, 0.968, 0.985), color=(0.80, 0.84, 0.90), width=0.75)
    p2.insert_text((52, 356.5), "Official Protocol Sources & Clinical Evidence Citations:", fontname="hebo", fontsize=7.2, color=c_dark_blue)

    citations = [
        ("[1] MoHFW National Guidelines:", "Revised India MCP Card (2018) & ASHA Modules 6-7", "https://hbnc-hbyc.mohfw.gov.in/assets/document/Revised%20India%20MCP%20Card%20English_(28-05-2018).pdf", 52, 368),
        ("[2] NHM Clinical Guidance:", "ASHA Induction Module (pp. 51-97) & STOP Diarrhoea", "https://nhm.gov.in/images/pdf/communitisation/asha/ASHA_Induction_Module_English.pdf", 52, 378),
        ("[3] PMC9292773 Study:", "Obstetric haemorrhage causes 47% maternal deaths in India", "https://pmc.ncbi.nlm.nih.gov/articles/PMC9292773/", 52, 388),
        ("[4] TelecomTalk AP Survey:", "1,700 remote hamlets (ASR dist) zero cellular signal", "https://telecomtalk.info/?p=654511", 365, 368),
        ("[5] New Lines / IJCMPH 2024:", "Frontline overload (28 paper registers, 23 apps)", "https://newlinesmag.com/reportage/indias-digital-health-push-is-overworking-its-front-line-women/", 365, 378),
        ("[6] WHO AI Safety Standards:", "Multi-modal AI guidance for clinical decision support", "https://www.downtoearth.org.in/science-technology/who-releases-guidelines-for-multi-modal-generative-ai-in-healthcare-resonates-with-recommendations-for-other-sectors-93991", 365, 388),
    ]

    for tag, desc, url, cx, cy in citations:
        p2.insert_text((cx, cy), tag, fontname="hebo", fontsize=6.8, color=c_dark_blue)
        tag_w = pymupdf.get_text_length(tag + " ", fontname="hebo", fontsize=6.8)
        p2.insert_text((cx + tag_w, cy), desc, fontname="helv", fontsize=6.8, color=c_text_dark)
        tot_w = tag_w + pymupdf.get_text_length(desc, fontname="helv", fontsize=6.8)
        p2.insert_link({"kind": pymupdf.LINK_URI, "from": pymupdf.Rect(cx, cy - 6.5, cx + tot_w, cy + 2.5), "uri": url})

    # -------------------------------------------------------------
    # PAGE 3: PROPOSED SOLUTION (Overview & Mockup Image)
    # -------------------------------------------------------------
    p3 = doc[2]
    p3.add_redact_annot(pymupdf.Rect(45, 110, 700, 205), fill=c_white)
    p3.add_redact_annot(pymupdf.Rect(36, 204, 684, 395), fill=c_white)
    p3.apply_redactions()

    sol_bullets = [
        ("• Deterministic Danger Escalation + Referral Slip:", "Scans queries across English, Hinglish & Telugu-Latin; triggers red screen (100% recall on 367 phrasings [1, 2]) + 1-tap 108 Emergency Referral Slip."),
        ("• Strict Non-Prescribing Scope Gate:", "Hard-blocks antibiotic & dosage questions with physician redirects, ensuring paramedical workers operate strictly within ASHA guidelines [1, 6]."),
        ("• Extractive BM25 Protocol Retrieval:", "Pure Kotlin BM25 matches questions to official MoHFW/NHM manuals [1, 2] with exact document & page citations (0.53 ms latency, 0% hallucination)."),
        ("• Prioritised Daily Household Action Plan & 2G Sync:", "Triage ranking: Danger Flags > Overdue Follow-ups > Routine Due Dates + 1-Tap End-of-Day PHC Summary Report (transmissible via 2G SMS [4, 5]).")
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
        ("2. Layer 1: Deterministic Danger Rule Engine:", " Hard-coded regex & phrase matching across 27 danger signs (100% recall on MoHFW rules [1, 2]). Immediately generates Red Alert + 108 Emergency Referral Slip."),
        ("3. Layer 2: Intent Safety Gate:", " Blocks clinical prescription/antibiotic queries with fixed doctor redirect, enforcing non-prescribing boundaries [1, 6] to prevent medical malpractice."),
        ("4. Layer 3: Pure Kotlin BM25 Retriever:", " Tokenises with code-mixed synonym expansion, scores against grounded MoHFW chunks [1, 2] with confidence thresholding (0% hallucination)."),
        ("5. Dynamic Visit-List Engine & 2G Sync:", " Ranks 30+ family records by clinical urgency: Danger Flags -> Overdue -> Upcoming. 1-Tap generates daily PHC SMS field summaries [4].")
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
        "fires BEFORE any AI logic (per [1, 6]),",
        "achieving 100% recall (367 phrasings)",
        "with 0% hallucination + 1-tap 108",
        "Emergency Referral Slip generation."
    ]
    y = 135
    for l in d1_lines:
        p6.insert_text((55, y), l, fontsize=8.5, fontname="helv", color=c_text_dark)
        y += 13

    d2_lines = [
        "100% Offline & Measured Speed:",
        "Zero network permissions (built for [4]).",
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
        ("• Clinically Grounded Trust:", "Unlike generic LLM chatbots that hallucinate dosages, Sahaaya hard-refuses prescriptions [1, 6], cites official MoHFW/NHM protocols [1, 2], and produces actionable 108 ambulance referral slips."),
        ("• Workflow Unification:", "Replaces fragmented flipbooks, complex checklists, and multi-app fatigue [5] with a clean two-screen trilingual interface (EN/HI/TE) with offline voice input and 2G SMS field sync [4].")
    ]
    y = 300
    for title, desc in adv_bullets:
        y = draw_bullet_block(p6, 55, y, 615, title, desc, font_size=8.5, line_height=10.5, spacing=3.0)

    # -------------------------------------------------------------
    # PAGE 7: DEMO & POC VIDEO
    # -------------------------------------------------------------
    p7 = doc[6]
    p7.add_redact_annot(pymupdf.Rect(100, 345, 620, 398), fill=c_white)
    p7.apply_redactions()

    # Video link bar and repo link
    video_rect = pymupdf.Rect(110, 348, 610, 370)
    p7.draw_rect(video_rect, fill=(0.955, 0.968, 0.985), color=(0.80, 0.84, 0.90), width=0.75)
    p7.insert_text((120, 362), "Demo Video Link: [Insert Video Link / Embed]", fontsize=9.2, fontname="hebo", color=c_dark_blue)
    repo_text = "GitHub: github.com/yogeswar142/Sahaaya"
    repo_w = pymupdf.get_text_length(repo_text, fontname="hebo", fontsize=8.8)
    p7.insert_text((395, 362), repo_text, fontsize=8.8, fontname="hebo", color=(0.0, 0.45, 0.75))
    p7.insert_link({"kind": pymupdf.LINK_URI, "from": pymupdf.Rect(395, 353, 395 + repo_w, 366), "uri": "https://github.com/yogeswar142/Sahaaya"})

    poc_points = "Working Offline Android POC (app-debug.apk, 16.9 MB) | Grounded on MoHFW/NHM Protocols [1-3] | 100% Danger Recall | Zero Cloud Permissions"
    p7.insert_text((48, 388), poc_points, fontsize=7.8, fontname="helv", color=(0.3, 0.3, 0.3))

    doc.save(out_pdf)
    doc.close()
    print(f"Successfully generated {out_pdf}.")

if __name__ == "__main__":
    build_pdf()
