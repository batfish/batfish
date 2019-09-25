package(default_visibility = ["//visibility:public"])

java_library(
    name = "palo_alto_nested",
    srcs = [
        ":PaloAltoNestedLexer.java",
        ":PaloAltoNestedParser.java",
        ":PaloAltoNestedParserBaseListener.java",
        ":PaloAltoNestedParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "palo_alto_nested_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "PaloAltoNestedLexer.java",
        "PaloAltoNestedParser.java",
        "PaloAltoNestedParserBaseListener.java",
        "PaloAltoNestedParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location PaloAltoNestedLexer.g4) \
       $(location PaloAltoNestedParser.g4) \
    -package org.batfish.grammar.palo_alto_nested \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
