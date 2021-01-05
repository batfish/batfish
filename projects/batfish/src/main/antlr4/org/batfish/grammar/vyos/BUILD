load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "vyos",
    srcs = [
        ":VyosLexer.java",
        ":VyosParser.java",
        ":VyosParserBaseListener.java",
        ":VyosParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "vyos_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "VyosLexer.java",
        "VyosLexer.tokens",
        "VyosParser.java",
        "VyosParserBaseListener.java",
        "VyosParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location VyosLexer.g4) \
       $(location VyosParser.g4) \
    -package org.batfish.grammar.vyos \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
