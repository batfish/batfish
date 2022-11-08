load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "nxos",
    srcs = [
        ":NxosRoutingTableLexer.java",
        ":NxosRoutingTableParser.java",
        ":NxosRoutingTableParserBaseListener.java",
        ":NxosRoutingTableParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "nxos_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "NxosRoutingTableLexer.java",
        "NxosRoutingTableLexer.tokens",
        "NxosRoutingTableParser.java",
        "NxosRoutingTableParserBaseListener.java",
        "NxosRoutingTableParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location NxosRoutingTableLexer.g4) \
       $(location NxosRoutingTableParser.g4) \
    -package org.batfish.grammar.routing_table.nxos \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
