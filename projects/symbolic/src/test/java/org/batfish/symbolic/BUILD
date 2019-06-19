package(default_visibility = ["//visibility:private"])

load("@batfish//skylark:junit.bzl", "junit_tests")

junit_tests(
    name = "IngressLocationTest",
    srcs = ["IngressLocationTest.java"],
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/symbolic/src/main/java/org/batfish/symbolic:core",
        "@jsr305//:compile",
        "@junit//:compile",
    ],
)
