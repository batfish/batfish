package(default_visibility = ["//visibility:public"])

java_library(
    name = "flatjuniper",
    srcs = [
        ":FlatJuniperLexer.java",
        ":FlatJuniperParser.java",
        ":FlatJuniperParserBaseListener.java",
        ":FlatJuniperParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
    ],
)

genrule(
    name = "flatjuniper_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "FlatJuniperLexer.java",
        "FlatJuniperParser.java",
        "FlatJuniperParserBaseListener.java",
        "FlatJuniperParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location FlatJuniperLexer.g4) \
       $(location FlatJuniperParser.g4) \
    -package org.batfish.grammar.flatjuniper \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
