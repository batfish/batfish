load("@batfish//skylark:junit.bzl", "junit_tests")

package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

junit_tests(
    name = "tests",
    srcs = glob(["*.java"]),
    resources = ["//projects/batfish-common-protocol/src/test/resources/org/batfish/grammar/recovery_alts"],
    deps = [
        "//projects/batfish-common-protocol:common_lib",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish-common-protocol/src/test/antlr4/org/batfish/grammar/recovery_rule_alts",
        "@maven//:junit_junit",
        "@maven//:org_antlr_antlr4_runtime",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
