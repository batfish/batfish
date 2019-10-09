"""Test rule that executes PMD against a given library."""

def _impl(ctx):
    lib = ctx.attr.lib[JavaInfo]
    src_jars = lib.source_jars
    if len(lib.source_jars) != 1:
        fail("Expecting a single java library")

    src_jar = lib.source_jars[0]

    # We'd like this to be the compile-time jars only, but Bazel provides only
    # hjar or ijar files, which screw PMD up.
    jar_deps = lib.transitive_runtime_jars
    full_transitive_runtime_jars = ":".join([f.short_path for f in jar_deps.to_list()])

    pmd_exe_file = ctx.attr._pmd[DefaultInfo].files_to_run.executable
    ruleset = ctx.file.ruleset
    pmd_cmd_args = [
        pmd_exe_file.short_path,
        "-f text",
        "-R {}".format(ruleset.short_path),
        "-d {}".format(src_jar.short_path),
        "-auxclasspath {}".format(full_transitive_runtime_jars),
    ]
    pmd_cmd = " ".join(pmd_cmd_args)
    script = [
        "#!/usr/bin/env bash",
        "set -o errexit",
        # For debugging, add xtrace
        # "set -o xtrace",
        pmd_cmd,
    ]

    script_content = "\n".join(script)

    # Write the file, it is executed by 'bazel test'.
    test_file = ctx.actions.declare_file(ctx.attr.name)
    ctx.actions.write(
        output = test_file,
        content = script_content,
        is_executable = True,
    )

    # Ensure everything that is needed to run PMD is in the runfiles tree:
    pmd_runfiles = ctx.attr._pmd[DefaultInfo].data_runfiles.files
    runfiles_jar_deps = depset(transitive = [
        # Create a new depset w/ .to_list() to prevent a Java exception from being thrown in
        # bazel 0.29.1 when combining depsets with different orders (in runfiles contruction).
        depset(direct = jar_deps.to_list()),
        pmd_runfiles,
    ])
    exe_runfile_deps = [ruleset, src_jar]
    runfiles = ctx.runfiles(
        transitive_files = runfiles_jar_deps,
        files = exe_runfile_deps,
    )
    return [DefaultInfo(executable = test_file, runfiles = runfiles)]

pmd_test = rule(
    implementation = _impl,
    attrs = {
        "lib": attr.label(
            mandatory = True,
            allow_single_file = True,
            providers = [JavaInfo],
        ),
        "ruleset": attr.label(
            allow_single_file = True,
            default = "@//projects/build-tools:src/main/resources/org/batfish/pmd/pmd-ruleset.xml",
        ),
        "_pmd": attr.label(
            executable = True,
            cfg = "host",
            default = Label("@//projects/build-tools:pmd"),
        ),
    },
    test = True,
)
