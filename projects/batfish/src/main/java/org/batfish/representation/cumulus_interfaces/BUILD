package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "cumulus_interfaces",
    srcs = glob(
        ["*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish/src/main/java/org/batfish/representation/cumulus",
        "@guava//:compile",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":cumulus_interfaces",
)
