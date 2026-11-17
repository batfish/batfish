"""
This file creates a java_test target that runs tests on the JUnit Platform.
"""

# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Starlark rule to generate the list of test classes for the runner.
# Assumes srcs are all .java Test files.
# Assumes the JUnit Jupiter API is already added to deps by the user.

# See https://github.com/bazelbuild/bazel/issues/1017 for background.

load("@batfish//skylark:pmd_test.bzl", "pmd_test")
load("@rules_java//java:defs.bzl", "java_library", "java_test")

_PREFIXES = ("org", "com", "edu", "net")

def _SafeIndex(lst, val):
    for i, v in enumerate(lst):
        if val == v:
            return i
    return -1

def _AsClassName(fname):
    fname = [x.path for x in fname.files.to_list()][0]
    toks = fname[:-5].split("/")
    findex = -1
    for s in _PREFIXES:
        findex = _SafeIndex(toks, s)
        if findex != -1:
            break
    if findex == -1:
        fail(
            "%s does not contain any of %s" % (
                fname,
                _PREFIXES,
            ),
        )
    return ".".join(toks[findex:])

def _impl(ctx):
    ctx.actions.write(
        output = ctx.outputs.out,
        content = "".join([_AsClassName(x) + "\n" for x in ctx.attr.srcs]),
    )

_gen_test_classes = rule(
    attrs = {
        "srcs": attr.label_list(allow_files = True),
    },
    outputs = {"out": "%{name}.txt"},
    implementation = _impl,
)

# The attributes java_test accepts and java_library does not, and vice versa.
# rules_java defines them: java_test takes java_binary's attributes plus the ones
# Bazel adds to every test rule, while java_library takes only the compilation
# attributes. //skylark:junit_attrs_test rederives both lists from the rules
# themselves, so a rules_java upgrade that changes them fails there rather than
# in the next BUILD file to pass a new attribute to junit_tests.
TEST_ONLY_ATTRS = [
    "args",
    "classpath_resources",
    "create_executable",
    "deploy_manifest_lines",
    "env",
    "env_inherit",
    "flaky",
    "jvm_flags",
    "launcher",
    "local",
    "main_class",
    "shard_count",
    "size",
    "stamp",
    "test_class",
    "timeout",
    "use_launcher",
    "use_testrunner",
]

LIBRARY_ONLY_ATTRS = [
    "exported_plugins",
    "exports",
    "javabuilder_jvm_flags",
    "proguard_specs",
]

_RUNNER_MAIN_CLASS = "org.batfish.tools.junit.JUnitPlatformRunner"

_RUNNER_RUNTIME_DEPS = [
    "@batfish//tools/junit/org/batfish/tools/junit:runner",
    "@maven//:org_junit_jupiter_junit_jupiter_engine",
]

def _merge(a, b):
    return list(a) + [x for x in b if x not in a]

def junit_tests(name, srcs, skip_pmd = False, **kwargs):
    """Create a java_test that runs the given test sources on the JUnit Platform.

    The tests run under //tools/junit/org/batfish/tools/junit:runner, which does not
    support test sharding, so shard_count is rejected.

    Args:
      name: the name of the generated test.
      srcs: the source code of the tests.
      skip_pmd: if True, a corresponding pmd_test will not be generated over the test sources.
      **kwargs: other arguments that may be used in the generated java_test. The most common
        value is deps.
    """
    if "shard_count" in kwargs:
        fail("junit_tests does not support shard_count: the JUnit Platform runner does not implement test sharding")
    if len(srcs) == 0:
        return

    # Create a java library containing all the test sources
    lib_kwargs = dict(**kwargs)
    for attr_name in TEST_ONLY_ATTRS:
        lib_kwargs.pop(attr_name, None)
    testlib_name = name + "_testlib"
    java_library(
        name = testlib_name,
        srcs = srcs,
        testonly = True,
        **lib_kwargs
    )

    # List the test classes for the runner.
    classes_name = name + "_test_classes"
    _gen_test_classes(
        name = classes_name,
        srcs = [s for s in srcs if s.endswith("Test.java")],
    )

    # Run the tests with existing deps + new testlib. The java_test has no
    # sources of its own, so everything it needs is a runtime dep.
    test_kwargs = dict(**kwargs)
    for attr_name in LIBRARY_ONLY_ATTRS:
        test_kwargs.pop(attr_name, None)
    runtime_deps = _merge(
        test_kwargs.pop("deps", []) + test_kwargs.pop("runtime_deps", []),
        [":" + testlib_name] + _RUNNER_RUNTIME_DEPS,
    )
    java_test(
        name = name,
        main_class = _RUNNER_MAIN_CLASS,
        use_testrunner = False,
        args = ["$(rootpath :%s)" % classes_name] + test_kwargs.pop("args", []),
        data = [":" + classes_name] + test_kwargs.pop("data", []),
        jvm_flags = ["-ea"] + test_kwargs.pop("jvm_flags", []),
        runtime_deps = runtime_deps,
        **test_kwargs
    )

    # If PMD is on, generate the pmd_test
    if skip_pmd:
        return

    pmd_test(
        name = name + "_pmd",
        lib = testlib_name,
    )
