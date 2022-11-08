load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "grammar",
    srcs = [
        ":CheckPointGatewayLexer.java",
        ":CheckPointGatewayParser.java",
        ":CheckPointGatewayParserBaseListener.java",
        ":CheckPointGatewayParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/vendor/check_point_gateway/grammar:check_point_gateway_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "grammar_generated",
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
    -package org.batfish.vendor.check_point_gateway.grammar \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
