genrule(
    name = "gen_test_sh",
    outs = ["test.sh"],
    cmd = """
      ALL_FILES="$(rootpaths //projects/allinone:allinone_main)"
      BIN_FILE=$$(tr " " "\n" <<< $${ALL_FILES} | grep -v jar)
      echo \
        $${BIN_FILE} \
          -coordinatorargs '"-periodassignworkms 5"' \
          -cmdfile $(location :commands) > $@
        """,
    srcs = [
        "commands",
        "//projects/allinone:allinone_main",
    ],
)

sh_test(
    name = "ref_tests",
    srcs = ["test.sh"],
    data = glob([
        "*.ref",
        "templates/*.json",
    ]) + [
        "commands",
        "//projects/allinone:allinone_main",
        "//test_rigs",
    ],
)
