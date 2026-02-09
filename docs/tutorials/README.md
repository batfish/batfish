# Developer Tutorials

This directory contains hands-on tutorials for common Batfish development tasks. Each tutorial is designed to be followed step-by-step with code examples.

## Available Tutorials

### 1. [Adding Your First Vendor Support](adding_vendor_support.md)
**Time**: 2-4 hours | **Difficulty**: Intermediate

Learn how to add support for a new network vendor or device type. Covers:
- Creating ANTLR4 lexer and parser grammars
- Implementing the extractor
- Converting to vendor-independent format
- Writing tests
- Running and validating

**Prerequisites**:
- Familiarity with ANTLR4 grammars
- Understanding of network configuration formats
- Read [Parsing Documentation](../parsing/README.md)

---

### 2. [Writing a Custom Question](writing_custom_questions.md)
**Time**: 1-2 hours | **Difficulty**: Beginner

Learn how to add a new analysis question to Batfish. Covers:
- Understanding question types
- Creating a Java question class
- Adding template parameters
- Testing your question
- Documenting for users

**Prerequisites**:
- Java programming experience
- Understanding of Batfish questions
- Read [Question Development](../question_development/README.md)

---

### 3. [Extending the Data Plane](extending_data_plane.md)
**Time**: 2-3 hours | **Difficulty**: Advanced

Learn how to extend Batfish's data plane computation. Covers:
- Understanding IBDP algorithm
- Adding new protocol support
- Implementing custom forwarding logic
- Testing data plane changes

**Prerequisites**:
- Strong Java programming skills
- Understanding of network protocols
- Read [Data Plane Documentation](../data_plane/README.md)

---

### 4. [Debugging Parser Issues](debugging_parser_issues.md)
**Time**: 1 hour | **Difficulty**: Intermediate

Learn how to debug and fix parser issues. Covers:
- Identifying parse errors
- Using ANTLR debugging tools
- Extractor debugging techniques
- Common parsing pitfalls

**Prerequisites**:
- Working on a parser/grammar
- Familiarity with ANTLR4
- Read [Parser Conventions](../parsing/parser_rule_conventions.md)

---

### 5. [Contributing Your First PR](contributing_first_pr.md)
**Time**: 30 minutes | **Difficulty**: Beginner

Learn the complete workflow for contributing to Batfish. Covers:
- Setting up development environment
- Finding a good first issue
- Making changes and testing
- Creating a pull request
- Responding to review feedback

**Prerequisites**:
- Git experience
- Read [Building and Running](../building_and_running/README.md)
- Read [Contributing Guidelines](../../CONTRIBUTING.md)

---

## How to Use These Tutorials

### Choose Your Starting Point

**New to Batfish development?**
1. Start with [Contributing Your First PR](contributing_first_pr.md)
2. Then try [Writing a Custom Question](writing_custom_questions.md)

**Working on parsers?**
1. Read [Debugging Parser Issues](debugging_parser_issues.md)
2. Then tackle [Adding Your First Vendor Support](adding_vendor_support.md)

**Extending core functionality?**
1. Try [Writing a Custom Question](writing_custom_questions.md)
2. Then advance to [Extending the Data Plane](extending_data_plane.md)

---

### Tutorial Format

Each tutorial follows this structure:

1. **Overview**: What you'll build and learn
2. **Prerequisites**: What you need to know before starting
3. **Step-by-Step Guide**: Detailed instructions with code examples
4. **Testing**: How to verify your changes work
5. **Next Steps**: Where to go from here

---

### Getting Help

If you get stuck:
- Check the [Troubleshooting Guide](../troubleshooting.md)
- Ask questions on [Batfish Slack](https://join.slack.com/t/batfish-org/shared_invite/)
- Open a GitHub issue for bugs or confusion

---

## Contributing to Tutorials

Found an error or want to improve a tutorial? Contributions welcome!

1. Fork the repository
2. Edit the tutorial file
3. Submit a PR with clear description of changes
4. Tag with `documentation` label

---

## Recommended Reading Path

### For Parser Developers
1. [Parsing README](../parsing/README.md) - Complete parsing guide
2. [Parser Conventions](../parsing/parser_rule_conventions.md) - Grammar design patterns
3. [Tutorial: Adding Vendor Support](adding_vendor_support.md) - Hands-on practice
4. [Tutorial: Debugging Parser Issues](debugging_parser_issues.md) - Debugging skills

### For Question Developers
1. [Question Development](../question_development/README.md) - Question basics
2. [Tutorial: Writing Custom Questions](writing_custom_questions.md) - Hands-on practice
3. [Symbolic Engine](../symbolic_engine/README.md) - Understand how questions work
4. [BDD Best Practices](../development/bdd_best_practices.md) - Memory management

### For Core Developers
1. [Architecture README](../architecture/README.md) - System overview
2. [Data Plane](../data_plane/README.md) - Core algorithm
3. [Symbolic Engine](../symbolic_engine/README.md) - Analysis engine
4. [Tutorial: Extending Data Plane](extending_data_plane.md) - Advanced topics

---

## Quick Links

**Documentation**:
- [Quick Reference](../quick_reference.md)
- [Troubleshooting](../troubleshooting.md)
- [Performance Tuning](../performance.md)

**Development**:
- [Development Guide](../development/README.md)
- [Building and Running](../building_and_running/README.md)
- [Contributing Guidelines](../../CONTRIBUTING.md)
