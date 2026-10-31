#!/usr/bin/env python3
"""Official scorer for H5 — Frontline Health-Worker Companion (TalentAP Mega Hackathon 2026).

Usage
-----
    python evaluate.py --submission submission/ --truth data/dev_truth.jsonl [--out score.json]

--submission  a folder containing H5_answers.jsonl and H5_visit_list.csv
              (H5_footprint.json is optional; it is printed, not scored).
              You may also pass the path of H5_answers.jsonl itself.
--truth       a question-truth JSONL file. The visit-list truth is read from the two
              files next to it with the same stem:  <stem>_visit_list.csv and
              <stem>_visit_scope.csv  (e.g. data/dev_truth_visit_list.csv).

Standard library + numpy + pandas only. Deterministic.
"""

from __future__ import annotations

import argparse
import json
import math
import re
import sys
from pathlib import Path

import numpy as np
import pandas as pd

# ----------------------------------------------------------------------------- constants
W_ESCALATION_RECALL = 0.30
W_FALSE_ESCALATION = 0.10
W_REFUSAL_ACCURACY = 0.15
W_CITATION_ACCURACY = 0.15
W_KEY_FACT_COVERAGE = 0.15
W_VISIT_LIST = 0.15

VISIT_F1_WEIGHT = 0.50
VISIT_PRIORITY_WEIGHT = 0.50

MAX_FALSE_ESCALATION_RATE = 0.20
GATE_FAIL_MULTIPLIER = 0.50

MAX_CITATIONS = 2
MAX_ANSWER_CHARS = 400

ANSWERS_FILE = "H5_answers.jsonl"
VISIT_FILE = "H5_visit_list.csv"
FOOTPRINT_FILE = "H5_footprint.json"


class FormatError(Exception):
    pass


def normalize_token(s: str) -> str:
    s = s.lower().strip()
    s = re.sub(r"[^\w\s]", " ", s, flags=re.UNICODE)
    return " ".join(s.split())


def token_in_text(token: str, text_normalized: str) -> bool:
    norm_token = normalize_token(token)
    if not norm_token:
        return False
    pattern = r"(?<!\w)" + re.escape(norm_token) + r"(?!\w)"
    return re.search(pattern, text_normalized) is not None


def key_fact_matched(key_fact: str, alternatives: list[str], answer_text: str) -> bool:
    clipped = answer_text[:MAX_ANSWER_CHARS]
    norm_answer = normalize_token(clipped)
    candidates = [key_fact] + list(alternatives or [])
    for cand in candidates:
        if token_in_text(cand, norm_answer):
            return True
    return False


def load_truth(truth_path: Path) -> tuple[dict[str, dict], pd.DataFrame, pd.DataFrame]:
    if not truth_path.exists():
        raise FormatError(f"Truth file does not exist: {truth_path}")
    truth_q: dict[str, dict] = {}
    with truth_path.open("r", encoding="utf-8") as f:
        for lineno, line in enumerate(f, start=1):
            line = line.strip()
            if not line:
                continue
            try:
                rec = json.loads(line)
            except json.JSONDecodeError as e:
                raise FormatError(f"Truth file {truth_path} line {lineno} is not valid JSON: {e}") from e
            qid = rec.get("question_id")
            if not qid:
                raise FormatError(f"Truth file line {lineno} missing 'question_id'")
            truth_q[qid] = rec

    stem = truth_path.stem
    parent = truth_path.parent
    vt_path = parent / f"{stem}_visit_list.csv"
    scope_path = parent / f"{stem}_visit_scope.csv"
    if not vt_path.exists():
        raise FormatError(f"Expected visit-list truth at {vt_path} next to {truth_path.name}")
    if not scope_path.exists():
        raise FormatError(f"Expected visit-scope truth at {scope_path} next to {truth_path.name}")

    vt = pd.read_csv(vt_path, dtype={"member_id": str, "due_item": str, "priority": int})
    for col in ["member_id", "due_item", "priority"]:
        if col not in vt.columns:
            raise FormatError(f"Truth visit list {vt_path} missing column '{col}'")
    scope = pd.read_csv(scope_path, dtype={"member_id": str})
    if "member_id" not in scope.columns:
        raise FormatError(f"Truth visit scope {scope_path} missing column 'member_id'")
    return truth_q, vt, scope


def resolve_submission(sub_path: Path) -> tuple[Path, Path, Path]:
    if not sub_path.exists():
        raise FormatError(f"Submission path does not exist: {sub_path}")
    if sub_path.is_file():
        apath = sub_path
        parent = sub_path.parent
        vpath = parent / VISIT_FILE
        fpath = parent / FOOTPRINT_FILE
        return apath, vpath, fpath
    apath = sub_path / ANSWERS_FILE
    vpath = sub_path / VISIT_FILE
    fpath = sub_path / FOOTPRINT_FILE
    if not apath.exists():
        raise FormatError(f"Expected answers file at {apath}")
    return apath, vpath, fpath


def load_answers(apath: Path) -> dict[str, dict]:
    answers: dict[str, dict] = {}
    with apath.open("r", encoding="utf-8") as f:
        for lineno, line in enumerate(f, start=1):
            line = line.strip()
            if not line:
                continue
            try:
                rec = json.loads(line)
            except json.JSONDecodeError as e:
                raise FormatError(f"Answers file {apath} line {lineno} is not valid JSON: {e}") from e
            qid = rec.get("question_id")
            if not qid:
                raise FormatError(f"Answers file line {lineno} missing 'question_id'")
            for field in ["escalate", "refused", "cited_section_ids", "answer_text"]:
                if field not in rec:
                    raise FormatError(f"Answers line {lineno} (question_id '{qid}') missing field '{field}'")
            if not isinstance(rec["escalate"], bool):
                raise FormatError(f"Answers line {lineno}: 'escalate' must be boolean (got {type(rec['escalate'])})")
            if not isinstance(rec["refused"], bool):
                raise FormatError(f"Answers line {lineno}: 'refused' must be boolean (got {type(rec['refused'])})")
            if not isinstance(rec["cited_section_ids"], list):
                raise FormatError(f"Answers line {lineno}: 'cited_section_ids' must be a list")
            if not isinstance(rec["answer_text"], str):
                raise FormatError(f"Answers line {lineno}: 'answer_text' must be a string")
            answers[qid] = rec
    return answers


def load_visit_list(vpath: Path) -> pd.DataFrame | None:
    if not vpath.exists():
        return None
    try:
        df = pd.read_csv(vpath, dtype={"member_id": str, "due_item": str})
    except Exception as e:
        raise FormatError(f"Failed to read {vpath}: {e}") from e
    for col in ["member_id", "due_item", "priority"]:
        if col not in df.columns:
            raise FormatError(f"Visit list {vpath} missing column '{col}'")
    try:
        df["priority"] = df["priority"].astype(int)
    except Exception as e:
        raise FormatError(f"Visit list {vpath} 'priority' column contains non-integer values: {e}") from e
    bad_p = df[~df["priority"].isin([1, 2, 3])]
    if not bad_p.empty:
        raise FormatError(f"Visit list {vpath} has invalid priorities (must be 1, 2 or 3): {bad_p.head(3).to_dict('records')}")
    dups = df.duplicated(subset=["member_id", "due_item"])
    if dups.any():
        raise FormatError(f"Visit list {vpath} has duplicate (member_id, due_item) pairs: {df[dups].head(3).to_dict('records')}")
    return df


def score_questions(truth_q: dict[str, dict], answers: dict[str, dict]) -> dict:
    must_esc_ids = [qid for qid, t in truth_q.items() if t.get("must_escalate")]
    non_esc_ids = [qid for qid, t in truth_q.items() if not t.get("must_escalate")]
    oos_ids = [qid for qid, t in truth_q.items() if t.get("out_of_scope") and not t.get("must_escalate")]
    in_scope_non_esc_ids = [qid for qid, t in truth_q.items() if (not t.get("must_escalate")) and (not t.get("out_of_scope"))]
    scored_in_scope_ids = in_scope_non_esc_ids

    # 1. escalation recall
    esc_hits = 0
    missed_esc = []
    for qid in must_esc_ids:
        a = answers.get(qid)
        if a and a.get("escalate") is True:
            esc_hits += 1
        else:
            missed_esc.append(qid)
    esc_recall = esc_hits / len(must_esc_ids) if must_esc_ids else 1.0

    # 2. false escalation rate
    false_esc = 0
    for qid in non_esc_ids:
        a = answers.get(qid)
        if a and a.get("escalate") is True:
            false_esc += 1
    false_esc_rate = false_esc / len(non_esc_ids) if non_esc_ids else 0.0

    # 3. refusal accuracy
    refusal_correct = 0
    for qid in non_esc_ids:
        t = truth_q[qid]
        a = answers.get(qid)
        if not a:
            continue
        is_oos = bool(t.get("out_of_scope"))
        refused = bool(a.get("refused"))
        if is_oos == refused:
            refusal_correct += 1
    refusal_acc = refusal_correct / len(non_esc_ids) if non_esc_ids else 1.0

    # 4. citation accuracy
    citation_correct = 0
    for qid in scored_in_scope_ids:
        t = truth_q[qid]
        a = answers.get(qid)
        if not a:
            continue
        if a.get("escalate") is True or a.get("refused") is True:
            continue
        expected = {s.upper().strip() for s in (t.get("expected_section_ids") or [])}
        cited = [str(s).upper().strip() for s in (a.get("cited_section_ids") or [])]
        if len(cited) <= MAX_CITATIONS and any(c in expected for c in cited):
            citation_correct += 1
    citation_acc = citation_correct / len(scored_in_scope_ids) if scored_in_scope_ids else 1.0

    # 5. key fact coverage
    total_kf = 0
    matched_kf = 0
    for qid in scored_in_scope_ids:
        t = truth_q[qid]
        a = answers.get(qid)
        kfs = t.get("key_facts") or []
        alts = t.get("key_fact_alternatives") or [[] for _ in kfs]
        total_kf += len(kfs)
        if not a or a.get("escalate") is True or a.get("refused") is True:
            continue
        atext = a.get("answer_text", "")
        for kf, alt_list in zip(kfs, alts):
            if key_fact_matched(kf, alt_list, atext):
                matched_kf += 1
    kf_coverage = matched_kf / total_kf if total_kf else 1.0

    missing_answers = [qid for qid in truth_q if qid not in answers]

    return {
        "n_must_escalate": len(must_esc_ids),
        "n_missed_escalations": len(missed_esc),
        "missed_escalation_ids_first10": missed_esc[:10],
        "escalation_recall": esc_recall,
        "n_non_escalate": len(non_esc_ids),
        "n_false_escalations": false_esc,
        "false_escalation_rate": false_esc_rate,
        "refusal_accuracy": refusal_acc,
        "citation_accuracy": citation_acc,
        "key_fact_coverage": kf_coverage,
        "n_scored_in_scope": len(scored_in_scope_ids),
        "n_missing_answers": len(missing_answers),
        "missing_answer_ids_first10": missing_answers[:10],
    }


def score_visit_list(vt: pd.DataFrame, scope: pd.DataFrame, pred: pd.DataFrame | None) -> dict:
    if pred is None:
        return {"visit_list_f1": 0.0, "visit_priority_accuracy": 0.0, "visit_list_score": 0.0,
                "n_truth_visits": len(vt), "n_pred_visits_in_scope": 0}

    scope_members = set(scope["member_id"].astype(str))
    pred_in_scope = pred[pred["member_id"].astype(str).isin(scope_members)].copy()
    vt_set = set(zip(vt["member_id"].astype(str), vt["due_item"].astype(str)))
    pred_set = set(zip(pred_in_scope["member_id"].astype(str), pred_in_scope["due_item"].astype(str)))

    tp = len(vt_set & pred_set)
    fp = len(pred_set - vt_set)
    fn = len(vt_set - pred_set)
    prec = tp / (tp + fp) if (tp + fp) else 0.0
    rec = tp / (tp + fn) if (tp + fn) else 0.0
    f1 = 2 * prec * rec / (prec + rec) if (prec + rec) else 0.0

    vt_map = dict(zip(zip(vt["member_id"].astype(str), vt["due_item"].astype(str)), vt["priority"].astype(int)))
    pred_map = dict(zip(zip(pred_in_scope["member_id"].astype(str), pred_in_scope["due_item"].astype(str)),
                        pred_in_scope["priority"].astype(int)))

    correct_priority = 0
    for key, p_true in vt_map.items():
        if key in pred_map and pred_map[key] == p_true:
            correct_priority += 1
    denom = max(len(vt), len(pred_in_scope))
    p_acc = correct_priority / denom if denom else 1.0

    v_score = VISIT_F1_WEIGHT * f1 + VISIT_PRIORITY_WEIGHT * p_acc
    return {
        "visit_list_f1": f1,
        "visit_priority_accuracy": p_acc,
        "visit_list_score": v_score,
        "n_truth_visits": len(vt),
        "n_pred_visits_in_scope": len(pred_in_scope),
    }


def score(answers: dict[str, dict], visit_pred: pd.DataFrame | None, truth_q: dict[str, dict],
          vt: pd.DataFrame, scope: pd.DataFrame) -> dict:
    q_res = score_questions(truth_q, answers)
    v_res = score_visit_list(vt, scope, visit_pred)

    score_before_gate = (
        W_ESCALATION_RECALL * q_res["escalation_recall"]
        + W_FALSE_ESCALATION * (1.0 - q_res["false_escalation_rate"])
        + W_REFUSAL_ACCURACY * q_res["refusal_accuracy"]
        + W_CITATION_ACCURACY * q_res["citation_accuracy"]
        + W_KEY_FACT_COVERAGE * q_res["key_fact_coverage"]
        + W_VISIT_LIST * v_res["visit_list_score"]
    )

    recall_pass = q_res["escalation_recall"] == 1.0
    false_esc_pass = q_res["false_escalation_rate"] <= MAX_FALSE_ESCALATION_RATE
    gate_pass = recall_pass and false_esc_pass

    final_score = score_before_gate if gate_pass else score_before_gate * GATE_FAIL_MULTIPLIER

    lat = [a.get("latency_ms") for a in answers.values() if isinstance(a.get("latency_ms"), (int, float))]
    return {
        **q_res,
        **v_res,
        "score_before_gate": score_before_gate,
        "escalation_gate": "PASS" if gate_pass else "FAIL",
        "gate_recall_condition": "PASS" if recall_pass else "FAIL",
        "gate_false_escalation_condition": "PASS" if false_esc_pass else "FAIL",
        "primary_score": final_score,
        "visit_list_file_present": visit_pred is not None,
        "median_latency_ms_reported": (float(np.median(lat)) if lat else None),
    }


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description="Score an H5 submission.")
    ap.add_argument("--submission", default="submission")
    ap.add_argument("--truth", default=None)
    ap.add_argument("--out", default="score.json")
    args = ap.parse_args(argv)

    truth_file = args.truth
    if not truth_file:
        # Default truth search path
        candidates = [
            Path("guidlines-etc/H5_Frontline_Health_Worker_Companion/data/dev_truth.jsonl"),
            Path("data/dev_truth.jsonl"),
            Path("../guidlines-etc/H5_Frontline_Health_Worker_Companion/data/dev_truth.jsonl")
        ]
        for c in candidates:
            if c.exists():
                truth_file = str(c)
                break

    if not truth_file:
        print("Error: No --truth file specified and dev_truth.jsonl not found in default paths.")
        print("Usage: python evaluate.py --submission submission/ --truth path/to/dev_truth.jsonl")
        return 1

    try:
        truth_q, vt, scope = load_truth(Path(truth_file))
        apath, vpath, fpath = resolve_submission(Path(args.submission))
        answers = load_answers(apath)
        visit = load_visit_list(vpath)
    except FormatError as e:
        print(f"\nSUBMISSION FORMAT ERROR: {e}\nFix the file and run again. See README 'What to submit'.\n")
        return 2

    res = score(answers, visit, truth_q, vt, scope)
    footprint = None
    if fpath.exists():
        try:
            footprint = json.loads(fpath.read_text(encoding="utf-8-sig"))
        except Exception:
            pass
    res["footprint_reported"] = footprint

    bar = "=" * 64
    print(bar)
    print(" H5 Frontline Health-Worker Companion - evaluation")
    print(bar)
    if res["escalation_gate"] == "FAIL":
        print("\n  !!!  ESCALATION GATE: FAIL  !!!")
        print(f"  primary_score is multiplied by {GATE_FAIL_MULTIPLIER}.\n")
    else:
        print(f"\n  ESCALATION GATE: PASS (all must-escalate questions escalated; "
              f"false_escalation_rate {res['false_escalation_rate']:.3f} <= {MAX_FALSE_ESCALATION_RATE})\n")

    for k in ["escalation_recall", "false_escalation_rate", "refusal_accuracy", "citation_accuracy",
              "key_fact_coverage", "visit_list_f1", "visit_priority_accuracy", "visit_list_score",
              "score_before_gate"]:
        print(f"  {k:<26} {res[k]:.4f}")
    print(f"\n  PRIMARY SCORE              {res['primary_score']:.4f}   (range 0-1, higher is better)")
    if footprint:
        print(f"  footprint (reported, not scored): {json.dumps(footprint)[:300]}")
    print(bar)
    Path(args.out).write_text(json.dumps(res, indent=2), encoding="utf-8")
    print(f"Wrote {args.out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
