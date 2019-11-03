package(default_visibility = ["//visibility:private"])

load("@batfish//skylark:junit.bzl", "junit_tests")

junit_tests(
    name = "IngressLocationTest",
    srcs = ["IngressLocationTest.java"],
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/symbolic/src/main/java/org/batfish/symbolic:core",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:junit_junit",
    ],
)
