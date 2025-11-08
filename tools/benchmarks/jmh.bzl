"""
This file contains rules to import Java Microbench Harness (JMH) and
to build runnable benchmark targets.
"""

load("@rules_java//java:java_binary.bzl", "java_binary")
load("@rules_jvm_external//:defs.bzl", "maven_install")

def setup_jmh_maven():
    """Sets up the java dependencies to use JMH. We configured JMH in its own Maven namespace to avoid corruption of the main project. JMH is GPL licensed, so while we can use it as a tool it is not suitable for distribution as part of Batfish."""

    if native.existing_rule("jmh_maven"):
        return

    maven_install(
        name = "jmh_maven",
        artifacts = [
            "org.openjdk.jmh:jmh-core:1.35",
            "org.openjdk.jmh:jmh-generator-annprocess:1.35",
        ],
        fetch_sources = True,
        maven_install_json = "@batfish//tools/benchmarks:jmh_maven_install.json",
        repositories = [
            "https://repo1.maven.org/maven2",
        ],
        strict_visibility = True,
    )

def jmh_java_benchmarks(name, srcs, deps = [], tags = [], plugins = [], **kwargs):
    """Builds runnable JMH benchmarks.

    This rule builds a runnable target for one or more JMH benchmarks
    specified as srcs. It takes the same arguments as java_binary,
    except for main_class.
    """
    java_binary(
        name = name,
        srcs = srcs,
        main_class = "org.openjdk.jmh.Main",
        deps = deps + ["@jmh_maven//:org_openjdk_jmh_jmh_core"],
        javacopts = ["-XepDisableAllChecks"],
        plugins = plugins + ["@batfish//tools/benchmarks:jmh_annotation_processor"],
        tags = tags,
        **kwargs
    )
