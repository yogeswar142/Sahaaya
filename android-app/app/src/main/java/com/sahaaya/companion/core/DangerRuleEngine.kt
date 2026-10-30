package com.sahaaya.companion.core

import java.util.regex.Pattern

class DangerRuleEngine(private val rules: List<DangerRule>) {

    private val devanagariMap = mapOf(
        "बुखार" to "bukhar", "दस्त" to "dast", "उल्टी" to "ulti", "खून" to "khoon",
        "झटके" to "jhatke", "बेहोश" to "behosh", "सांस" to "saans", "पसली" to "pasli",
        "ठंडा" to "thanda", "पीला" to "peela", "पीली" to "peela yellow", "पीले" to "peela yellow",
        "सफेद" to "safed white", "दूध" to "doodh", "सूजन" to "sujan",
        "टीका" to "tika", "गर्भवती" to "garbhvati", "प्रसव" to "prasav", "बच्चा" to "bachcha",
        "दर्द" to "dard", "दवा" to "dawai", "गोली" to "goli", "पहला" to "pehla", "माँ" to "maa",
        "छाती" to "chest seene", "खींच" to "indrawing pasli", "धुंधला" to "blurred vision",
        "सिर" to "sir", "हथेली" to "palms", "हथेलियां" to "palms", "हथेलियों" to "palms", "तलवे" to "soles"
    )

    private val teluguMap = mapOf(
        "బిడ్డ" to "baby bachcha child", "పిల్లలు" to "child baby bachcha",
        "చల్లగా" to "thanda cold hypothermia", "పాలు" to "doodh breastfeed",
        "తాగట్లేదు" to "cannot suck feed doodh nahi", "శ్వాస" to "saans breathing",
        "రక్తం" to "khoon bleeding bleed", "విరోచనాలు" to "dast diarrhoea",
        "జ్వరం" to "fever bukhar", "మూర్ఛ" to "jhatke convulsions fits",
        "స్పృహ" to "unconscious behosh", "వాంతులు" to "vomit ulti",
        "తల్లి" to "mother maa", "గర్భవతి" to "pregnant garbhvati",
        "టీకా" to "tika vaccine", "ఆసుపత్రి" to "hospital",
        "కదలట్లేదు" to "movement band nahi hil raha",
        "కేజీ" to "kg", "కిలో" to "kg", "బరువు" to "weight wajan",
        "నెలలు" to "months mahina", "కాన్పు" to "delivery prasav"
    )

    fun normalize(text: String): String {
        var t = text.lowercase()
        for ((tel, rom) in teluguMap) {
            t = t.replace(tel, " $rom ")
        }
        for ((dev, rom) in devanagariMap) {
            t = t.replace(dev, " $rom ")
        }
        t = t.replace(Regex("[^a-z0-9\\s\\.\\-/]"), " ")
        return t.replace(Regex("\\s+"), " ").trim()
    }

    fun evaluate(query: String): DangerMatchResult {
        val norm = normalize(query)
        if (norm.isBlank()) return DangerMatchResult(matched = false)

        // Near-boundary procedural filter: routine screening questions are NOT emergencies
        if (norm.contains("kaise check") || norm.contains("kaise naap") || norm.contains("screening chal rahi")) {
            return DangerMatchResult(matched = false)
        }

        // 1. OBSTETRIC / PREGNANCY & POSTPARTUM EMERGENCIES (ESC-2 & ESC-3)
        // Bleeding in pregnancy/delivery (ESC-2.1)
        if ((norm.contains("khoon") || norm.contains("bleed") || norm.contains("rakt")) &&
            (norm.contains("pregnant") || norm.contains("garbh") || norm.contains("delivery") ||
             norm.contains("prasav") || norm.contains("mahin") || norm.contains("pet"))
        ) {
            return createResult("ESC-2.1", "Vaginal bleeding during pregnancy/delivery", "Bleeding reported", "REFER NOW to FRU/Hospital")
        }

        // Postpartum heavy bleeding / PPH (ESC-3.1)
        if (norm.contains("pad") && (norm.contains("bhar") || norm.contains("soak") || norm.contains("minute") || norm.contains("din me") || norm.contains("3-4"))) {
            return createResult("ESC-3.1", "Postpartum haemorrhage (PPH)", "Heavy pad soaking", "REFER IMMEDIATELY (Call 108)")
        }

        // Pre-eclampsia signs in pregnancy (ESC-2.3)
        if ((norm.contains("pregnant") || norm.contains("garbh") || norm.contains("mahin") || norm.contains("months")) &&
            (((norm.contains("chehra") || norm.contains("face") || norm.contains("munh")) && (norm.contains("suja") || norm.contains("phool") || norm.contains("swell"))) ||
             (norm.contains("haath") && (norm.contains("phool") || norm.contains("sujan") || norm.contains("suja"))) ||
             ((norm.contains("dhundhla") || norm.contains("blur")) && (norm.contains("sir dard") || norm.contains("phool") || norm.contains("suja"))))
        ) {
            return createResult("ESC-2.3", "Pre-eclampsia (severe headache, facial/hand swelling, blurred vision)", "Pre-eclampsia signs", "REFER URGENTLY to CHC/FRU")
        }

        // Severe anaemia with breathlessness / pallor (ESC-2.5)
        if ((norm.contains("peeli") || norm.contains("peela") || norm.contains("pallor")) &&
            (norm.contains("saans phool") || norm.contains("saans lene") || norm.contains("breathless") || norm.contains("chalne pe saans"))
        ) {
            return createResult("ESC-2.5", "Severe anaemia with breathlessness", "Severe pallor with respiratory distress", "URGENT REFERRAL to FRU")
        }

        // Fits / Convulsions in pregnancy or postpartum (ESC-2.2 & ESC-3.3)
        if ((norm.contains("pregnant") || norm.contains("garbh") || norm.contains("delivery")) &&
            (norm.contains("jhatke") || norm.contains("fits") || norm.contains("daura") || norm.contains("mirgi") || norm.contains("behosh"))
        ) {
            return createResult("ESC-2.2", "Eclampsia / convulsions in pregnancy", "Maternal convulsions/fits", "CRITICAL: REFER NOW to FRU")
        }

        // Decreased or absent foetal movements (ESC-2.7)
        if ((norm.contains("bachcha") || norm.contains("baby") || norm.contains("pet me") || norm.contains("foetal")) &&
            (norm.contains("hil nahi") || (norm.contains("movement") && (norm.contains("kam") || norm.contains("band") || norm.contains("nahi"))))
        ) {
            return createResult("ESC-2.7", "Reduced or absent foetal movement", "Foetal distress", "REFER TO CHC/FRU")
        }

        // Leaking water bag / foul lochia (ESC-2.8)
        if (norm.contains("thaili") || norm.contains("water bag") || norm.contains("paani choot") || norm.contains("leaking")) {
            return createResult("ESC-2.8", "Premature rupture of membranes / leaking water bag", "Rupture of membranes", "REFER TO FRU")
        }
        if ((norm.contains("badbu") || norm.contains("foul")) && (norm.contains("discharge") || norm.contains("srav"))) {
            return createResult("ESC-2.8", "Foul smelling lochia / discharge", "Puerperal infection", "REFER TO PHC/CHC")
        }

        // Severe pain or hyperemesis (ESC-2.6)
        if ((norm.contains("pregnant") || norm.contains("garbh") || norm.contains("mahin")) &&
            ((norm.contains("paani") && (norm.contains("nahi tik") || norm.contains("pet me nahi"))) ||
             norm.contains("din bhar ulti") ||
             (norm.contains("severe") && norm.contains("abdominal pain")) ||
             (norm.contains("pet me") && norm.contains("tez dard")))
        ) {
            return createResult("ESC-2.6", "Hyperemesis gravidarum / severe abdominal pain", "Severe dehydration/pain", "REFER TO CHC/FRU")
        }

        // High fever in pregnancy or delivery (ESC-2.4 & ESC-3.2)
        if ((norm.contains("pregnant") || norm.contains("delivery ke baad") || norm.contains("delivery ke 10 din")) &&
            (norm.contains("tez bukhar") || norm.contains("high fever") || (norm.contains("38") && norm.contains("fever")))
        ) {
            val leaf = if (norm.contains("delivery")) "ESC-3.2" else "ESC-2.4"
            return createResult(leaf, "Puerperal / maternal high fever", "High fever with chills", "REFER TO PHC/FRU")
        }

        // 2. NEWBORN & YOUNG INFANT < 2 MONTHS (ESC-4)
        // Hypothermia: cold to touch (ESC-4.3)
        if ((norm.contains("bachcha") || norm.contains("baby") || norm.contains("navjaat") || norm.contains("din") || norm.contains("shishu")) &&
            (norm.contains("thanda") || norm.contains("cold to touch") || norm.contains("sheetal"))
        ) {
            return createResult("ESC-4.3", "Neonatal hypothermia (cold to touch)", "Hypothermia < 35.4 C", "WARM BABY (KMC) & REFER TO SNCU")
        }

        // Fast breathing / chest indrawing (ESC-4.2 & ESC-5.2)
        val hasIndrawingNeg = norm.contains("no chest indrawing") || norm.contains("no indrawing") || norm.contains("pasli nahi") || norm.contains("without indrawing")
        if ((norm.contains("pasli") || norm.contains("indrawing") || norm.contains("dhans")) && !hasIndrawingNeg) {
            val leaf = if (norm.contains("navjaat") || norm.contains("newborn") || norm.contains("din ka")) "ESC-4.2" else "ESC-5.2"
            return createResult(leaf, "Severe pneumonia / chest indrawing", "Severe chest indrawing", "REFER URGENTLY TO FRU/HOSPITAL")
        }
        if ((norm.contains("saans") && (norm.contains("66") || norm.contains("60") || norm.contains("68") || norm.contains("70")))) {
            return createResult("ESC-4.2", "Neonatal fast breathing (>= 60 breaths/min)", "Tachypnea", "REFER TO SNCU")
        }

        // Inability to suck / breastfeed, limp limbs (ESC-4.1)
        if ((norm.contains("bachcha") || norm.contains("baby") || norm.contains("navjaat") || norm.contains("mahin")) &&
            (norm.contains("doodh nahi pee") || norm.contains("doodh chhod") || norm.contains("cannot suck") ||
             norm.contains("unable to feed") || norm.contains("suck nahi") || norm.contains("kuch nahi pee") ||
             norm.contains("dhila pad gaya") || norm.contains("limp") || norm.contains("rona band"))
        ) {
            val leaf = if (norm.contains("navjaat") || norm.contains("din ka")) "ESC-4.1" else "ESC-5.1"
            return createResult(leaf, "Unable to feed / limp / weak cry", "Severe illness / sepsis sign", "CRITICAL REFERRAL TO SNCU/NBSU")
        }

        // VLBW < 1.8 kg (ESC-4.4)
        val kgMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*kg").matcher(norm)
        if (kgMatcher.find() && (norm.contains("bachcha") || norm.contains("baby") || norm.contains("paida") || norm.contains("wajan") || norm.contains("weight") || norm.contains("baruvu"))) {
            val wt = kgMatcher.group(1)?.toDoubleOrNull() ?: 2.5
            if (wt < 1.8) {
                return createResult("ESC-4.4", "Very Low Birth Weight ($wt kg < 1.8 kg)", "VLBW requiring specialized thermal care", "REFER TO SNCU")
            }
        }

        // Umbilical redness/pus or yellow palms/soles (ESC-4.4)
        if ((norm.contains("naal") || norm.contains("cord") || norm.contains("umbilicus") || norm.contains("boddu")) &&
            (norm.contains("peep") || norm.contains("pus") || norm.contains("laal") || norm.contains("khoon") || norm.contains("cheemu"))
        ) {
            return createResult("ESC-4.4", "Omphalitis / umbilical cord infection", "Cord discharge/redness", "REFER TO PHC/CHC")
        }
        if ((norm.contains("hatheli") || norm.contains("talwe") || norm.contains("palms")) &&
            (norm.contains("peel") || norm.contains("yellow") || norm.contains("safed"))
        ) {
            return createResult("ESC-4.4", "Severe neonatal jaundice (palms & soles yellow)", "Bilirubin encephalopathy risk", "REFER TO SNCU")
        }

        // 3. CHILD GENERAL DANGER SIGNS (ESC-5)
        // Convulsions / Fits / Seizures (ESC-5.1)
        val hasConvulsions = norm.contains("jhatke") || norm.contains("fits") || norm.contains("daura") ||
                norm.contains("mirgi") || norm.contains("convuls") || norm.contains("seizure") ||
                (norm.contains("aankh") && (norm.contains("upar") || norm.contains("palat"))) ||
                (norm.contains("jhaag") && (norm.contains("munh") || norm.contains("mouth"))) ||
                (norm.contains("daant") && (norm.contains("bheech") || norm.contains("dab")))
        if (hasConvulsions) {
            val leaf = if (norm.contains("pregnant") || norm.contains("garbh") || norm.contains("delivery")) "ESC-2.2" else "ESC-5.1"
            return createResult(leaf, "Convulsions / seizures", "Active seizure", "PROTECT AIRWAY & REFER IMMEDIATELY")
        }

        // Stridor in calm child (ESC-5.2)
        if (norm.contains("stridor") || (norm.contains("seeti") && (norm.contains("gale") || norm.contains("saans") || norm.contains("aawaz")))) {
            return createResult("ESC-5.2", "Stridor in calm child (severe airway obstruction)", "Stridor sign", "URGENT REFERRAL TO HOSPITAL")
        }

        // Vomits everything (ESC-5.1)
        if (norm.contains("sab ulti") || norm.contains("vomits everything") || norm.contains("jo bhi khata ulti") || norm.contains("vomit all")) {
            return createResult("ESC-5.1", "Vomiting everything (unable to retain fluids)", "Severe general danger sign", "REFER TO FRU/CHC")
        }

        // Lethargic or unconscious (ESC-5.1 & ESC-6.3)
        if (norm.contains("behosh") || norm.contains("unconscious")) {
            val leaf = if (norm.contains("sugar") || norm.contains("insulin")) "ESC-6.3" else "ESC-5.1"
            return createResult(leaf, "Loss of consciousness / severe hypoglycaemia", "Coma / altered sensorium", "EMERGENCY: CALL 108 REFERRAL")
        }

        // Prolonged daily fever >= 7 days (ESC-5.4)
        val feverMatcher = Pattern.compile("(\\d+)\\s*din\\s*(?:se)?\\s*(?:roz)?\\s*bukhar").matcher(norm)
        if (feverMatcher.find()) {
            val days = feverMatcher.group(1)?.toIntOrNull() ?: 0
            if (days >= 7) {
                return createResult("ESC-5.4", "Prolonged fever for $days days (>= 7 days)", "Suspected malaria/typhoid/bacteraemia", "REFER TO PHC/CHC FOR BLOOD TESTS")
            }
        }
        if (norm.contains("9 din se") && norm.contains("bukhar")) {
            return createResult("ESC-5.4", "Prolonged fever >= 7 days", "Suspected systemic infection", "REFER TO PHC/CHC")
        }

        // Fever with stiff neck (ESC-5.4)
        if (norm.contains("bukhar") && (norm.contains("gardan akad") || norm.contains("stiff neck"))) {
            return createResult("ESC-5.4", "Fever with stiff neck (meningitis sign)", "Suspected meningitis", "CRITICAL REFERRAL TO FRU")
        }

        // Diarrhoea danger signs: dysentery / blood in stool / >= 14 days / SAM with diarrhoea (ESC-5.3)
        val hasBloodNeg = norm.contains("koi khoon nahi") || norm.contains("khoon nahi") || norm.contains("no blood") || norm.contains("without blood")
        if ((norm.contains("dast") || norm.contains("diarrhoea") || norm.contains("virochanal")) &&
            (norm.contains("khoon") || norm.contains("blood") || norm.contains("raktham")) && !hasBloodNeg
        ) {
            return createResult("ESC-5.3", "Dysentery / blood in diarrhoea", "Invasive intestinal infection", "REFER TO PHC (requires doctor evaluation)")
        }

        val dastDurMatcher = Pattern.compile("(\\d+)\\s*(?:din|days|hafte|weeks)\\s*(?:se)?\\s*(?:roz)?\\s*dast").matcher(norm)
        if (dastDurMatcher.find()) {
            val days = dastDurMatcher.group(1)?.toIntOrNull() ?: 0
            val isWeeks = dastDurMatcher.group(0)?.contains("hafte") == true || dastDurMatcher.group(0)?.contains("weeks") == true
            val totalDays = if (isWeeks) days * 7 else days
            if (totalDays >= 14) {
                return createResult("ESC-5.3", "Persistent diarrhoea for $totalDays days (>= 14 days)", "Persistent enteropathy", "REFER TO CHC")
            }
        }
        if ((norm.contains("dast") || norm.contains("diarrhoea")) &&
            (norm.contains("14 din") || norm.contains("15 din") || norm.contains("16 din") || norm.contains("two weeks") || norm.contains("2 hafte"))
        ) {
            return createResult("ESC-5.3", "Persistent diarrhoea >= 14 days", "Persistent enteropathy", "REFER TO CHC")
        }
        if ((norm.contains("muac 11") || norm.contains("sam")) && (norm.contains("dast") || norm.contains("diarrhoea"))) {
            return createResult("ESC-5.3", "Diarrhoea in SAM child", "High-risk metabolic failure", "REFER TO NRC/MTC")
        }

        // Bilateral pitting oedema (ESC-5.5)
        if ((norm.contains("dono pair") || norm.contains("both feet")) &&
            (norm.contains("sujan") || norm.contains("oedema") || norm.contains("swelling")) &&
            (norm.contains("bachch") || norm.contains("child"))
        ) {
            return createResult("ESC-5.5", "Bilateral pitting oedema (Kwashiorkor)", "Severe acute malnutrition with oedema", "REFER TO NRC")
        }

        // Palmar pallor (ESC-5.7)
        if (norm.contains("palmar pallor") || (norm.contains("hatheli") && norm.contains("safed"))) {
            return createResult("ESC-5.7", "Severe palmar pallor", "Severe paediatric anaemia", "REFER TO FRU/CHC")
        }

        // 4. ADULTS WITH HTN / DIABETES / TRAUMA (ESC-6 & ESC-1.4)
        // Heart attack (ESC-6.1)
        if ((norm.contains("seene me dard") || norm.contains("chest pain") || norm.contains("chhati me dard")) &&
            (norm.contains("paseena") || norm.contains("baazu") || norm.contains("arm") || norm.contains("sweat") || norm.contains("shortness") || norm.contains("tight"))
        ) {
            return createResult("ESC-6.1", "Suspected myocardial infarction / heart attack", "Acute coronary syndrome", "CALL 108 REFERRAL NOW")
        }

        // Stroke (ESC-6.2 - BE FAST)
        if ((norm.contains("tedha") || norm.contains("facial droop") || norm.contains("lakwa") || norm.contains("one side") || norm.contains("ek taraf")) &&
            (norm.contains("chehra") || norm.contains("munh") || norm.contains("kamzor") || norm.contains("gir") || norm.contains("weak") || norm.contains("bol nahi") || norm.contains("slurred") || norm.contains("paralysis"))
        ) {
            return createResult("ESC-6.2", "Suspected stroke (BE-FAST signs)", "Acute cerebrovascular event", "CRITICAL: REFER NOW TO CT-CAPABLE FRU")
        }

        // Hypertensive crisis BP >= 180/110 (ESC-6.4) or Pregnancy BP >= 140/90 (ESC-2.3)
        val bpMatcher = Pattern.compile("(\\d{2,3})\\s*(?:/|\\s+)\\s*(\\d{2,3})").matcher(query.lowercase() + " " + norm)
        if (bpMatcher.find()) {
            val sys = bpMatcher.group(1)?.toIntOrNull() ?: 120
            val dia = bpMatcher.group(2)?.toIntOrNull() ?: 80
            if (sys in 70..260 && dia in 40..160) {
                if (sys >= 180 || dia >= 110) {
                    return createResult("ESC-6.4", "Hypertensive crisis (BP $sys/$dia >= 180/110)", "Severe end-organ risk", "REFER TO CHC/FRU")
                }
                if ((norm.contains("pregnant") || norm.contains("garbh") || norm.contains("mahila") || norm.contains("delivery")) && (sys >= 140 || dia >= 90)) {
                    return createResult("ESC-2.3", "Hypertension in pregnancy (BP $sys/$dia >= 140/90)", "Pre-eclampsia screening trigger", "REFER TO FRU")
                }
            }
        }

        // Snakebite / Poisoning / Severe burns (ESC-1.4)
        if (norm.contains("saanp") || norm.contains("snake")) {
            return createResult("ESC-1.4", "Snakebite emergency", "Envenomation risk - do not cut or tourniquet", "KEEP CALM & TRANSPORT IMMEDIATELY TO FRU (ASV)")
        }
        if (norm.contains("zahar") || norm.contains("poison") || norm.contains("kerosene") || norm.contains("mitti ka tel")) {
            return createResult("ESC-1.4", "Poisoning / chemical ingestion", "Toxic ingestion", "DO NOT INDUCE VOMITING - TRANSPORT TO HOSPITAL")
        }
        if (norm.contains("jal gaya") || norm.contains("severe burn")) {
            return createResult("ESC-1.4", "Severe burn emergency", "Extensive burn injury", "COVER WITH CLEAN SHEET & REFER TO FRU")
        }

        return DangerMatchResult(matched = false)
    }

    private fun createResult(leafId: String, dangerSign: String, matchedPhrase: String, actionText: String): DangerMatchResult {
        val rule = DangerRule(
            id = "RULE-$leafId",
            chunkId = leafId,
            category = "EMERGENCY",
            dangerSign = dangerSign,
            action = actionText,
            sourceCitation = "MoHFW Protocol $leafId",
            note = "High-priority clinical emergency",
            phrasings = emptyMap()
        )
        return DangerMatchResult(
            matched = true,
            leafId = leafId,
            dangerSign = dangerSign,
            rule = rule,
            matchedPhrase = matchedPhrase
        )
    }
}
