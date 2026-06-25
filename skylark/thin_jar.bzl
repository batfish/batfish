"""Rule to produce a thin JAR for embedding Batfish as a library.

Collects all transitive JavaInfo runtime JARs from a target's dependencies,
includes all workspace-internal JARs plus explicitly allowed external JARs,
and excludes all other external (Maven) dependencies. This is useful for
consumers that depend on Batfish as a library and supply the third-party
dependencies themselves (e.g. from their own Maven build).
"""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")

def _should_include(file, include_external):
    """Decide whether a JAR should be included in the thin JAR."""
    path = file.short_path

    # Always include internal (workspace) JARs
    if not path.startswith("../") and not path.startswith("external/"):
        if "/maven/" not in path:
            return True

    # Check if any include_external pattern matches
    for pattern in include_external:
        if pattern in path:
            return True

    return False

def _thin_jar_impl(ctx):
    if not ctx.attr.name.endswith(".jar"):
        fail("thin_jar target name must end in .jar, got: " + ctx.attr.name)
    include_external = ctx.attr.include_external
    all_depsets = []
    for dep in ctx.attr.deps:
        if JavaInfo in dep:
            all_depsets.append(dep[JavaInfo].transitive_runtime_jars)
    all_jars = [
        jar
        for jar in depset(transitive = all_depsets).to_list()
        if _should_include(jar, include_external)
    ]

    output = ctx.actions.declare_file(ctx.attr.name)

    args = ctx.actions.args()
    args.add("--output", output)
    args.add("--compression")
    args.add_all([j.path for j in all_jars], before_each = "--sources")

    ctx.actions.run(
        inputs = all_jars,
        outputs = [output],
        executable = ctx.executable._singlejar,
        arguments = [args],
        mnemonic = "ThinJar",
        progress_message = "Building thin JAR %s (%d JARs)" % (
            ctx.label,
            len(all_jars),
        ),
    )

    return [DefaultInfo(files = depset([output]))]

thin_jar = rule(
    implementation = _thin_jar_impl,
    attrs = {
        "deps": attr.label_list(
            providers = [JavaInfo],
            doc = "Dependencies to collect JARs from.",
        ),
        "include_external": attr.string_list(
            default = [],
            doc = "Substrings to match against external JAR paths to include. " +
                  "These are deps the consumer does not supply itself, so they " +
                  "must be bundled. Ideally this list should be empty.",
        ),
        "_singlejar": attr.label(
            default = Label("@bazel_tools//tools/jdk:singlejar"),
            cfg = "exec",
            executable = True,
        ),
    },
    doc = "Produces a JAR with workspace-internal classes plus selected external deps, " +
          "excluding all other external (Maven) dependencies.",
)
