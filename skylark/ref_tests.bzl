_CMD = """
ALL_FILES="$(rootpaths {allinone})"
BIN_FILE=$$(tr " " "\n" <<< $${{ALL_FILES}} | grep -v jar)
echo \
  $${{BIN_FILE}} --jvm_flag=-ea \
    -coordinatorargs '"-periodassignworkms 5"' \
    -cmdfile $(location {commands}) > $@
"""

def ref_tests(name, commands, allinone = None, extra_deps = None, **kwargs):
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
    native.sh_test(
        name = name,
        srcs = [name + ".sh"],
        data = [
            allinone,
            commands,
        ] + extra_deps,
        tags = tags,
    )
