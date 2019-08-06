package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_ports_base",
    srcs = [
        ":CumulusPortsBaseLexer.java",
        ":CumulusPortsBaseParser.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
        "@com_google_guava_guava//jar",
    ],
)
