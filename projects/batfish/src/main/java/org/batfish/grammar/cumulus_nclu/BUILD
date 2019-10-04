package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "cumulus_nclu",
    srcs = glob(
        ["**/*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/cumulus_nclu",
        "//projects/batfish/src/main/java/org/batfish/representation/cumulus",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
        "@jackson_core//:compile",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":cumulus_nclu",
)
