package(default_visibility = ["//visibility:public"])

java_library(
    name = "vyos",
    srcs = [
        ":VyosLexer.java",
        ":VyosParser.java",
        ":VyosParserBaseListener.java",
        ":VyosParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "vyos_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "VyosLexer.java",
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
