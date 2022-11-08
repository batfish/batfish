load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "frr_base",
    srcs = [
        "FrrBaseLexer.java",
        "FrrBaseParser.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:com_google_guava_guava",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)
