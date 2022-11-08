load("@batfish//skylark:junit.bzl", "junit_tests")

package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

junit_tests(
    name = "tests",
    srcs = glob([
        "**/*Test.java",
    ]),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/bdd",
        "@maven//:junit_junit",
    ],
)
