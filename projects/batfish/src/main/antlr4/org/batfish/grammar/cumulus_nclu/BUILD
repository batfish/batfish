package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_nclu",
    srcs = [
        ":CumulusNcluLexer.java",
        ":CumulusNcluParser.java",
        ":CumulusNcluParserBaseListener.java",
        ":CumulusNcluParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_nclu/parsing:cumulus_nclu_base_parser",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "cumulus_nclu_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusNcluLexer.java",
        "CumulusNcluParser.java",
        "CumulusNcluParserBaseListener.java",
        "CumulusNcluParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CumulusNcluLexer.g4) \
       $(location CumulusNcluParser.g4) \
    -package org.batfish.grammar.cumulus_nclu \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
