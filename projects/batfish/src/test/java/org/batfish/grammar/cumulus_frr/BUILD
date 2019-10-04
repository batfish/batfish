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
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_frr",
        "//projects/batfish/src/main/java/org/batfish/representation/cumulus",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)
