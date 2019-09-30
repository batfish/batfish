package(default_visibility = ["//visibility:public"])

java_library(
    name = "flatjuniper_base",
    srcs = [
        ":FlatJuniperBaseLexer.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
        "@jsr305//:compile",
    ],
)
