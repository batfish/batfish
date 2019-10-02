package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

java_library(
    name = "cisco_base_parser",
    srcs = [
        ":CiscoBaseLexer.java",
        ":CiscoBaseParser.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
        "@com_google_guava_guava//jar",
        "@jsr305//:compile",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":cisco_base_parser",
)
