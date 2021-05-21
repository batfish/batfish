load("@batfish//skylark:junit.bzl", "junit_tests")
package(
    default_testonly = True,
    default_visibility = ["//visibility:private"],
)

junit_tests(
    name = "tests",
    srcs = glob([
        "**/*Test.java",
    ]),
    deps = [
        "//projects/batfish:batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/bdd",
        "//projects/question",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
