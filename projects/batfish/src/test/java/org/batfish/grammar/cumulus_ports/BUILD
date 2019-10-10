package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

load("@batfish//skylark:junit.bzl", "junit_tests")

junit_tests(
    name = "tests",
    srcs = glob([
        "**/*Test.java",
    ]),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/cumulus_ports",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_ports",
        "//projects/batfish/src/main/java/org/batfish/representation/cumulus",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)
