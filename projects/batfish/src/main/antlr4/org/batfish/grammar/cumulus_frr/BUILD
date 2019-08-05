package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_frr",
    srcs = [
        ":CumulusFrrLexer.java",
        ":CumulusFrrParser.java",
        ":CumulusFrrParserBaseListener.java",
        ":CumulusFrrParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_frr/parsing:cumulus_frr_base_parser",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "cumulus_frr_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusFrrLexer.java",
        "CumulusFrrParser.java",
        "CumulusFrrParserBaseListener.java",
        "CumulusFrrParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CumulusFrrLexer.g4) \
       $(location CumulusFrrParser.g4) \
    -package org.batfish.grammar.cumulus_frr \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
