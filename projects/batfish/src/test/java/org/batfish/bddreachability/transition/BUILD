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
    resources = [
        "//projects/batfish/src/test/resources",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish-common-protocol:common",
        "//projects/bdd",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
