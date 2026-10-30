#!/usr/bin/env python3
"""Root evaluation runner for Sahaaya H5.

Usage:
    python3 evaluate.py
    python3 evaluate.py --submission submission_dev --truth guidlines-etc/H5_Frontline_Health_Worker_Companion/data/dev_truth.jsonl
"""
import sys
from pathlib import Path

EVAL_SCRIPT = Path(__file__).parent / "guidlines-etc" / "H5_Frontline_Health_Worker_Companion" / "evaluate.py"

if not EVAL_SCRIPT.exists():
    print(f"Error: Evaluator not found at {EVAL_SCRIPT}")
    sys.exit(1)

# Default arguments if none provided: evaluates submission_dev against dev_truth.jsonl
if len(sys.argv) == 1:
    default_sub = Path(__file__).parent / "submission_dev"
    default_truth = Path(__file__).parent / "guidlines-etc" / "H5_Frontline_Health_Worker_Companion" / "data" / "dev_truth.jsonl"
    sys.argv.extend(["--submission", str(default_sub), "--truth", str(default_truth)])

import runpy
runpy.run_path(str(EVAL_SCRIPT), run_name="__main__")
