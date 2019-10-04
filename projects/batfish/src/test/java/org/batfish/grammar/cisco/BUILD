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
    resources = [
        "//projects/batfish/src/test/resources/org/batfish/grammar/cisco/testconfigs",
        "//projects/batfish/src/test/resources/org/batfish/grammar/cisco/testrigs",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/batfish/src/main/java/org/batfish/grammar/cisco",
        "//projects/batfish/src/main/java/org/batfish/representation/cisco",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)
