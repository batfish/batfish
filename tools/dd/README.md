# Delta Debugging for Batfish

This tool automates test-case minimization for Batfish bugs using the
[delta debugging](http://www.st.cs.uni-sb.de/dd/) algorithm (Zeller, 1999).

Given a Batfish snapshot that triggers a bug and a test function that detects
the bug, the tool systematically removes files and lines until it finds the
smallest snapshot that still reproduces the problem.

## Requirements

- Python 3.10+
- pybatfish (`pip install pybatfish`)
- A running Batfish server (see `../../tools/bazel_run.sh` or the
  [Batfish docs](https://batfish.readthedocs.io/))

## Quick start

```bash
# Set up a virtualenv
python3 -m venv .venv && source .venv/bin/activate
pip install pybatfish

# Start Batfish in another terminal
../../tools/bazel_run.sh

# Run delta debugging against a snapshot
python main.py --snapshot_path /path/to/snapshot --clear_network
```

## How it works

The tool runs in two phases:

1. **File minimization** -- removes entire files from `configs/` until a
   minimal set of files that still triggers the bug is found.
2. **Line minimization** -- removes individual lines from the remaining
   files until a minimal set of lines that still triggers the bug is found.

Both phases use the `ddmin` algorithm, which is guaranteed to produce a
1-minimal result: removing any single remaining element causes the bug to
disappear.

### Line minimization and hierarchical configs

Line minimization removes individual lines, which can break hierarchical
configuration syntax (e.g., Junos `set`-style configs inside braces). If your
configs are hierarchical, flatten them first using
`bazel run //tools:flatten -- <input> <output>` before running this tool.
Alternatively, skip line minimization and only use file minimization.

## Writing a test function

The core of delta debugging is the **test function**. It takes a pybatfish
`Session` and a `pathlib.Path` to a snapshot directory, and returns one of:

| Return value    | Meaning                                         |
|-----------------|-------------------------------------------------|
| `DD.PASS`       | The bug is **not** present in this snapshot.     |
| `DD.FAIL`       | The bug **is** present -- this is what we want.  |
| `DD.UNRESOLVED` | Cannot determine (e.g., snapshot failed to load).|

The distinction between `FAIL` and `UNRESOLVED` is important. If removing a
file causes a *different* error (not the one you are investigating), return
`UNRESOLVED` so the minimizer does not get sidetracked.

### Example: any parse/convert/dataplane failure

The built-in `init_snapshot_or_device_or_dataplane_fails` function is the
broadest test: it returns `FAIL` if anything goes wrong during snapshot
initialization, file parsing, or dataplane generation.

```python
TESTFN = init_snapshot_or_device_or_dataplane_fails
```

### Example: a specific exception

`dataplane_crashes_with_specific_error` is a more targeted test. It returns
`FAIL` only if dataplane generation throws an exception whose message or
stack trace contains a target string, and `UNRESOLVED` for any other error.
Here, it targets `BdpOscillationException` (route oscillation during
dataplane computation):

```python
TARGET_STRING = "BdpOscillationException"

def dataplane_crashes_with_specific_error(bf, path):
    try:
        bf.init_snapshot(str(path))
    except:
        return DD.UNRESOLVED  # not our bug

    try:
        bf.generate_dataplane()
    except Exception as e:
        if TARGET_STRING in str(e):
            return DD.FAIL   # this is the bug
        return DD.UNRESOLVED  # different bug
    return DD.PASS
```

### Writing your own test function

1. Start Batfish and reproduce the bug manually using pybatfish.
2. Identify how to detect the bug programmatically (exception message,
   question output, specific parse warning, etc.).
3. Write a function following the pattern above. Return `FAIL` for the
   specific bug, `UNRESOLVED` for other errors, and `PASS` when the bug
   is absent.
4. Set `TESTFN` in `main()` to your function.

Tips for faster minimization:
- If the bug is in dataplane generation, consider modifying Batfish to skip
  unnecessary computation after the relevant stage (e.g., terminate after IGP
  computation if you are debugging an OSPF issue).
- If the bug is in parsing, use `-haltonparseerror` and `-haltonconverterror`
  to fail fast.

## Command-line options

| Flag               | Description                                 | Default |
|--------------------|---------------------------------------------|---------|
| `--snapshot_path`  | Path to the snapshot directory (required).  | --      |
| `--dd_network`     | Batfish network name to use during testing. | `DD`    |
| `--clear_network`  | Delete the network before starting.         | false   |

## Output

The tool writes two directories next to the input snapshot:

- `minimized_files/` -- the snapshot after file minimization.
- `minimized_lines/` -- the snapshot after line minimization.

### Reading the log output

```
# Confirm the bug reproduces on the full snapshot.
Testing a snapshot with 7 total files
dd: 7 deltas left

# Split the files into groups and test without each group.
dd (run #1): trying splits [3, 4]
Testing a snapshot with 4 total files
Testing a snapshot with 3 total files

# Neither group could be removed, so split into smaller groups.
dd: increase granularity to 4

dd (run #2): trying splits [1, 2, 2, 2]
Testing a snapshot with 6 total files
Testing a snapshot with 5 total files
...

# Eventually splits reach size 1 (individual files).
dd (run #3): trying splits [1, 1, 1, 1, 1, 1, 1]
...
dd: done

Minimized the snapshot down to 7 files
Wrote the file-minimized snapshot to /path/to/minimized_files
```

After file minimization, line minimization proceeds with the same output
format, but operating on individual lines instead of files.

## Third-party code

`DD.py` is the delta debugging algorithm implementation, based on Andreas
Zeller's [public domain code](https://www.st.cs.uni-sb.de/dd/) (1999-2004).
See the license header in `DD.py` for details.
