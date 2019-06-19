package(default_visibility = ["//visibility:public"])

java_library(
    name = "juniper",
    srcs = [
        ":JuniperLexer.java",
        ":JuniperParser.java",
        ":JuniperParserBaseListener.java",
        ":JuniperParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "juniper_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "JuniperLexer.java",
        "JuniperParser.java",
        "JuniperParserBaseListener.java",
        "JuniperParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location JuniperLexer.g4) \
       $(location JuniperParser.g4) \
    -package org.batfish.grammar.juniper \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
