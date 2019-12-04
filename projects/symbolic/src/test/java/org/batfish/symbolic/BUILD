package(default_visibility = ["//visibility:private"])

load("@batfish//skylark:junit.bzl", "junit_tests")

junit_tests(
    name = "IngressLocationTest",
    srcs = ["IngressLocationTest.java"],
    deps = [
        "//projects/symbolic/src/main/java/org/batfish/symbolic:core",
        "@maven//:junit_junit",
    ],
)
