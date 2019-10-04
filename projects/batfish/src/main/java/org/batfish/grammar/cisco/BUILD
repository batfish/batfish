package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "cisco",
    srcs = glob(
        ["**/*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/cisco",
        "//projects/batfish/src/main/java/org/batfish/representation/cisco",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":cisco",
)
