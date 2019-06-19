package(default_visibility = ["//visibility:public"])

java_library(
    name = "cisco",
    srcs = [
        ":CiscoLexer.java",
        ":CiscoParser.java",
        ":CiscoParserBaseListener.java",
        ":CiscoParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/cisco/parsing:cisco_base_parser",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "cisco_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CiscoLexer.java",
        "CiscoParser.java",
        "CiscoParserBaseListener.java",
        "CiscoParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CiscoLexer.g4) \
       $(location CiscoParser.g4) \
    -package org.batfish.grammar.cisco \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
