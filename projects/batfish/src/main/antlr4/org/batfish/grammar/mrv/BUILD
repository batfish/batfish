package(default_visibility = ["//visibility:public"])

java_library(
    name = "mrv",
    srcs = [
        ":MrvLexer.java",
        ":MrvParser.java",
        ":MrvParserBaseListener.java",
        ":MrvParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "mrv_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "MrvLexer.java",
        "MrvParser.java",
        "MrvParserBaseListener.java",
        "MrvParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location MrvLexer.g4) \
       $(location MrvParser.g4) \
    -package org.batfish.grammar.mrv \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
