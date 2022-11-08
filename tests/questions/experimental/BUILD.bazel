load("@batfish//skylark:ref_tests.bzl", "ref_tests")

ref_tests(
    name = "ref_tests",
    allinone = "@batfish//projects/allinone:allinone_with_minesweeper_main",
    commands = ":commands",
    extra_deps = glob(["*.ref"]) + [
        "//questions:experimental",
        "//networks:example",
    ],
)
