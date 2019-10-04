package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "cumulus_concatenated",
    srcs = glob(
        ["**/*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/cumulus_concatenated",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/cumulus_frr",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/cumulus_interfaces",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/cumulus_ports",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_frr",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_interfaces",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_ports",
        "//projects/batfish/src/main/java/org/batfish/representation/cumulus",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":cumulus_concatenated",
)
