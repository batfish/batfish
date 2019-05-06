package(default_visibility = ["//visibility:private"])

load("@batfish//skylark:junit.bzl", "junit_tests")

junit_tests(
    name = "IngressLocationTest",
    srcs = ["IngressLocationTest.java"],
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/symbolic",
        "@jsr305//:compile",
        "@junit//:compile",
    ],
)
