# Batfish AI Coding Instructions

## Architecture Overview
Batfish analyzes network device configurations through a 6-stage pipeline:
1. **Parsing**: ANTLR grammars convert vendor configs to parse trees
2. **Extraction**: Parse trees → vendor-specific Java objects
3. **Conversion**: Vendor-specific → unified vendor-independent model
4. **Post-processing**: Finalize and validate the model
5. **Data Plane**: Compute RIBs/FIBs via protocol simulation
6. **Forwarding Analysis**: Symbolic analysis for reachability/security

Key design: Vendor-independent model enables cross-vendor analysis. Symbolic BDD-based analysis handles large packet sets efficiently.

## Build & Test Workflow
- **Build system**: Bazel (not Maven/Gradle)
- **Java version**: 17 (set JAVA_HOME if multiple versions)
- **Key commands**:
  - Build: `bazel build //projects/allinone:allinone_main`
  - Run service: `tools/bazel_run.sh` or `bazel run //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs questions -containerslocation containers"`
  - Run frontend: `npm run dev` (from `batfish-ui` directory)
  - Run backend: `python3 batfish_api.py` or `./start-backend.sh`
- **Port mapping**: 
  - Batfish default: 9996 (v2), 9997 (coordinator)
  - Python API: 5003
  - Frontend: 5173
- **Local Dev Note**: If running without Docker, ensure `bazel` and `npm` are in your `PATH` (e.g., in `/opt/homebrew/bin` on Apple Silicon).
- **Testing priority**: Run specific tests first, then expand scope

## Code Patterns & Conventions
- **Style**: Google Java Style (2-space indent, 100-char lines, trailing commas)
- **Formatting**: ALWAYS run `tools/fix_java_format.sh` and `tools/run_checkstyle.sh` before submitting changes.
- **Minimization**: Do NOT reformat unchanged code paths. Only modify necessary lines.
- **Consistency**: Match existing style in legacy files even if it differs slightly from modern standards.
- **Annotations**: `@Nullable` on types, `@Nonnull` sparingly, `@Override` always
- **Documentation**: Javadoc for public APIs, inline comments for complex logic
- **Naming**: PascalCase classes, camelCase methods/vars, UPPER_SNAKE constants
- **Organization**: One class per file, composition over inheritance
- **Parsing**: ANTLR grammars in `grammar/` subdirs, extractors extend `ControlPlaneExtractor`
- **Questions**: JSON definitions in `questions/` with corresponding Java classes
- **Models**: Vendor-specific in `vendor/`, unified in `representation/`
- **Tests**: JUnit, mock dependencies, test edge cases, reference tests for parsing


## Pre-commit Hooks & Formatting
The project uses pre-commit to enforce styling and formatting.
- **Java**: `tools/fix_java_format.sh --replace` (Google Java Format)
- **Bazel**: `bazel run //:buildifier.fix`
- **Python**: `black`, `isort`, `autoflake`
- **Checkstyle**: `tools/run_checkstyle.sh` (Manual check, not in pre-commit but required)

## Integration Points
- **Client**: Pybatfish Python SDK for analysis scripting
- **Deployment**: Docker containers (`batfish/allinone`)
- **Data formats**: JSON for questions/results, protocol buffers internally
- **External data**: BGP routes, LLDP/CDP topology can enhance analysis

## Common Pitfalls & Troubleshooting
- **Bazel Sandbox**: `bazel run` executes in a sandbox. Always use ABSOLUTE PATHS for `-templatedirs` and `-containerslocation` to avoid `NoSuchFileException`.
- **Address already in use**: Grizzly will fail to start if another instance is running. Use `pkill -f allinone_main` to clear conflicting processes.
- **Python venv**: Prefer using the system python or a dedicated venv to avoid `urllib3` vs `LibreSSL` warnings on macOS.
- Don't use Maven commands - always Bazel
- Set Java 17 explicitly if multiple JDKs installed
- Run specific tests before full suite to isolate issues
- Update both grammar and extractor when adding config features
- Maintain vendor-independent model for new analysis features

## Key Files
- Pipeline overview: `docs/architecture/pipeline_overview.md`
- Build guide: `docs/building_and_running/README.md`
- Coding standards: `docs/development/coding_standards.md`
- Testing guide: `docs/development/testing_guide.md`
- Example grammar: `projects/batfish/src/main/java/org/batfish/grammar/cisco/`
- Example question: `questions/stable/routes.json` and corresponding Java class