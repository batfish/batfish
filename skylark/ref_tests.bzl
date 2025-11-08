"""
This file defines the functionality for testing a Batfish reference (ref) test.

These are tests in //tests/*, where they use the client's functionality to
generate output and confirm that output matches an existing reference (extension .ref)."""

load("@rules_shell//shell:sh_test.bzl", "sh_test")

_CMD = """
ALL_FILES="$(rootpaths {allinone})"
BIN_FILE=$$(tr " " "\n" <<< $${{ALL_FILES}} | grep -v jar)
echo \
  $${{BIN_FILE}} --jvm_flag=-ea \
    -coordinatorargs '"-periodassignworkms 5"' \
    -cmdfile $(location {commands}) > $@
"""

def ref_tests(name, commands, allinone = None, extra_deps = None, **kwargs):
    """Create a reference sh_test using the given commands file.

    Args:
      name: the name of the generated ref test.
      commands: the file with the commands in it.
      allinone: the java_binary target to be used.
      extra_deps: extra dependencies of the test.
      **kwargs: other arguments that may be used in the generated sh_test.
    """
    if allinone == None:
        allinone = "//projects/allinone:allinone_main"

    # Generate a shell script that will run allinone on given commands file
    cmd = _CMD.format(allinone = allinone, commands = commands)
    native.genrule(
        name = "gen_" + name + ".sh",
        outs = [name + ".sh"],
        cmd = cmd,
        srcs = [allinone, commands],
    )

    # Reserve 4 cores for ref tests, to indicate they are more expensive than
    # unit tests. This prevents killing laptop CPU but enables more parallelism
    # than `exclusive`.
    # NOTE: bazel messages will look like they are running 2*#cores tests in
    # parallel, but this is not really happening.
    tags = kwargs.get("tags", [])
    tags.append("cpu:4")

    # Run the sh_test on the needed inputs.
    if extra_deps == None:
        extra_deps = []
    sh_test(
        name = name,
        srcs = [name + ".sh"],
        data = [
            allinone,
            commands,
        ] + extra_deps,
        tags = tags,
    )
