load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "ios",
    srcs = [
        ":IosRoutingTableLexer.java",
        ":IosRoutingTableParser.java",
        ":IosRoutingTableParserBaseListener.java",
        ":IosRoutingTableParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "ios_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "IosRoutingTableLexer.java",
        "IosRoutingTableLexer.tokens",
        "IosRoutingTableParser.java",
        "IosRoutingTableParserBaseListener.java",
        "IosRoutingTableParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location IosRoutingTableLexer.g4) \
       $(location IosRoutingTableParser.g4) \
    -package org.batfish.grammar.routing_table.ios \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
