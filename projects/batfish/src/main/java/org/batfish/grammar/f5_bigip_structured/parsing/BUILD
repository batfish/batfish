package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "f5_bigip_structured_base",
    srcs = [
        ":F5BigipStructuredBaseLexer.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
        "@jsr305//:compile",
    ],
)
pmd_test(
    name = "pmd",
    lib = ":f5_bigip_structured_base",
)
