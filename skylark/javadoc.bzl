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
# Javadoc rule.
def _impl(ctx):
    if not ctx.attr.libs:
        fail("no libs provided")

    zip_output = ctx.outputs.zip
    transitive_jar_set = depset()
    source_jars = depset()
    for l in ctx.attr.libs:
        source_jars += l.java.source_jars
        transitive_jar_set += l.java.transitive_deps
    transitive_jar_paths = [j.path for j in transitive_jar_set.to_list()]
    dir = ctx.outputs.zip.path + ".dir"
    source = ctx.outputs.zip.path + ".source"
    external_docs = ["https://docs.oracle.com/javase/8/docs/api/"] + ctx.attr.external_docs
    cmd = [
        "TZ=UTC",
        "export TZ",
        "rm -rf %s" % source,
        "mkdir %s" % source,
        " && ".join(["unzip -qud %s %s" % (source, j.path) for j in source_jars.to_list()]),
        "rm -rf %s" % dir,
        "mkdir %s" % dir,
        " ".join([
            "%s/bin/javadoc" % ctx.attr._jdk[java_common.JavaRuntimeInfo].java_home,
            "-Xdoclint:-missing",
            "-protected",
            "-encoding UTF-8",
            "-charset UTF-8",
            "-notimestamp",
            "-quiet",
            "-windowtitle '%s'" % ctx.attr.title,
            " ".join(["-link %s" % url for url in external_docs]),
            "-sourcepath %s" % source,
            "-subpackages ",
            ":".join(ctx.attr.pkgs),
            " -classpath ",
            ":".join(transitive_jar_paths),
            "-d %s" % dir,
        ]),
        "find %s -exec touch -t 198001010000 '{}' ';'" % dir,
        "(cd %s && zip -Xqr ../%s *)" % (dir, ctx.outputs.zip.basename),
    ]
    ctx.actions.run_shell(
        inputs = transitive_jar_set.to_list() + source_jars.to_list() + ctx.files._jdk,
        outputs = [zip_output],
        command = " && ".join(cmd),
    )

java_doc = rule(
    attrs = {
        "libs": attr.label_list(allow_files = False),
        "pkgs": attr.string_list(),
        "title": attr.string(),
        "external_docs": attr.string_list(),
        "_jdk": attr.label(
            default = Label("@bazel_tools//tools/jdk:current_java_runtime"),
            allow_files = True,
            providers = [java_common.JavaRuntimeInfo],
        ),
    },
    outputs = {"zip": "%{name}.zip"},
    implementation = _impl,
)
