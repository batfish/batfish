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
        "//projects/batfish/src/test/resources",
        "//projects/batfish/src/test/resources/org/batfish/job/palo_alto",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish-common-protocol:common",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)
