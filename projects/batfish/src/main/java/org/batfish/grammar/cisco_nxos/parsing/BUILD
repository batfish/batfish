package(default_visibility = ["//visibility:public"])

java_library(
    name = "cisco_nxos_base",
    srcs = [
        ":CiscoNxosBaseLexer.java",
        ":CiscoNxosBaseParser.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
        "@jsr305//:compile",
    ],
)
