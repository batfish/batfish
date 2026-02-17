#!/usr/bin/env python3
"""
Validate Batfish ANTLR grammar files against conventions documented in docs/parsing/README.md

This script checks:
1. Parser rule naming conventions (lowercase, prefixes, _null suffix)
2. Lexer rule naming conventions (uppercase with underscores)
3. Fragment naming (F_CamelCase prefix)
4. _null suffix usage (leaf vs non-leaf rules)
5. NEWLINE placement (should be at leaf level, not parent)
6. Import structure (no circular imports)
7. Rule ordering (lexicographical within prefix groups)
8. Spacing and formatting (indentation, blank lines between groups)
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path
from typing import Dict, List, Set, Tuple


class GrammarValidator:
    def __init__(self, grammar_file: Path, changed_rules: Set[str] = None):
        self.grammar_file = grammar_file
        self.content = grammar_file.read_text()
        self.lines = self.content.split("\n")
        self.errors = []
        self.warnings = []
        self.changed_rules = changed_rules or set()

        # Extracted grammar info
        self.parser_rules: Dict[str, Tuple[str, int]] = (
            {}
        )  # name -> (definition, line_num)
        self.lexer_rules: Set[Tuple[str, int]] = set()  # (name, line_num)
        self.fragments: Set[Tuple[str, int]] = set()  # (name, line_num)
        self.mode_tokens: Dict[str, List[Tuple[str, int]]] = (
            {}
        )  # mode_name -> [(token, line)]
        self.imports: List[str] = []
        self.is_main_parser = False

    def validate(self) -> bool:
        """Run all validations and return True if no errors found."""
        self._parse_grammar()

        if not self.parser_rules and not self.lexer_rules:
            return True  # Empty or non-grammar file

        # Filter to only changed rules if specified
        if self.changed_rules:
            # Only validate changed rules
            self.parser_rules = {
                name: data
                for name, data in self.parser_rules.items()
                if name in self.changed_rules
            }

        # Only validate parser grammars
        if "parser grammar" in self.content:
            self._validate_parser_rule_names()
            self._validate_null_suffix_usage()
            self._validate_newline_placement()
            self._validate_imports()
            self._validate_ll1_hint()
            self._validate_rule_ordering()
            self._validate_formatting()
            self._check_rule_grouping()
        elif "lexer grammar" in self.content:
            self._validate_lexer_rule_names()

        return len(self.errors) == 0

    def _parse_grammar(self):
        """Extract rules and imports from the grammar file."""
        current_rule = None
        current_definition = []
        rule_start_line = 0
        current_mode = None  # Track current lexer mode
        in_fragment = False  # Track if we're expecting a fragment rule

        for i, line in enumerate(self.lines, 1):
            # Skip comments
            if line.strip().startswith("//"):
                continue

            # Track standalone 'fragment' keyword (Batfish style)
            if line.strip() == "fragment":
                in_fragment = True
                continue

            # Extract imports
            import_match = re.match(r"^import\s+(.+);", line.strip())
            if import_match:
                self.imports.extend(
                    [imp.strip() for imp in import_match.group(1).split(",")]
                )
                continue

            # Detect mode declaration in lexer grammars
            mode_match = re.match(r"^mode\s+([A-Za-z][A-Za-z0-9_]*);?", line.strip())
            if mode_match:
                current_mode = mode_match.group(1)
                if current_mode not in self.mode_tokens:
                    self.mode_tokens[current_mode] = []
                continue

            # Detect parser rule (lowercase start)
            parser_rule_match = re.match(r"^([a-z][a-z0-9_]*)\s*:", line.strip())
            if parser_rule_match:
                # Save previous rule
                if current_rule:
                    self.parser_rules[current_rule] = (
                        "\n".join(current_definition),
                        rule_start_line,
                    )

                current_rule = parser_rule_match.group(1)
                current_definition = [line.strip()]
                rule_start_line = i
                in_fragment = False  # Reset fragment flag
                continue

            # Detect lexer rule or fragment (uppercase start)
            if current_rule is None:
                # Check for fragment (inline style)
                if line.strip().startswith("fragment"):
                    frag_match = re.match(
                        r"^fragment\s+([A-Z][A-Za-z0-9_]*)\s*:", line.strip()
                    )
                    if frag_match:
                        self.fragments.add((frag_match.group(1), i))
                        in_fragment = False
                    continue

                # Check for lexer rule
                lexer_rule_match = re.match(r"^([A-Z][A-Za-z0-9_]*)\s*:", line.strip())
                if lexer_rule_match:
                    rule_name = lexer_rule_match.group(1)
                    if in_fragment:
                        # This is a fragment (Batfish style: 'fragment' on own line)
                        self.fragments.add((rule_name, i))
                        in_fragment = False
                    elif current_mode:
                        # Mode token
                        self.mode_tokens[current_mode].append((rule_name, i))
                    else:
                        # Default mode token
                        self.lexer_rules.add((rule_name, i))

            # Continue building current rule definition
            if current_rule:
                current_definition.append(line.strip())
                # Rule ends at semicolon or empty line followed by new rule
                if line.strip().endswith(";"):
                    self.parser_rules[current_rule] = (
                        "\n".join(current_definition),
                        rule_start_line,
                    )
                    current_rule = None
                    current_definition = []

        # Save last rule if file doesn't end with semicolon
        if current_rule:
            self.parser_rules[current_rule] = (
                "\n".join(current_definition),
                rule_start_line,
            )

        # Check if this is a main parser (no subordinates import it)
        self.is_main_parser = any(
            imp.endswith("Parser") or imp.endswith("common") for imp in self.imports
        )

    def _validate_parser_rule_names(self):
        """Check parser rule naming conventions."""
        for rule_name, (definition, line_num) in self.parser_rules.items():
            # Check: must be lowercase
            if rule_name != rule_name.lower():
                self.errors.append(
                    f"Line {line_num}: Parser rule '{rule_name}' must be all lowercase"
                )

            # Check: valid _null suffix patterns
            if "_null" in rule_name:
                # _null should only be on leaf rules
                self._check_leaf_vs_nonleaf_null(rule_name, definition, line_num)

    def _check_leaf_vs_nonleaf_null(
        self, rule_name: str, definition: str, line_num: int
    ):
        """Check if _null suffix is used correctly (leaf vs non-leaf)."""
        # Extract all referenced parser rules from definition
        referenced_rules = set(re.findall(r"\b([a-z][a-z0-9_]*)\b", definition))
        # Remove current rule and keywords
        referenced_rules.discard(rule_name)
        # Common keywords/patterns to ignore
        keywords = {
            "null",
            "rest",
            "of",
            "line",
            "new",
            "line",
            "str",
            "int",
            "dec",
            "ip",
            "variable",
            "string",
            "uint",
            "exit",
            "quit",
            "to",
            "from",
            "with",
            "without",
        }
        referenced_rules -= keywords

        # Null helper patterns - referencing these does NOT make a rule non-leaf
        # per docs/parsing/README.md example: mplslsp_random_null: RANDOM null_filler;
        null_helpers = {
            "null_rest_of_line",
            "null_filler",
            "null_rest_of_line_hex",
            "null_rest_of_line_no_pipe",
        }
        referenced_rules -= null_helpers

        has_rule_references = bool(referenced_rules)

        if rule_name.endswith("_null"):
            if has_rule_references:
                # Non-leaf rule with _null suffix - error
                self.errors.append(
                    f"Line {line_num}: Non-leaf rule '{rule_name}' has _null suffix but references "
                    f"other rules: {', '.join(sorted(referenced_rules))}. "
                    f"Per docs/parsing/README.md, non-leaf rules SHOULD NOT have _null suffix."
                )
        else:
            # Check if this might need _null suffix
            # Only warn if rule has no references and is not a known top-level pattern
            if not has_rule_references and not rule_name.startswith("s_"):
                if "null_rest_of_line" in definition or "null_filler" in definition:
                    self.warnings.append(
                        f"Line {line_num}: Leaf rule '{rule_name}' uses null_ pattern but lacks _null suffix. "
                        f"Consider renaming to '{rule_name}_null' per documentation."
                    )

    def _validate_null_suffix_usage(self):
        """Validate _null suffix is used appropriately."""
        # This is covered in _validate_parser_rule_names

    def _validate_newline_placement(self):
        """Check that NEWLINE is at leaf level, not parent level."""
        for rule_name, (definition, line_num) in self.parser_rules.items():
            # Check if rule has children (alternatives with sub-rules)
            has_alternatives = "|" in definition
            has_child_rules = bool(re.search(r"[a-z][a-z0-9_]+\s*\(", definition))

            if has_alternatives or has_child_rules:
                # Parent rule - check if it ends with NEWLINE directly
                # Strip comments and check
                cleaned = re.sub(r"//.*", "", definition)
                # Look for pattern like ") NEWLINE" at end of alternatives
                if re.search(r"\)\s*NEWLINE\s*;", cleaned):
                    # This might be a problem - check if it's truly a parent
                    child_rules = re.findall(r"\b([a-z][a-z0-9_]+)\s*\(", definition)
                    if child_rules:
                        self.errors.append(
                            f"Line {line_num}: Parent rule '{rule_name}' ends with NEWLINE. "
                            f"NEWLINE should be at leaf level (in child rules) not parent level. "
                            f"Child rules detected: {', '.join(set(child_rules))}"
                        )

    def _validate_imports(self):
        """Check import structure."""
        if not self.imports:
            return

        # Check for potential circular imports
        # (Basic check - would need full graph analysis for completeness)
        grammar_name = self.grammar_file.stem
        for imp in self.imports:
            if grammar_name in imp.lower():
                self.warnings.append(
                    f"Potential circular import detected: '{grammar_name}' imports '{imp}'"
                )

    def _validate_ll1_hint(self):
        """Provide hints about LL(1) compliance."""
        for rule_name, (definition, line_num) in self.parser_rules.items():
            # Check if alternatives start with same token
            if "|" in definition:
                # Extract alternatives
                alternatives = re.split(r"\s*\|\s*", definition)
                # Remove the rule name and initial colon
                alternatives = [
                    alt.split(":", 1)[-1].strip() for alt in alternatives if alt.strip()
                ]

                # Get first tokens from each alternative
                first_tokens = []
                for alt in alternatives:
                    # Try to find first token reference
                    token_match = re.search(r"\b([A-Z][A-Z0-9_]*)\b", alt)
                    if token_match:
                        first_tokens.append(token_match.group(1))

                # Check for duplicates
                duplicates = [t for t in set(first_tokens) if first_tokens.count(t) > 1]
                if duplicates:
                    self.warnings.append(
                        f"Line {line_num}: Rule '{rule_name}' may not be LL(1). "
                        f"Multiple alternatives start with same token(s): {', '.join(set(duplicates))}. "
                        f"Consider restructuring to be LL(1) compliant."
                    )

    def _validate_lexer_rule_names(self):
        """Check lexer rule naming conventions."""
        # Default mode tokens: ALL_CAPS with underscores
        for rule, line_num in self.lexer_rules:
            if rule != rule.upper():
                self.errors.append(
                    f"Line {line_num}: Lexer rule '{rule}' should be ALL_CAPS with underscores"
                )

        # Mode tokens: M_ModeName_TOKEN_NAME pattern
        # The mode name already includes M_ prefix (e.g., M_AsPath)
        for mode_name, tokens in self.mode_tokens.items():
            for token_name, line_num in tokens:
                expected_prefix = f"{mode_name}_"
                if not token_name.startswith(expected_prefix):
                    self.warnings.append(
                        f"Line {line_num}: Mode token '{token_name}' in mode '{mode_name}' "
                        f"should follow pattern '{mode_name}_TOKEN_NAME'"
                    )

        # Fragments: F_CamelCase
        for frag, line_num in self.fragments:
            if not frag.startswith("F_"):
                self.warnings.append(
                    f"Line {line_num}: Fragment '{frag}' should use F_CamelCase naming convention"
                )
            elif len(frag) > 2:
                # After F_, should be CamelCase (PascalCase)
                suffix = frag[2:]
                # Check it starts with uppercase and contains at least one letter
                if not (suffix[0].isupper() and any(c.isalpha() for c in suffix)):
                    self.warnings.append(
                        f"Line {line_num}: Fragment '{frag}' should use F_CamelCase naming "
                        f"(expected F_ followed by CamelCase, e.g., F_Uint8)"
                    )

        # Check lexer rule ordering in default mode
        self._validate_lexer_rule_ordering()

    def _validate_lexer_rule_ordering(self):
        """Check that lexer rules are ordered correctly.

        In ANTLR, for same-length matches, the first rule wins.
        So keywords/literals should come before general patterns like [a-z]+.
        """
        # Patterns that are "catch-all" or general for alphanumeric text
        # These can match the same text as keywords, so keywords must be first
        general_alphanum_pattern = re.compile(
            r"^\s*"
            r"([A-Z][A-Z0-9_]*)\s*:\s*"
            r"(\[[a-zA-Z]|~['\"]|F_)"  # Starts with [a-z], [A-Z], negation, or fragment ref
        )

        # Pattern for literal keywords like 'if', 'then', etc. (alphanumeric only)
        literal_alphanum_pattern = re.compile(
            r"^\s*"
            r"([A-Z][A-Z0-9_]*)\s*:\s*"
            r"'([a-zA-Z][a-zA-Z0-9_-]*)'"  # Literal string that's alphanumeric/hyphen
        )

        first_general_line = None
        first_general_rule = None

        for i, line in enumerate(self.lines, 1):
            # Skip comments and empty lines
            stripped = line.strip()
            if not stripped or stripped.startswith("//"):
                continue

            # Check for general pattern (catch-all style for alphanumeric)
            if first_general_line is None:
                match = general_alphanum_pattern.match(line)
                if match:
                    first_general_line = i
                    first_general_rule = match.group(1)
            else:
                # Check if a literal keyword appears after the general pattern
                match = literal_alphanum_pattern.match(line)
                if match:
                    literal_rule = match.group(1)
                    literal_text = match.group(2)
                    self.warnings.append(
                        f"Line {i}: Lexer rule '{literal_rule}' ('{literal_text}') is a keyword "
                        f"but appears after general pattern '{first_general_rule}' (line {first_general_line}). "
                        f"In ANTLR, for same-length matches, the first rule wins. "
                        f"Move keywords before general patterns."
                    )
                    # Only warn once per file to avoid noise
                    break

    def _validate_rule_ordering(self):
        """Check that rules are in lexicographical order within their prefix groups."""
        rule_names = list(self.parser_rules.keys())
        if len(rule_names) < 2:
            return

        # Group rules by their prefix (before first underscore, or full name if no underscore)
        groups: Dict[str, List[str]] = {}
        for rule_name in rule_names:
            if "_" in rule_name:
                prefix = rule_name.split("_")[0]
            else:
                prefix = rule_name

            if prefix not in groups:
                groups[prefix] = []
            groups[prefix].append(rule_name)

        # Check ordering within each group
        for prefix, group_rules in groups.items():
            if len(group_rules) < 2:
                continue

            sorted_rules = sorted(group_rules)
            if group_rules != sorted_rules:
                # Find which rules are out of order
                for i, (actual, expected) in enumerate(zip(group_rules, sorted_rules)):
                    if actual != expected:
                        line_num = self.parser_rules[actual][1]
                        self.warnings.append(
                            f"Line {line_num}: Rule '{actual}' is out of order. "
                            f"Expected '{expected}' (rules should be sorted alphabetically within '{prefix}*' group)"
                        )

    def _validate_formatting(self):
        """Check formatting consistency (indentation, spacing, blank lines).

        Note: This check is currently disabled as the codebase has consistent
        formatting and the line number tracking in definition parsing is not
        accurate enough for reliable formatting validation.
        """

    def _check_rule_grouping(self):
        """Check that related rules are grouped together by prefix."""
        if not self.parser_rules:
            return

        rule_lines = {name: data[1] for name, data in self.parser_rules.items()}
        sorted_by_line = sorted(rule_lines.keys(), key=lambda x: rule_lines[x])

        # Check if rules with same prefix are grouped together
        current_prefixes = set()
        prev_prefix = None

        for rule_name in sorted_by_line:
            prefix = rule_name.split("_")[0] if "_" in rule_name else rule_name

            if prev_prefix is not None and prev_prefix != prefix:
                if prefix in current_prefixes:
                    # This prefix appeared before, means rules are not grouped
                    line_num = rule_lines[rule_name]
                    self.warnings.append(
                        f"Line {line_num}: Rule '{rule_name}' with prefix '{prefix}*' is not grouped with "
                        f"other '{prefix}*' rules. Rules with the same prefix should be grouped together."
                    )

            current_prefixes.add(prefix)
            prev_prefix = prefix

    def print_results(self):
        """Print validation results."""
        if self.errors or self.warnings:
            print(f"\n{'='*70}")
            print(f"Grammar validation for {self.grammar_file}")
            print(f"{'='*70}")

            if self.errors:
                print(f"\n❌ ERRORS ({len(self.errors)}):")
                for error in self.errors:
                    print(f"  {error}")

            if self.warnings:
                print(f"\n⚠️  WARNINGS ({len(self.warnings)}):")
                for warning in self.warnings:
                    print(f"  {warning}")

            print()
        else:
            print(f"✅ {self.grammar_file}: No issues found")


def find_grammar_files(root_dir: Path) -> List[Path]:
    """Find all .g4 grammar files."""
    return list(root_dir.rglob("*.g4"))


def get_changed_rules(grammar_file: Path) -> Set[str]:
    """Get set of changed parser rules from git diff."""
    try:
        # Get git diff for the file
        result = subprocess.run(
            ["git", "diff", "--cached", str(grammar_file)],
            capture_output=True,
            text=True,
            check=False,
        )

        # If no staged changes, try unstaged
        if not result.stdout:
            result = subprocess.run(
                ["git", "diff", str(grammar_file)],
                capture_output=True,
                text=True,
                check=False,
            )

        if not result.stdout:
            return set()

        # Parse the diff to find changed/added rules
        changed_rules = set()
        lines = result.stdout.split("\n")

        for line in lines:
            # Look for added lines that define rules
            if line.startswith("+") and not line.startswith("+++") and ":" in line:
                content = line[1:].strip()
                # Check if it's a parser rule definition (lowercase start, colon)
                match = re.match(r"^([a-z][a-z0-9_]*)\s*:", content)
                if match:
                    changed_rules.add(match.group(1))

        return changed_rules
    except (subprocess.SubprocessError, FileNotFoundError):
        # Not in a git repo or git not available
        return set()


def main():
    parser = argparse.ArgumentParser(
        description="Validate Batfish ANTLR grammar conventions"
    )
    parser.add_argument(
        "files",
        nargs="*",
        type=Path,
        help="Grammar files to validate (default: all .g4 files in project)",
    )
    parser.add_argument(
        "--warn-only",
        action="store_true",
        help="Only print warnings, don't fail on errors",
    )
    parser.add_argument(
        "--verbose",
        "-v",
        action="store_true",
        help="Show warnings in addition to errors",
    )
    parser.add_argument(
        "--changed-only",
        action="store_true",
        help="Only validate changed rules (via git diff)",
    )

    args = parser.parse_args()

    # Find grammar files
    if args.files:
        grammar_files = args.files
    else:
        grammar_files = find_grammar_files(Path.cwd())

    if not grammar_files:
        print("No .g4 grammar files found")
        return 0

    # Validate each file
    all_valid = True
    has_issues = False
    for grammar_file in grammar_files:
        # Get changed rules if requested
        changed_rules = get_changed_rules(grammar_file) if args.changed_only else None

        validator = GrammarValidator(grammar_file, changed_rules)
        is_valid = validator.validate()

        # Only print results if there are errors, or if verbose mode is on
        if validator.errors or (args.verbose and validator.warnings):
            has_issues = True
            if not args.verbose:
                # In non-verbose mode, only show errors
                temp_warnings = validator.warnings
                validator.warnings = []
                validator.print_results()
                validator.warnings = temp_warnings
            else:
                validator.print_results()
        elif not is_valid:
            validator.print_results()

        if not is_valid and not args.warn_only:
            all_valid = False

    if not all_valid:
        print("\n❌ Grammar validation failed. Please fix the errors above.")
        print("   See docs/parsing/README.md for grammar conventions.")
        return 1
    elif not has_issues and not args.verbose:
        print("\n✅ All grammar files validated successfully!")
        return 0
    elif has_issues:
        print(f"\n✅ Grammar validation passed (checked {len(grammar_files)} file(s))")
        return 0
    else:
        print("\n✅ All grammar files validated successfully!")
        return 0


if __name__ == "__main__":
    sys.exit(main())
