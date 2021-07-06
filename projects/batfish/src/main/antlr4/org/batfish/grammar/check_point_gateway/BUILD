load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "check_point_gateway",
    srcs = [
        ":CheckPointGatewayLexer.java",
        ":CheckPointGatewayParser.java",
        ":CheckPointGatewayParserBaseListener.java",
        ":CheckPointGatewayParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/check_point_gateway/parsing:check_point_gateway_base_parser",
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "check_point_gateway_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CheckPointGatewayLexer.java",
        "CheckPointGatewayLexer.tokens",
        "CheckPointGatewayParser.java",
        "CheckPointGatewayParserBaseListener.java",
        "CheckPointGatewayParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CheckPointGatewayLexer.g4) \
       $(location CheckPointGatewayParser.g4) \
    -package org.batfish.grammar.check_point_gateway \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
