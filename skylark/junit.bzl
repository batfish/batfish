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

load("@rules_java//java:defs.bzl", "java_library", "java_test")
load("@batfish//skylark:pmd_test.bzl", "pmd_test")

_OUTPUT = """import org.junit.runners.Suite;
import org.junit.runner.RunWith;

@RunWith(Suite.class)
@Suite.SuiteClasses({%s})
public class %s {}
"""

_PREFIXES = ("org", "com", "edu", "net")

def _SafeIndex(l, val):
    for i, v in enumerate(l):
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

_GenSuite = rule(
    attrs = {
        "srcs": attr.label_list(allow_files = True),
        "outname": attr.string(),
    },
    outputs = {"out": "%{name}.java"},
    implementation = _impl,
)

def junit_tests(name, srcs, skip_pmd = False, **kwargs):
    if len(srcs) == 0:
        return

    # Create a java library containing all the test sources
    lib_kwargs = dict(**kwargs)  # have to remove non-java_library args
    lib_kwargs.pop("size", "")
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
    _GenSuite(
        name = s_name,
        srcs = test_files,
        outname = s_name,
    )

    # Run a Java test with suite source file, and existing deps + new testlib
    test_kwargs = dict(**kwargs)
    test_deps = list(kwargs.pop("deps", []))
    java_test(
        name = name,
        test_class = s_name,
        srcs = [":" + s_name],
        deps = test_deps + [":" + testlib_name],
        **kwargs
    )

    # If PMD is on, generate the pmd_test
    if skip_pmd:
        return

    pmd_test(
        name = name + "_pmd",
        lib = testlib_name,
    )
