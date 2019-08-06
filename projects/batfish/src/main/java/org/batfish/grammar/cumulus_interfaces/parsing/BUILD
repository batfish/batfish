package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_interfaces_base",
    srcs = [
        ":CumulusInterfacesBaseLexer.java",
        ":CumulusInterfacesBaseParser.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
        "@com_google_guava_guava//jar",
    ],
)
