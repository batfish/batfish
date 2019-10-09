package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "flatjuniper",
    srcs = glob(
        ["*.java"],
        exclude = ["BUILD"],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish/src/main/antlr4/org/batfish/grammar/flatjuniper",
        "//projects/batfish/src/main/java/org/batfish/representation/juniper",
        "@antlr4_runtime//:compile",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@opentracing_api//:compile",
        "@opentracing_util//:compile",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":flatjuniper",
)
