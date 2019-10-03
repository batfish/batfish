package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "palo_alto",
    srcs = glob(
        ["*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "@commons_lang3//:compile",
        "@guava//:compile",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":palo_alto",
)
