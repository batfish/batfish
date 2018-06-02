genrule(
    name = "gen_test_sh",
    outs = ["test.sh"],
    cmd = """
      echo \
        $(rootpath //projects/allinone:allinone_main) \
          -coordinatorargs '"-periodassignworkms 5"' \
          -cmdfile $(location :commands) > $@
        """,
    srcs = [
        "commands",
    ],
    tools = [
        "//projects/allinone:allinone_main",
    ],
)

sh_test(
    name = "ref_tests",
    srcs = ["test.sh"],
    data = glob([
        "*.ref",
    ]) + [
        "commands",
        "//projects/allinone:allinone_main",
        "//test_rigs",
    ],
)
