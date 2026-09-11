"""Checks junit.bzl's attribute lists against the java rules it wraps."""

load("@bazel_skylib//rules:diff_test.bzl", "diff_test")
load("@bazel_skylib//rules:write_file.bzl", "write_file")
load("@rules_java//java:defs.bzl", "java_library", "java_test")
load(":junit.bzl", "LIBRARY_ONLY_ATTRS", "TEST_ONLY_ATTRS")

def junit_attrs_test(name):
    """Assert that junit.bzl knows exactly which attributes each java rule takes.

    Declares an empty java_library and java_test, reads their attribute names
    back with native.existing_rule, and diffs each rule's exclusive attributes
    against the corresponding list in junit.bzl.

    Args:
      name: the name of the generated test.
    """
    probe_lib = name + "_probe_lib"
    probe_test = name + "_probe_test"

    # Nothing has to build these probes, only load them, so keep them out of
    # wildcard builds with tags = ["manual"].
    #
    # native.existing_rule omits attributes left at a None default. testonly and
    # timeout are set to make them visible; without that they look like they
    # belong to only one of the two rules.
    java_library(
        name = probe_lib,
        testonly = True,
        tags = ["manual"],
    )
    java_test(
        name = probe_test,
        timeout = "short",
        tags = ["manual"],
        test_class = "Unused",
    )

    lib_attrs = native.existing_rule(probe_lib).keys()
    test_attrs = native.existing_rule(probe_test).keys()

    for kind, observed, declared in [
        ("test_only", [a for a in test_attrs if a not in lib_attrs], TEST_ONLY_ATTRS),
        ("library_only", [a for a in lib_attrs if a not in test_attrs], LIBRARY_ONLY_ATTRS),
    ]:
        write_file(
            name = "%s_%s_observed" % (name, kind),
            out = "%s_%s_observed.txt" % (name, kind),
            content = sorted(observed),
        )
        write_file(
            name = "%s_%s_declared" % (name, kind),
            out = "%s_%s_declared.txt" % (name, kind),
            content = sorted(declared),
        )
        diff_test(
            name = "%s_%s" % (name, kind),
            failure_message = "%s_ATTRS in skylark/junit.bzl is stale; set it to the observed attributes on the left" % kind.upper(),
            file1 = "%s_%s_observed.txt" % (name, kind),
            file2 = "%s_%s_declared.txt" % (name, kind),
        )

    native.test_suite(
        name = name,
        tests = ["%s_test_only" % name, "%s_library_only" % name],
    )
