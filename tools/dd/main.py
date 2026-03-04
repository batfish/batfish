#!/usr/bin/env python
import argparse
import logging
import pathlib
import shutil
import tempfile
from collections import defaultdict
from typing import Callable, Dict, List, Union

from DD import DD
from pybatfish.client.session import Session

logging.basicConfig(format="%(asctime)s %(levelname)s: %(message)s", level=logging.WARN)
logger = logging.getLogger("dd")
logger.setLevel(logging.INFO)


def make_snapshot_structure_from_list_of_files(
    *, snapshot: pathlib.Path, files: List[pathlib.Path], outdir: pathlib.Path
):
    for other in snapshot.glob("*"):
        if other.name == "configs":
            continue
        if other.is_file():
            shutil.copy(other, outdir / other.name)
        else:
            shutil.copytree(other, outdir / other.name)

    for f in files:
        output = outdir / f.relative_to(snapshot)
        output.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy(f, output)


class MinimizeFiles(DD):
    def __init__(
        self,
        *,
        path: pathlib.Path,
        bf: Session,
        testfn: Callable[
            [Session, pathlib.Path], Union[DD.PASS, DD.FAIL, DD.UNRESOLVED]
        ],
    ):
        self._path = path
        self._bf = bf
        self._testfn = testfn
        super(MinimizeFiles, self).__init__()

    def doit(self) -> pathlib.Path:
        files = list(f for f in self._path.glob("configs/**/*") if f.is_file())

        files = self.ddmin(files)
        logger.info("Minimized the snapshot down to %s files", len(files))

        files_output = self._path.parent / "minimized_files"
        if files_output.exists():
            shutil.rmtree(files_output)

        files_output.mkdir()
        make_snapshot_structure_from_list_of_files(
            snapshot=self._path, files=files, outdir=files_output
        )
        logger.info("Wrote the file-minimized snapshot to %s", files_output)
        return files_output

    def _test(self, files: List[pathlib.Path]):
        if not files:
            # TODO: fix batfish crashing on empty configs/
            return self.PASS

        logger.info("Testing a snapshot with %d total files", len(files))
        with tempfile.TemporaryDirectory() as outdirname:
            outdir = pathlib.Path(outdirname)
            make_snapshot_structure_from_list_of_files(
                snapshot=self._path, files=files, outdir=outdir
            )
            return self._testfn(self._bf, outdir)


class MinimizeLines(DD):
    def __init__(
        self,
        *,
        path: pathlib.Path,
        bf: Session,
        testfn: Callable[
            [Session, pathlib.Path], Union[DD.PASS, DD.FAIL, DD.UNRESOLVED]
        ],
    ):
        self._path = path
        self._bf = bf
        self._lines_dict = self._compute_lines_dict()
        self._testfn = testfn
        super(MinimizeLines, self).__init__()

    def _compute_lines_dict(self):
        configs = self._path / "configs"
        lines_dict = {}
        for f in configs.glob("**/*"):
            if not f.is_file():
                continue
            lines = f.read_text().splitlines()
            for line in lines:
                lines_dict[len(lines_dict)] = (f, line)
        return lines_dict

    def assemble_snapshot(self, *, lines: List[int], outdir: pathlib.Path):
        # First assemble all the non-configs to the output directory
        make_snapshot_structure_from_list_of_files(
            snapshot=self._path, files=[], outdir=outdir
        )
        # Second, reassemble each file in the new configs folder
        reassemble_lines: Dict[pathlib.Path, List[str]] = defaultdict(list)
        for line_num in sorted(lines):
            (f, num) = self._lines_dict[line_num]
            reassemble_lines[f].append(num)
        for f, f_lines in reassemble_lines.items():
            text = "\n".join(f_lines)
            file = outdir / f.relative_to(self._path)
            file.parent.mkdir(parents=True, exist_ok=True)
            file.write_text(text)

    def init_snapshot(self, *, lines: List[int]):
        logger.info("Testing a snapshot with %d total lines", len(lines))
        with tempfile.TemporaryDirectory() as outdir:
            self.assemble_snapshot(lines=lines, outdir=pathlib.Path(outdir))
            self._bf.init_snapshot(outdir)

    def doit(self) -> pathlib.Path:
        num_lines = len(self._lines_dict)

        which_lines = self.ddmin(list(range(num_lines)))
        logger.info("Minimized the snapshot down to %d lines", len(which_lines))

        lines_output = self._path.parent / "minimized_lines"
        if lines_output.exists():
            shutil.rmtree(lines_output)

        lines_output.mkdir()
        self.assemble_snapshot(lines=which_lines, outdir=lines_output)
        logger.info("Wrote the line-minimized snapshot to %s", lines_output)
        return lines_output

    def _test(self, lines: List[int]):
        if not lines:
            # TODO: fix batfish crashing on empty configs/
            return self.PASS

        logger.info("Testing a snapshot with %d total lines", len(lines))
        with tempfile.TemporaryDirectory() as outdirname:
            outdir = pathlib.Path(outdirname)
            self.assemble_snapshot(lines=lines, outdir=outdir)
            return self._testfn(self._bf, outdir)


# ---------------------------------------------------------------------------
# Example test functions
#
# Each test function takes a pybatfish Session and a snapshot path, and returns
# DD.PASS, DD.FAIL, or DD.UNRESOLVED. Write your own following these patterns.
# ---------------------------------------------------------------------------


def init_snapshot_or_device_or_dataplane_fails(
    bf: Session, path: pathlib.Path
) -> Union[DD.PASS, DD.FAIL, DD.UNRESOLVED]:
    """Fails if init_snapshot throws an exception, any file fails to
    parse/convert, or dataplane generation crashes."""
    try:
        bf.init_snapshot(
            str(path),
            extra_args={"-haltonparseerror": True, "-haltonconverterror": True},
        )
    except Exception:
        logger.exception("Error initializing snapshot")
        return DD.FAIL

    try:
        fps = bf.q.fileParseStatus().answer().frame()
        if not fps[fps.Status == "FAILED"].empty:
            return DD.FAIL
    except Exception:
        return DD.FAIL

    try:
        bf.generate_dataplane()
    except:
        logger.exception("Error building dataplane")
        return DD.FAIL

    return DD.PASS


def dataplane_crashes_with_specific_error(
    bf: Session, path: pathlib.Path
) -> Union[DD.PASS, DD.FAIL, DD.UNRESOLVED]:
    """Fails if dataplane generation throws an exception whose message or
    stack trace contains a target string.

    This is an example of a targeted test function: it returns UNRESOLVED
    for other errors so that the minimizer does not get distracted by
    unrelated failures.

    Replace TARGET_STRING below with a substring from the exception you
    are investigating (e.g., a class name like BdpOscillationException,
    a method name from the stack trace, or a specific error message)."""

    TARGET_STRING = "BdpOscillationException"

    try:
        bf.init_snapshot(str(path))
    except:
        logger.exception("Unexpected error initializing snapshot")
        return DD.UNRESOLVED

    try:
        bf.generate_dataplane()
    except Exception as e:
        if TARGET_STRING in str(e):
            return DD.FAIL
        else:
            logger.exception("Unexpected dataplane error")
            return DD.UNRESOLVED

    return DD.PASS


def main(args: argparse.Namespace):
    bf = Session.get()
    bf.enable_diagnostics = False

    TESTFN = init_snapshot_or_device_or_dataplane_fails

    if args.clear_network and args.dd_network in bf.list_networks():
        bf.delete_network(args.dd_network)

    bf.set_network(args.dd_network)
    snapshot_path = pathlib.Path(args.snapshot_path)
    dd1 = MinimizeFiles(path=snapshot_path, bf=bf, testfn=TESTFN)
    files_output = dd1.doit()

    dd2 = MinimizeLines(path=files_output, bf=bf, testfn=TESTFN)
    dd2.doit()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Perform delta debugging for Batfish.")
    parser.add_argument("--snapshot_path", type=str, required=True)
    parser.add_argument("--dd_network", type=str, default="DD")
    parser.add_argument("--clear_network", action="store_true")

    args = parser.parse_args()
    main(args)
