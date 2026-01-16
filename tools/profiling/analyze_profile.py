#!/usr/bin/env python3
"""Analyze async-profiler collapsed format output.

Provides automated analysis of profiling data for CI/automation:
- Extract hotspots from profile data
- Compare profiles between runs
- Detect performance regressions
- Generate reports in machine-readable formats
"""

import argparse
import json
import os
import sys
from collections import defaultdict
from pathlib import Path
from typing import Dict, List, Tuple


def parse_collapsed_profile(filename: str) -> Dict[str, int]:
    """Parse collapsed stack format into a dictionary.

    Args:
        filename: Path to collapsed format profile file

    Returns:
        Dict mapping stack trace to sample count
    """
    stacks = {}
    with open(filename) as f:
        for line in f:
            line = line.strip()
            if not line:
                continue

            parts = line.rsplit(" ", 1)
            if len(parts) != 2:
                print(f"Warning: Skipping malformed line: {line}", file=sys.stderr)
                continue

            stack, count_str = parts
            try:
                stacks[stack] = int(count_str)
            except ValueError:
                print(f"Warning: Invalid count in line: {line}", file=sys.stderr)

    return stacks


def get_hotspots(stacks: Dict[str, int], top_n: int = 20) -> List[Tuple[str, int]]:
    """Extract top N hottest methods from profile.

    Args:
        stacks: Dict mapping stack traces to sample counts
        top_n: Number of top methods to return

    Returns:
        List of (method_name, total_samples) sorted by samples descending
    """
    method_samples = defaultdict(int)

    for stack, count in stacks.items():
        for frame in stack.split(";"):
            method_samples[frame] += count

    return sorted(method_samples.items(), key=lambda x: x[1], reverse=True)[:top_n]


def compare_profiles(baseline_file: str, current_file: str) -> Dict:
    """Compare two profile files and identify regressions.

    Args:
        baseline_file: Path to baseline profile
        current_file: Path to current profile

    Returns:
        Dict with comparison metrics including regressions
    """
    baseline = parse_collapsed_profile(baseline_file)
    current = parse_collapsed_profile(current_file)

    baseline_total = sum(baseline.values())
    current_total = sum(current.values())

    if baseline_total == 0:
        return {
            "baseline_samples": 0,
            "current_samples": current_total,
            "sample_delta_percent": 0,
            "regressions": [],
        }

    baseline_pct = {k: (v / baseline_total * 100) for k, v in baseline.items()}
    current_pct = {k: (v / current_total * 100) for k, v in current.items()}

    regressions = []
    for stack, current_percent in current_pct.items():
        baseline_percent = baseline_pct.get(stack, 0)
        delta = current_percent - baseline_percent
        if delta > 1.0:
            regressions.append(
                {
                    "stack": stack,
                    "baseline_percent": baseline_percent,
                    "current_percent": current_percent,
                    "delta_percent": delta,
                }
            )

    regressions.sort(key=lambda x: x["delta_percent"], reverse=True)

    return {
        "baseline_samples": baseline_total,
        "current_samples": current_total,
        "sample_delta_percent": (current_total - baseline_total) / baseline_total * 100,
        "regressions": regressions[:20],
    }


def main():
    parser = argparse.ArgumentParser(description="Analyze async-profiler output")
    parser.add_argument(
        "command", choices=["hotspots", "compare"], help="Command to execute"
    )
    parser.add_argument("profile", help="Profile file (collapsed format)")
    parser.add_argument("--baseline", help="Baseline profile for comparison")
    parser.add_argument(
        "--top", type=int, default=20, help="Number of top hotspots to show"
    )
    parser.add_argument(
        "--format", choices=["text", "json"], default="text", help="Output format"
    )

    args = parser.parse_args()

    # Resolve relative paths when running under Bazel
    build_wd = os.getenv("BUILD_WORKING_DIRECTORY")
    if build_wd:
        build_path = Path(build_wd)
        args.profile = str(build_path / args.profile)
        if args.baseline:
            args.baseline = str(build_path / args.baseline)

    if args.command == "hotspots":
        stacks = parse_collapsed_profile(args.profile)
        hotspots = get_hotspots(stacks, args.top)
        total_samples = sum(stacks.values())

        if args.format == "json":
            result = {
                "total_samples": total_samples,
                "hotspots": [
                    {
                        "method": method,
                        "samples": samples,
                        "percent": samples / total_samples * 100,
                    }
                    for method, samples in hotspots
                ],
            }
            print(json.dumps(result, indent=2))
        else:
            print(f"Total samples: {total_samples}\n")
            print(f"Top {args.top} hotspots:")
            print("-" * 80)
            for method, samples in hotspots:
                pct = samples / total_samples * 100
                print(f"{pct:6.2f}%  {samples:8d}  {method}")

    elif args.command == "compare":
        if not args.baseline:
            print("Error: --baseline required for compare command", file=sys.stderr)
            sys.exit(1)

        result = compare_profiles(args.baseline, args.profile)

        if args.format == "json":
            print(json.dumps(result, indent=2))
        else:
            print(f"Baseline samples: {result['baseline_samples']}")
            print(f"Current samples:  {result['current_samples']}")
            print(f"Delta:            {result['sample_delta_percent']:+.1f}%\n")

            if result["regressions"]:
                print("Performance Regressions (>1% increase):")
                print("-" * 80)
                for reg in result["regressions"]:
                    print(
                        f"{reg['delta_percent']:+6.2f}%  "
                        f"{reg['baseline_percent']:6.2f}% -> {reg['current_percent']:6.2f}%  "
                        f"{reg['stack'][:60]}"
                    )
            else:
                print("No significant regressions detected.")


if __name__ == "__main__":
    main()
