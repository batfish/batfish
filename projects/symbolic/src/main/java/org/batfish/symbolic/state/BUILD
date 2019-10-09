package(default_visibility = ["//projects/symbolic:__pkg__"])

load("@batfish//skylark:junit.bzl", "junit_tests")

java_library(
    name = "state",
    srcs = glob([
        "*.java",
    ]),
    deps = [
        "@jsr305//:compile",
    ],
)

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

pmd_test(
    name = "pmd",
    lib = ":state",
)
