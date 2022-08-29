"""
This file defines a rule to use the Java Object Layout (JOL) tool to inspect class sizes in memory.
"""

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

def setup_jol_maven():
    """Sets up the java dependencies to use JOL. We configured JOL in its own Maven namespace to avoid corruption of the main project. JOL is GPL licensed, so while we can use it as a tool it is not suitable for distribution as part of Batfish."""

    if native.existing_rule("jol_maven"):
        return

    maven_install(
        name = "jol_maven",
        artifacts = [maven.artifact(
            group = "org.openjdk.jol",
            artifact = "jol-cli",
            version = "0.16",
            classifier = "full",
        )],
        fetch_sources = True,
        maven_install_json = "@batfish//tools/jol:jol_maven_install.json",
        repositories = [
            "https://repo1.maven.org/maven2",
        ],
        strict_visibility = True,
    )
