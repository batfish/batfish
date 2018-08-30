_CMD = """
ALL_FILES="$(rootpaths {allinone})"
BIN_FILE=$$(tr " " "\n" <<< $${{ALL_FILES}} | grep -v jar)
echo \
  $${{BIN_FILE}} \
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
        tags = ["exclusive"],
    )
