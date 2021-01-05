load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_concatenated",
    srcs = [
        ":CumulusConcatenatedLexer.java",
        ":CumulusConcatenatedParser.java",
        ":CumulusConcatenatedParserBaseListener.java",
        ":CumulusConcatenatedParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cumulus_concatenated_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusConcatenatedLexer.java",
        "CumulusConcatenatedLexer.tokens",
        "CumulusConcatenatedParser.java",
        "CumulusConcatenatedParserBaseListener.java",
        "CumulusConcatenatedParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CumulusConcatenatedLexer.g4) \
       $(location CumulusConcatenatedParser.g4) \
    -package org.batfish.grammar.cumulus_concatenated \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
