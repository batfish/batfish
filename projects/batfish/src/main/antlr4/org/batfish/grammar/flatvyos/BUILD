package(default_visibility = ["//visibility:public"])

java_library(
    name = "flatvyos",
    srcs = [
        ":FlatVyosLexer.java",
        ":FlatVyosParser.java",
        ":FlatVyosParserBaseListener.java",
        ":FlatVyosParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "flatvyos_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "FlatVyosLexer.java",
        "FlatVyosParser.java",
        "FlatVyosParserBaseListener.java",
        "FlatVyosParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location FlatVyosLexer.g4) \
       $(location FlatVyosParser.g4) \
    -package org.batfish.grammar.flatvyos \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
