"""Script used to generate JSON Palo Alto application definitions from their XML representation."""

# Run through bazel:
#  bazel run //tools:generate_pan_apps
import json
import os
from pathlib import Path

import xmltodict


def get_applications(input_dict):
    """Convert Palo Alto query result dict into a list of applications."""
    apps = (
        input_dict.get("response", {})
        .get("result", {})
        .get("application", {})
        .get("entry", [])
    )
    if not apps:
        raise RuntimeError(
            "No applications found. Is the provided application XML in the correct format?"
        )
    return apps


def convert_applications(input_path, output_path):
    """Converts XML applications definitions at input path into JSON and writes them to output path."""
    with open(output_path, "w") as file_out:
        with open(input_path, "r") as file_in:
            json.dump(xmltodict.parse(file_in.read()), file_out)


if __name__ == "__main__":
    workspace_dir = os.getenv("BUILD_WORKSPACE_DIRECTORY")
    if not workspace_dir:
        raise RuntimeError("Must be run inside bazel")
    workspace_path = Path(workspace_dir)
    input_path = workspace_path / "tools" / "palo_alto_applications.xml"
    if not input_path.is_file():
        raise RuntimeError(
            f"Palo Alto applications XML must exist at {input_path.relative_to(workspace_path)}"
        )
    output_path = (
        workspace_path
        / "projects"
        / "batfish"
        / "src"
        / "main"
        / "resources"
        / "org"
        / "batfish"
        / "representation"
        / "palo_alto"
        / "application_definitions"
        / "application_definitions.json"
    )
    convert_applications(input_path, output_path)
