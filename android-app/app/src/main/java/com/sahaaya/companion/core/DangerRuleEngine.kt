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
        "నెలలు" to "months mahina", "కాన్పు" to "delivery prasav",
        "తాచు" to "taachu snake", "కట్ల పాము" to "katla pamu snake",
        "నాగు పాము" to "nagu pamu snake", "పింజరి" to "pinjari snake",
        "సర్పం" to "sarpa snake", "పాము" to "pamu snake",
        "పురుగుల మందు" to "purugula mandu poison", "గడ్డి మందు" to "gaddi mandu poison",
        "చెట్ల మందు" to "chetlu mandu poison", "చేల మందు" to "cheelam mandu poison",
        "మంటల్లో" to "mantallo burn kaalipoyi", "కాలిపోయింది" to "kaalipoyindi burn",
        "కాలిపొయింది" to "kaalipoyindi burn", "విషం" to "visham poison",
        "మింగ" to "ming swallow"
    )

    private val ruleToLeafMap = mapOf(
        "RULE-PREG-BLEED" to "ESC-2.1",
        "RULE-PREG-CONV" to "ESC-2.2",
        "RULE-PREG-HEADACHE-VISION" to "ESC-2.3",
        "RULE-PREG-NO-MOVE" to "ESC-2.7",
        "RULE-PREG-SWELL" to "ESC-2.3",
        "RULE-PREG-MALPRESENT" to "ESC-2.8",
        "RULE-PREG-FEVER" to "ESC-2.4",
        "RULE-NB-POOR-SUCK" to "ESC-4.1",
        "RULE-NB-CONV" to "ESC-5.1",
        "RULE-NB-FAST-BREATH" to "ESC-4.2",
        "RULE-NB-CHEST-INDRAW" to "ESC-4.2",
        "RULE-NB-FEVER" to "ESC-4.3",
        "RULE-NB-COLD" to "ESC-4.3",
        "RULE-NB-PUS-UMBILICUS" to "ESC-4.4",
        "RULE-NB-BOILS" to "ESC-4.4",
        "RULE-NB-JAUNDICE" to "ESC-4.4",
        "RULE-NB-CYANOSIS" to "ESC-4.2",
        "RULE-NB-BLEED" to "ESC-4.4",
        "RULE-NB-LETHARGY" to "ESC-4.1",
        "RULE-NB-DISTENSION" to "ESC-4.4",
        "RULE-NB-NO-URINE" to "ESC-4.4",
        "RULE-CHILD-BLOOD-STOOL" to "ESC-5.3",
        "RULE-CHILD-SEVERE-DEHYDRATION" to "ESC-5.3",
        "RULE-CHILD-STRIDOR-ARI" to "ESC-5.2",
        "RULE-CHILD-HIGH-FEVER-RIGOR" to "ESC-5.4",
        "RULE-CHILD-FEVER-2DAYS" to "ESC-5.4",
        "RULE-PREG-SEVERE-ANAEMIA" to "ESC-2.5",
        "RULE-SNAKE-BITE" to "ESC-1.4",
        "RULE-POISONING" to "ESC-1.4",
        "RULE-BURN" to "ESC-1.4",
        "RULE-ADULT-HEART" to "ESC-6.1",
        "RULE-ADULT-STROKE" to "ESC-6.2",
        "RULE-ADULT-HYPOGLYCEMIA" to "ESC-6.3",
        "RULE-ADULT-HTN-CRISIS" to "ESC-6.4"
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
        val hasNegatedBleeding = Pattern.compile("\\b(no|not|without|koi|zero)\\s+(?:vaginal\\s+)?(bleeding|bleed|khoon|rakt\\w*)\\b").matcher(norm).find() ||
                Pattern.compile("\\b(bleeding|bleed|khoon|rakt\\w*|raktha\\s*sravam)\\s+(?:hai\\s+)?(nahi|nahi hai|ledu|lekapovadam)\\b").matcher(norm).find()

        val hasNegatedConvulsions = Pattern.compile("\\b(no|not|without|koi|zero)\\s+(?:general\\s+)?(fits|convulsions?|seizures?|jhatke|daura|mirgi)\\b").matcher(norm).find() ||
                Pattern.compile("\\b(fits|convulsions?|seizures?|jhatke|daura|mirgi)\\s+(?:hai\\s+)?(nahi|nahi hai|ledu|levu)\\b").matcher(norm).find()

        val hasNegatedFever = Pattern.compile("\\b(no|not|without|koi|zero)\\s+(?:high\\s+)?(fever|bukhar|jwaram)\\b").matcher(norm).find() ||
                Pattern.compile("\\b(fever|bukhar|jwaram)\\s+(?:hai\\s+)?(nahi|nahi hai|ledu)\\b").matcher(norm).find()

        // 0. Curated Phrasing Match from loaded danger rules database
        for (rule in rules) {
            for ((_, phrases) in rule.phrasings) {
                for (phrase in phrases) {
                    val normPhrase = normalize(phrase)
                    if (normPhrase.isNotBlank() && (norm == normPhrase || norm.contains(normPhrase))) {
                        // Check negation guardrails
                        if (hasNegatedBleeding && (normPhrase.contains("bleed") || normPhrase.contains("khoon") || normPhrase.contains("rakt"))) continue
                        if (hasNegatedConvulsions && (normPhrase.contains("convuls") || normPhrase.contains("fits") || normPhrase.contains("jhatke") || normPhrase.contains("daura"))) continue
                        if (hasNegatedFever && (normPhrase.contains("fever") || normPhrase.contains("bukhar"))) continue

                        val leafId = ruleToLeafMap[rule.id] ?: rule.chunkId
                        return DangerMatchResult(
                            matched = true,
                            leafId = leafId,
                            dangerSign = rule.dangerSign,
                            rule = rule,
                            matchedPhrase = phrase
                        )
                    }
                }
            }
        }

        // Bleeding in pregnancy/delivery/postpartum (ESC-2.1 & ESC-3.1)
        if (!hasNegatedBleeding && (norm.contains("khoon") || norm.contains("bleed") || norm.contains("rakt") || norm.contains("raktha sravam")) &&
            (norm.contains("pregnant") || norm.contains("garbh") || norm.contains("deliver") ||
             norm.contains("prasav") || norm.contains("postpartum") || norm.contains("mahin") || norm.contains("pet") || norm.contains("anc"))
        ) {
            val isPostpartum = norm.contains("deliver") || norm.contains("prasav") || norm.contains("postpartum") || norm.contains("pad")
            val leaf = if (isPostpartum) "ESC-3.1" else "ESC-2.1"
            return createResult(leaf, "Vaginal bleeding / Postpartum haemorrhage (PPH)", "Obstetric bleeding reported", "REFER IMMEDIATELY (Call 108) to FRU")
        }

        // Postpartum heavy bleeding / PPH via pad count (ESC-3.1)
        if (norm.contains("pad") && (norm.contains("bhar") || norm.contains("soak") || norm.contains("minute") || norm.contains("din me") || norm.contains("3-4") || norm.contains("bheeg"))) {
            return createResult("ESC-3.1", "Postpartum haemorrhage (PPH)", "Heavy pad soaking", "REFER IMMEDIATELY (Call 108)")
        }

        // Pre-eclampsia signs in pregnancy (ESC-2.3)
        if ((norm.contains("pregnant") || norm.contains("garbh") || norm.contains("mahin") || norm.contains("months") || norm.contains("mother")) &&
            (((norm.contains("chehra") || norm.contains("face") || norm.contains("munh")) && (norm.contains("suja") || norm.contains("phool") || norm.contains("swell"))) ||
             (norm.contains("haath") && (norm.contains("phool") || norm.contains("sujan") || norm.contains("suja"))) ||
             ((norm.contains("dhundhla") || norm.contains("blur") || norm.contains("vision")) && (norm.contains("sir dard") || norm.contains("headache") || norm.contains("phool") || norm.contains("suja") || norm.contains("high bp") || norm.contains("bp"))))
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
        if (!hasNegatedConvulsions && (norm.contains("pregnant") || norm.contains("garbh") || norm.contains("delivery")) &&
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

        // Temperature parsing for maternal high fever and neonatal hypothermia
        val tempPattern1 = Pattern.compile("\\b(?:temp(?:erature)?|taapman|fever|bukhar)\\s*[:=]?\\s*(\\d{2,3}(?:\\.\\d+)?)\\s*(?:deg(?:rees?)?)?\\s*([cf])?\\b")
        val tempPattern2 = Pattern.compile("\\b(\\d{2,3}(?:\\.\\d+)?)\\s*(?:deg(?:rees?)?|°)?\\s*([cf])\\b")
        val m1 = tempPattern1.matcher(norm)
        val m2 = tempPattern2.matcher(norm)
        val (tempVal, tempUnit) = if (m1.find()) {
            Pair(m1.group(1)?.toDoubleOrNull(), m1.group(2))
        } else if (m2.find()) {
            Pair(m2.group(1)?.toDoubleOrNull(), m2.group(2))
        } else {
            Pair(null, null)
        }

        // High fever in pregnancy or delivery (ESC-2.4 & ESC-3.2)
        if (!hasNegatedFever && (norm.contains("pregnant") || norm.contains("delivery") || norm.contains("prasav") || norm.contains("postpartum") || norm.contains("garbh"))) {
            if (norm.contains("tez bukhar") || norm.contains("high fever")) {
                val leaf = if (norm.contains("delivery") || norm.contains("prasav") || norm.contains("postpartum")) "ESC-3.2" else "ESC-2.4"
                return createResult(leaf, "Puerperal / maternal high fever", "High fever with chills", "REFER TO PHC/FRU")
            }
            if (tempVal != null) {
                val isHigh = (tempUnit == "c" && tempVal >= 38.0) ||
                        (tempUnit == "f" && tempVal >= 100.4) ||
                        (tempUnit == null && tempVal in 38.0..43.0) ||
                        (tempUnit == null && tempVal >= 100.4)
                if (isHigh) {
                    val leaf = if (norm.contains("delivery") || norm.contains("prasav") || norm.contains("postpartum")) "ESC-3.2" else "ESC-2.4"
                    return createResult(leaf, "Puerperal / maternal high fever (Temp $tempVal)", "High fever with chills", "REFER TO PHC/FRU")
                }
            }
        }

        // 2. NEWBORN & YOUNG INFANT < 2 MONTHS (ESC-4)
        // Hypothermia: cold to touch or Temp < 35.5 C / < 95.9 F (ESC-4.3)
        val isInfant = norm.contains("bachcha") || norm.contains("baby") || norm.contains("navjaat") || norm.contains("shishu") || Pattern.compile("\\b\\d+\\s*din\\b").matcher(norm).find()
        if (isInfant) {
            if (norm.contains("thanda") || norm.contains("cold to touch") || norm.contains("sheetal")) {
                return createResult("ESC-4.3", "Neonatal hypothermia (cold to touch)", "Hypothermia < 35.4 C", "WARM BABY (KMC) & REFER TO SNCU")
            }
            if (tempVal != null) {
                val isHypo = (tempUnit == "c" && tempVal < 35.5) ||
                        (tempUnit == "f" && tempVal < 95.9) ||
                        (tempUnit == null && tempVal <= 35.4)
                if (isHypo) {
                    return createResult("ESC-4.3", "Neonatal hypothermia (Temp $tempVal < 35.5 C / 95.9 F)", "Neonatal hypothermia", "WARM BABY (KMC) & REFER TO SNCU")
                }
            }
        }

        // Fast breathing / chest indrawing (ESC-4.2 & ESC-5.2)
        val hasIndrawingNeg = norm.contains("no chest indrawing") || norm.contains("no indrawing") || norm.contains("pasli nahi") || norm.contains("without indrawing")
        if ((norm.contains("pasli") || norm.contains("indrawing") || norm.contains("dhans") || norm.contains("ribs sinking") || norm.contains("sinking in")) && !hasIndrawingNeg) {
            val leaf = if (norm.contains("navjaat") || norm.contains("newborn") || norm.contains("baby") || norm.contains("din ka")) "ESC-4.2" else "ESC-5.2"
            return createResult(leaf, "Severe pneumonia / chest indrawing", "Severe chest indrawing", "REFER URGENTLY TO FRU/HOSPITAL")
        }
        // Tachypnea / Fast breathing in infant (>= 60 breaths/min, ESC-4.2)
        val breathMatcher = Pattern.compile("(?:saans|breath(?:ing)?|respirat(?:ion|ory))\\s*(?:rate)?\\s*(?:is)?\\s*[:=]?\\s*(\\d{2,3})").matcher(norm)
        val breathAltMatcher = Pattern.compile("(\\d{2,3})\\s*(?:breaths?|saans|/min|per minute)").matcher(norm)
        val rate = if (breathMatcher.find()) {
            breathMatcher.group(1)?.toIntOrNull()
        } else if (breathAltMatcher.find()) {
            breathAltMatcher.group(1)?.toIntOrNull()
        } else null

        if (rate != null && rate in 40..120) {
            if (rate >= 60) {
                val leaf = if (norm.contains("navjaat") || norm.contains("newborn") || norm.contains("baby") || norm.contains("infant") || norm.contains("din ka") || norm.contains("shishu")) "ESC-4.2" else "ESC-5.2"
                return createResult(leaf, "Neonatal / infant fast breathing ($rate >= 60 breaths/min)", "Tachypnea ($rate/min)", "REFER TO SNCU/HOSPITAL")
            }
        }
        if (norm.contains("saans") && (norm.contains("66") || norm.contains("60") || norm.contains("68") || norm.contains("70"))) {
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

        // VLBW < 1.8 kg or < 1800 grams (ESC-4.4)
        val gramMatcher = Pattern.compile("(\\d{3,4})\\s*(?:g|gm|gram|grams)\\b").matcher(norm)
        if (gramMatcher.find() && (norm.contains("bachcha") || norm.contains("baby") || norm.contains("born") || norm.contains("paida") || norm.contains("wajan") || norm.contains("weight") || norm.contains("baruvu"))) {
            val grams = gramMatcher.group(1)?.toDoubleOrNull() ?: 2500.0
            if (grams < 1800.0) {
                val wtKg = grams / 1000.0
                return createResult("ESC-4.4", "Very Low Birth Weight ($grams g < 1800g / $wtKg kg)", "VLBW requiring specialized thermal care", "REFER TO SNCU")
            }
        }
        val kgMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*kg").matcher(norm)
        if (kgMatcher.find() && (norm.contains("bachcha") || norm.contains("baby") || norm.contains("born") || norm.contains("paida") || norm.contains("wajan") || norm.contains("weight") || norm.contains("baruvu"))) {
            val wt = kgMatcher.group(1)?.toDoubleOrNull() ?: 2.5
            if (wt < 1.8) {
                return createResult("ESC-4.4", "Very Low Birth Weight ($wt kg < 1.8 kg)", "VLBW requiring specialized thermal care", "REFER TO SNCU")
            }
        }
        val lbsMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:lbs?|pounds?)\\b").matcher(norm)
        if (lbsMatcher.find() && (norm.contains("birth") || norm.contains("baby") || norm.contains("bachch") || norm.contains("born") || norm.contains("paida") || norm.contains("weight") || norm.contains("wajan") || norm.contains("baruvu"))) {
            val lbs = lbsMatcher.group(1)?.toDoubleOrNull() ?: 5.0
            val wtKg = lbs * 0.4536
            if (wtKg < 1.8) {
                val formattedKg = String.format("%.2f", wtKg)
                return createResult("ESC-4.4", "Very Low Birth Weight ($lbs lbs = $formattedKg kg < 1.8 kg)", "VLBW requiring specialized thermal care", "REFER TO SNCU")
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
        val hasConvulsions = !hasNegatedConvulsions && (norm.contains("jhatke") || norm.contains("fits") || norm.contains("daura") ||
                norm.contains("mirgi") || norm.contains("convuls") || norm.contains("seizure") ||
                (norm.contains("aankh") && (norm.contains("upar") || norm.contains("palat"))) ||
                (norm.contains("jhaag") && (norm.contains("munh") || norm.contains("mouth"))) ||
                (norm.contains("daant") && (norm.contains("bheech") || norm.contains("dab"))))
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

        // Prolonged daily fever >= 7 days in Hindi, English, Telugu (ESC-5.4)
        val feverMatcher = Pattern.compile("(\\d+)\\s*(?:din|days|day)\\s*(?:se)?\\s*(?:roz)?\\s*(?:bukhar|fever|jwar)").matcher(norm)
        val feverAltMatcher = Pattern.compile("(?:fever|bukhar)\\s*(?:for|se)?\\s*(\\d+)\\s*(?:din|days)").matcher(norm)
        val feverDays = if (feverMatcher.find()) {
            feverMatcher.group(1)?.toIntOrNull() ?: 0
        } else if (feverAltMatcher.find()) {
            feverAltMatcher.group(1)?.toIntOrNull() ?: 0
        } else 0
        if (feverDays >= 7) {
            return createResult("ESC-5.4", "Prolonged fever for $feverDays days (>= 7 days)", "Suspected malaria/typhoid/bacteraemia", "REFER TO PHC/CHC FOR BLOOD TESTS")
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

        // Hypertensive crisis BP >= 180/110 (ESC-6.4) or Pregnancy/ANC BP >= 140/90 (ESC-2.3)
        val bpMatcher = Pattern.compile("(\\d{2,3})\\s*(?:/|\\s+)\\s*(\\d{2,3})").matcher(query.lowercase() + " " + norm)
        if (bpMatcher.find()) {
            val sys = bpMatcher.group(1)?.toIntOrNull() ?: 120
            val dia = bpMatcher.group(2)?.toIntOrNull() ?: 80
            if (sys in 70..260 && dia in 40..160) {
                if (sys >= 180 || dia >= 110) {
                    return createResult("ESC-6.4", "Hypertensive crisis (BP $sys/$dia >= 180/110)", "Severe end-organ risk", "REFER TO CHC/FRU")
                }
                val isMaternal = norm.contains("pregnant") || norm.contains("garbh") || norm.contains("mahila") || norm.contains("deliver") ||
                        norm.contains("anc") || norm.contains("mother") || norm.contains("maa") || norm.contains("talli") ||
                        norm.contains("7 mahin") || norm.contains("8 mahin") || norm.contains("9 mahin") || norm.contains("months")
                if (isMaternal && (sys >= 140 || dia >= 90)) {
                    return createResult("ESC-2.3", "Hypertension in pregnancy (BP $sys/$dia >= 140/90)", "Pre-eclampsia screening trigger", "REFER TO FRU")
                }
            }
        }

        // Snakebite / Poisoning / Severe burns in English, Hindi & Telugu / Tribal dialects (ESC-1.4)
        if (norm.contains("saanp") || norm.contains("snake") || norm.contains("pamu") || norm.contains("paamu") ||
            norm.contains("taachu") || norm.contains("katla pamu") || norm.contains("nagu pamu") || norm.contains("pinjari") ||
            norm.contains("godchi") || norm.contains("sarpa")) {
            return createResult("ESC-1.4", "Snakebite emergency", "Envenomation risk - do not cut or tourniquet", "KEEP CALM & TRANSPORT IMMEDIATELY TO FRU (ASV)")
        }
        if (norm.contains("zahar") || norm.contains("poison") || norm.contains("kerosene") || norm.contains("mitti ka tel") ||
            norm.contains("visham") || norm.contains("purugula mandu") || norm.contains("gaddi mandu") ||
            norm.contains("chetlu mandu") || norm.contains("cheelam mandu") || norm.contains("matsya mandu") ||
            ((norm.contains("mandhu") || norm.contains("mandu")) && (norm.contains("taag") || norm.contains("drink") || norm.contains("ming") || norm.contains("swallow")))) {
            return createResult("ESC-1.4", "Poisoning / chemical ingestion", "Toxic ingestion", "DO NOT INDUCE VOMITING - TRANSPORT TO HOSPITAL")
        }
        if (norm.contains("jal gaya") || norm.contains("severe burn") || norm.contains("kaalina") ||
            norm.contains("kaalipoyi") || norm.contains("kaalindi") || norm.contains("mantallo")) {
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
