"""
This file creates a java_test target for JUnit4 run tests.
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

# Starlark rule to generate a Junit4 TestSuite
# Assumes srcs are all .java Test files
# Assumes junit4 is already added to deps by the user.

# See https://github.com/bazelbuild/bazel/issues/1017 for background.

load("@batfish//skylark:pmd_test.bzl", "pmd_test")
load("@rules_java//java:defs.bzl", "java_library", "java_test")

_OUTPUT = """import org.junit.runners.Suite;
import org.junit.runner.RunWith;

@RunWith(Suite.class)
@Suite.SuiteClasses({%s})
public class %s {}
"""

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
    return ".".join(toks[findex:]) + ".class"

def _impl(ctx):
    classes = ",".join(
        [_AsClassName(x) for x in ctx.attr.srcs],
    )
    ctx.actions.write(output = ctx.outputs.out, content = _OUTPUT % (
        classes,
        ctx.attr.outname,
    ))

_gen_suite = rule(
    attrs = {
        "srcs": attr.label_list(allow_files = True),
        "outname": attr.string(),
    },
    outputs = {"out": "%{name}.java"},
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

def junit_tests(name, srcs, skip_pmd = False, **kwargs):
    """Create a reference java_test using the given commands file.

    Args:
      name: the name of the generated test.
      srcs: the source code of the tests.
      skip_pmd: if True, a corresponding pmd_test will not be generated over the test sources.
      **kwargs: other arguments that may be used in the generated java_test. The most common
        value is deps.
    """
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

    # generate a JUnit Suite java file for all the test sources
    s_name = name.replace("-", "_") + "TestSuite"
    test_files = [s for s in srcs if s.endswith("Test.java")]
    _gen_suite(
        name = s_name,
        srcs = test_files,
        outname = s_name,
    )

    # Run a Java test with suite source file, and existing deps + new testlib
    test_kwargs = dict(**kwargs)
    for attr_name in LIBRARY_ONLY_ATTRS:
        test_kwargs.pop(attr_name, None)
    test_deps = list(test_kwargs.pop("deps", []))
    java_test(
        name = name,
        test_class = s_name,
        srcs = [":" + s_name],
        deps = test_deps + [":" + testlib_name],
        **test_kwargs
    )

    # If PMD is on, generate the pmd_test
    if skip_pmd:
        return

    pmd_test(
        name = name + "_pmd",
        lib = testlib_name,
    )
