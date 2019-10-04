package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "cumulus",
    srcs = glob(
        ["*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "@guava//:compile",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":cumulus",
)
