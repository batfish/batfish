package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:junit.bzl", "junit_tests")

java_library(
    name = "symbolic",
    srcs = glob([
        "src/main/**/*.java",
    ]),
    deps = [
        "//projects/batfish-common-protocol:common",
        "@jsr305//:compile",
    ],
)
