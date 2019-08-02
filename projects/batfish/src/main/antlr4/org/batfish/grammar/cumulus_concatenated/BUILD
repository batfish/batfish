package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_concatenated",
    srcs = [
        ":CumulusConcatenatedLexer.java",
        ":CumulusConcatenatedParser.java",
        ":CumulusConcatenatedParserBaseListener.java",
        ":CumulusConcatenatedParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "cumulus_concatenated_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusConcatenatedLexer.java",
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
