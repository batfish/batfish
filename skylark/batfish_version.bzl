"""Exposes the contents of //projects:VERSION as a Starlark constant.

Release builds overwrite projects/VERSION before invoking Bazel. Reading the
file in a repository rule makes the version available at load time, e.g. for
java_binary's deploy_manifest_lines, which does no substitution of its own.
Bazel re-runs the rule whenever the file changes.
"""

def _batfish_version_impl(rctx):
    version = rctx.read(rctx.attr.version_file).strip()
    rctx.file("BUILD.bazel", "")
    rctx.file("version.bzl", "BATFISH_VERSION = %r\n" % version)

batfish_version = repository_rule(
    implementation = _batfish_version_impl,
    attrs = {
        "version_file": attr.label(allow_single_file = True, mandatory = True),
    },
    doc = "Generates @<name>//:version.bzl defining BATFISH_VERSION.",
)
