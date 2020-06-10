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
        "//projects/batfish/src/main/java/org/batfish/grammar/juniper",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
