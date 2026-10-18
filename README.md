# Sahaaya (సహాయ / सहाय)
### Frontline Health-Worker Clinical Companion for Low-Connectivity Areas

[![Talent AP Mega Hackathon 2026](https://img.shields.io/badge/Talent%20AP%20Mega%20Hackathon%202026-Problem%20H5-0288D1?style=for-the-badge)](https://talentap.in/2026/MegaHackathon-Oct26/)
[![Team](https://img.shields.io/badge/Team-Vizag%20Mavericks%20%5BTEAM--001245%5D-1F4E8C?style=for-the-badge)](#team-vizag-mavericks)
[![Offline First](https://img.shields.io/badge/Network-100%25%20Offline%20(Airplane%20Mode)-2E7D32?style=for-the-badge)](#measured-hardware-benchmarks)
[![Danger Recall](https://img.shields.io/badge/Safety-100%25%20Danger%20Recall-D32F2F?style=for-the-badge)](#3-tier-safety-first-architecture)
[![Response Latency](https://img.shields.io/badge/Median%20Latency-0.53%20ms-00796B?style=for-the-badge)](#measured-hardware-benchmarks)

---

## 📌 Overview

**Sahaaya** is a clinical offline companion engineered specifically for Accredited Social Health Activists (**ASHA**) and Auxiliary Nurse Midwives (**ANM**) operating in rural, low-connectivity, and zero-reception areas across India.

Built for the **[Talent AP Mega Hackathon 2026](https://talentap.in/2026/MegaHackathon-Oct26/)** under **Problem H5: Frontline Health-Worker Companion for Low-Connectivity Areas**, Sahaaya replaces cumbersome paper flipbooks, complex danger checklists, and multi-app fatigue with a unified, high-speed, zero-cloud assistant that enforces strict clinical safety protocols.

> **Field Reality:** India has over **10.29 lakh active ASHAs** serving population norms of ~1 per 1,000 citizens. In Andhra Pradesh alone, over **1,700 remote villages have zero cellular reception**, while obstetric haemorrhage remains the leading cause of maternal mortality (47%). Sahaaya ensures frontline workers are never cut off from verified government clinical guidance.

---

## 🛡️ 3-Tier Safety-First Architecture

Unlike generic cloud LLM chatbots that require constant 4G connectivity and risk hallucinating lethal drug dosages, Sahaaya employs a deterministic, zero-hallucination paramedical defense pipeline:

```
[ Frontline Worker Input (Voice / Multilingual Text) ]
                        │
                        ▼
┌──────────────────────────────────────────────────────────────┐
│  LAYER 1: DETERMINISTIC DANGER RULE ENGINE                   │
│  • Hard-coded regex & 367 multilingual tokens (EN/HI/TE)     │
│  • 100% Zero-Delay Escalation (Fires BEFORE any retrieval)   │
└───────────────────────┬──────────────────────────────────────┘
                        │
            ┌───────────┴───────────┐
      [Danger Detected]       [No Danger]
            │                       │
            ▼                       ▼
┌───────────────────────┐ ┌────────────────────────────────────┐
│ 🚨 RED REFER NOW CARD │ │  LAYER 2: INTENT SAFETY GATE       │
│ • MoHFW Section Cited │ │  • Detects Dosage / Antibiotics    │
│ • 1-Tap 108 Emergency │ │  • Enforces ASHA Non-Prescribing   │
│   Referral Slip (SMS) │ └─────────────────┬──────────────────┘
└───────────────────────┘                   │
                                ┌───────────┴───────────┐
                         [Out of Scope]            [In Scope]
                                │                       │
                                ▼                       ▼
                     ┌─────────────────────┐ ┌─────────────────────────┐
                     │ ⚠️ PHYSICIAN REDIRECT│ │ LAYER 3: KOTLIN BM25    │
                     │ Strict Refusal to   │ │ • Synonym Expansion     │
                     │ Prevent Malpractice │ │ • Cited Protocol Answer │
                     └─────────────────────┘ │ • 0% Hallucination      │
                                             └─────────────────────────┘
```

1. **Layer 1: Hard-Coded Danger Rule Engine:** Scans queries across English, Hinglish, and Telugu (Latin script). Covers 27 maternal, newborn, and child danger signs with **100% recall on 367 phrasings**.
2. **Layer 2: Intent Safety Gate:** Intercepts out-of-scope clinical prescription and dosage questions (e.g., antibiotic syrups) and returns an immediate physician redirect.
3. **Layer 3: Pure Kotlin BM25 Retriever:** Indexes official government protocols (**MoHFW Revised MCP Card**, **ASHA Induction Module**, **HBNC/HBYC Job Aids**, **HP STOP Diarrhoea Guidance**) and returns exact protocol sections with document and page citations.

---

## ✨ Key Features & Actionable Workflows

| Feature | Description | Frontline Value |
| :--- | :--- | :--- |
| **🚨 1-Tap Emergency Referral Slip** | Generates an official, pre-filled emergency referral ticket citing the MoHFW protocol, first-aid actions, and one-tap 108 ambulance SMS alert. | Eliminates referral delays during critical haemorrhage or newborn asphyxia emergencies. |
| **📋 1-Tap Daily PHC Summary (2G SMS Sync)** | In one tap, compiles completed visits, pending overdue follow-ups, and flagged emergency cases into an offline digest transmissible via basic 2G SMS. | Eliminates double-entry fatigue between paper registers and state portals. |
| **🎙️ Offline Voice-to-Text Input** | Native speech recognition supporting English, Hindi, and Telugu spoken queries directly in the field. | Enables hands-free clinical lookups while workers walk between village households. |
| **🌐 Trilingual Localization** | Instant runtime switcher between **English**, **Hindi (हिन्दी)**, and **Telugu (తెలుగు)** with daylight-readable High-Contrast Dark & Light themes. | Fully accessible to paramedical workers in regional vernacular. |
| **📅 Prioritized Household Triage** | Daily visit list engine sorting 30+ family records strictly by clinical urgency: **1. Danger Flags ➔ 2. Overdue Follow-ups ➔ 3. Scheduled Routine Care**. | Ensures high-risk pregnant mothers and unimmunized infants receive immediate priority. |
| **⚡ On-Device Benchmark Harness** | Built-in speedometer evaluation suite that runs a 30-question diagnostic test on live hardware in Airplane Mode. | Provides verifiable latency, heap footprint, and accuracy metrics directly on screen. |

---

## 📱 App Preview & Architecture

<p align="center">
  <img src="docs/app_mockup.png" alt="Sahaaya App Mockup" width="92%" />
</p>

<p align="center">
  <img src="docs/architecture_diagram.png" alt="Sahaaya Architecture Diagram" width="92%" />
</p>

---

## 📊 Measured Hardware Benchmarks

Measured on a physical **OnePlus (CPH2613)** device in **Airplane Mode** (zero Wi-Fi, zero cellular data):

| Metric | Measured Value | Field Impact |
| :--- | :--- | :--- |
| **Median CPU Response Latency** | **0.53 ms** (531 µs) | Instant response; zero network spinner or cloud wait time |
| **P95 CPU Response Latency** | **5.38 ms** | Sub-10ms response even on code-mixed 30-word queries |
| **Heap Memory Footprint** | **~901 KB** (< 1 MB) | Runs reliably on entry-level Android phones with 2GB–4GB RAM |
| **Standalone APK Size** | **16.9 MB** (Debug APK) | Lightweight; shareable peer-to-peer via Bluetooth or Nearby Share |
| **Network Permissions** | **0** (`INTERNET` omitted) | Zero battery drain from background sync; zero data pack cost |
| **Danger Sign Recall** | **100.0%** (367 / 367) | Verified on English, Hinglish, and Telugu (Latin) phrasings |
| **Out-of-Scope Refusal Rate** | **100.0%** (6 / 6) | Zero prescription leakage; complete protection against malpractice |
| **Protocol Citation Rate** | **100.0%** (14 / 14) | Every clinical answer cites Section ID, Document Name, and Page |

---

## 📂 Project Structure

```
Sahaaya/
├── android-app/                       # Native Android Studio Project
│   ├── app/src/main/java/com/sahaaya/companion/
│   │   ├── core/                      # Pure Kotlin domain logic (0 Android dependencies)
│   │   │   ├── DangerRuleEngine.kt    # Deterministic 27-rule multilingual matcher
│   │   │   ├── IntentGate.kt          # Dosage & prescription out-of-scope blocker
│   │   │   ├── Bm25Retriever.kt       # Pure Kotlin BM25 ranking algorithm
│   │   │   ├── VisitListEngine.kt     # Household register triage ranker
│   │   │   └── SahaayaPipeline.kt     # Orchestrated 3-tier clinical pipeline
│   │   └── ui/                        # Jetpack Compose UI & Localization
│   │       ├── MainActivity.kt        # App container & theme/language bar
│   │       ├── AskScreen.kt           # Search, voice mic, emergency alert cards
│   │       ├── VisitsScreen.kt        # Prioritized household visit cards & triage stats
│   │       ├── ReferralSlipDialog.kt  # 1-Tap 108 Emergency Referral Slip
│   │       ├── DailySummaryDialog.kt  # 1-Tap End-of-Day PHC Summary (2G SMS)
│   │       ├── BenchmarkScreen.kt     # Live on-device latency & memory harness
│   │       ├── Theme.kt               # High-contrast Light & Dark theme tokens
│   │       └── i18n/AppLanguage.kt    # English, Hindi, Telugu strings & prompts
├── assets/
│   ├── protocol_chunks.json           # 10 verified MoHFW/NHM protocol chunks with citations
│   ├── danger_rules.json              # 27 danger rules with 367 multilingual phrasings
│   └── register.csv                   # Synthetic 30-family household register
├── data/
│   └── test_questions.csv             # 30-item evaluation suite (escalations, refusals, code-mix)
├── docs/
│   ├── architecture_diagram.png       # Complete system architecture diagram
│   ├── app_mockup.png                 # Trilingual dual-screen interface mockup
│   ├── verification_table.md          # Fact-by-fact clinical verification mapping
│   └── BUILD.md                       # Comprehensive compilation & execution guide
├── scripts/
│   ├── build_submission_pdf.py        # Official Round 1 template filling script
│   └── generate_diagrams.py           # Matplotlib architecture & mockup generator
├── Round1_Submission_Filled.pdf       # Verified official Round 1 submission presentation
└── Problem-Statement-Plan.md          # Comprehensive clinical research brief
```

---

## 🚀 Quick Start & Build Instructions

### Prerequisites
- **JDK 17 LTS**
- **Android SDK (API 34 / 35)**
- **Gradle 8.14** (bundled via `./android-app/gradlew.bat`)

### 1. Run Pure JVM Standalone Tests (No Emulator or Device Needed)
The core logic has zero Android framework imports and can be verified on any standard JVM in milliseconds:

```powershell
# Set Java 17 environment
$env:JAVA_HOME = "C:\Users\yoges\develop\jdk-17\jdk-17.0.20.1+1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Run precompiled test runner
java -jar test-runner.jar
```
*Output: 15 / 15 tests passed (100% success rate on danger signs, scope refusals, BM25 retrieval, and visit prioritization).*

### 2. Build the Android APK
```powershell
cd android-app
.\gradlew.bat assembleDebug
```
*Generated package: `android-app/app/build/outputs/apk/debug/app-debug.apk`.*

### 3. Install via ADB onto a Physical Phone
```powershell
adb install -r android-app/app/build/outputs/apk/debug/app-debug.apk
```

For detailed instructions, refer to [`docs/BUILD.md`](docs/BUILD.md).

---

## 👥 Team: Vizag Mavericks

Submitted for **[Talent AP Mega Hackathon 2026](https://talentap.in/2026/MegaHackathon-Oct26/)**  
**Track:** Problem H5 — Frontline Health-Worker Companion for Low-Connectivity Areas  
**Team Code:** `TEAM-001245`

| Member | Role | Focus Area |
| :--- | :--- | :--- |
| **Amara Yogeswararao** | **Team Lead** | Architecture, Offline Retrieval Pipeline & Android System Design |
| **Seeramreddi Praveen** | Team Member | Clinical Protocols, Danger Rules & Multilingual Mapping |
| **K Lalith** | Team Member | Visit-List Triage Engine, Synthesis & Household Registers |
| **Harish Naidu BN** | Team Member | UI/UX Design, Trilingual Localization & Benchmark Validation |

---

## 📜 Clinical Disclaimer & Scope Assurance

Sahaaya is an offline decision-support companion engineered for Accredited Social Health Activists (ASHA) and Auxiliary Nurse Midwives (ANM). It is **not** a clinical diagnostic or prescribing tool. All recommendations are strictly cited from published training manuals of the **Ministry of Health and Family Welfare (MoHFW)** and the **National Health Mission (NHM)**, Government of India. The application hard-refuses prescription drug and antibiotic dosages, strictly enforcing that clinical prescription authority remains solely with qualified MBBS Medical Officers.

---

## 📄 License

Distributed under the **MIT License**. See `LICENSE` for details.
