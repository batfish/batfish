"""
This file contains rules to build runnable JMH benchmark targets.

JMH Maven dependencies are configured in MODULE.bazel.
"""

load("@rules_java//java:java_binary.bzl", "java_binary")

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
