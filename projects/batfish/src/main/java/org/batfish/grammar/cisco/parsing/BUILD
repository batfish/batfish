package(default_visibility = ["//visibility:public"])

java_library(
    name = "cisco_base_parser",
    srcs = [
        ":CiscoBaseParser.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
        "@com_google_guava_guava//jar",
        "@jsr305//:compile",
    ],
)
