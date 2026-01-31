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
"""

import argparse
import re
import sys
from pathlib import Path
from typing import Dict, List, Set, Tuple


class GrammarValidator:
    def __init__(self, grammar_file: Path):
        self.grammar_file = grammar_file
        self.content = grammar_file.read_text()
        self.lines = self.content.split("\n")
        self.errors = []
        self.warnings = []

        # Extracted grammar info
        self.parser_rules: Dict[str, Tuple[str, int]] = (
            {}
        )  # name -> (definition, line_num)
        self.lexer_rules: Set[str] = set()
        self.fragments: Set[str] = set()
        self.imports: List[str] = []
        self.is_main_parser = False

    def validate(self) -> bool:
        """Run all validations and return True if no errors found."""
        self._parse_grammar()

        if not self.parser_rules and not self.lexer_rules:
            return True  # Empty or non-grammar file

        # Only validate parser grammars
        if "parser grammar" in self.content:
            self._validate_parser_rule_names()
            self._validate_null_suffix_usage()
            self._validate_newline_placement()
            self._validate_imports()
            self._validate_ll1_hint()
        elif "lexer grammar" in self.content:
            self._validate_lexer_rule_names()

        return len(self.errors) == 0

    def _parse_grammar(self):
        """Extract rules and imports from the grammar file."""
        current_rule = None
        current_definition = []
        rule_start_line = 0

        for i, line in enumerate(self.lines, 1):
            # Skip comments
            if line.strip().startswith("//"):
                continue

            # Extract imports
            import_match = re.match(r"^import\s+(.+);", line.strip())
            if import_match:
                self.imports.extend(
                    [imp.strip() for imp in import_match.group(1).split(",")]
                )
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
                continue

            # Detect lexer rule or fragment (uppercase start)
            if current_rule is None:
                lexer_rule_match = re.match(r"^([A-Z][A-Z0-9_]*)\s*:", line.strip())
                if lexer_rule_match:
                    rule_name = lexer_rule_match.group(1)
                    if line.strip().startswith("fragment"):
                        self.fragments.add(rule_name)
                    else:
                        self.lexer_rules.add(rule_name)

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

            # Check: recommended prefixes for top-level rules
            if self.is_main_parser and not rule_name.startswith("s_"):
                # Only warn for non-top-level looking rules
                if "_" in rule_name and not rule_name.startswith("_"):
                    self.warnings.append(
                        f"Line {line_num}: Top-level parser rule '{rule_name}' should use 's_' prefix "
                        f"(or follow naming convention in docs/parsing/README.md)"
                    )

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
        # Keywords: ALL_CAPS with underscores
        for rule in self.lexer_rules:
            if rule != rule.upper():
                self.errors.append(
                    f"Lexer rule '{rule}' should be ALL_CAPS with underscores"
                )

        # Fragments: F_CamelCase
        for frag in self.fragments:
            if not frag.startswith("F_"):
                self.warnings.append(
                    f"Fragment '{frag}' should use F_CamelCase naming convention"
                )
            elif not frag[2:].islower() and not frag[2:] == frag[2:].title().replace(
                "_", ""
            ):
                # After F_, should be roughly camelCase or PascalCase
                pass  # Allow flexibility here

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
        validator = GrammarValidator(grammar_file)
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
