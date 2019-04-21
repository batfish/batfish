package(default_visibility = ["//visibility:public"])

java_library(
    name = "f5_bigip_structured",
    srcs = [
        ":F5BigipStructuredLexer.java",
        ":F5BigipStructuredParser.java",
        ":F5BigipStructuredParserBaseListener.java",
        ":F5BigipStructuredParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "f5_bigip_structured_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "F5BigipStructuredLexer.java",
        "F5BigipStructuredParser.java",
        "F5BigipStructuredParserBaseListener.java",
        "F5BigipStructuredParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location F5BigipStructuredLexer.g4) \
       $(location F5BigipStructuredParser.g4) \
    -package org.batfish.grammar.f5_bigip_structured \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
