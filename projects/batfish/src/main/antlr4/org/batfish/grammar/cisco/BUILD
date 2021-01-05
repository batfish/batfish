load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cisco",
    srcs = [
        ":CiscoLexer.java",
        ":CiscoParser.java",
        ":CiscoParserBaseListener.java",
        ":CiscoParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/cisco/parsing:cisco_base_parser",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cisco_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CiscoLexer.java",
        "CiscoLexer.tokens",
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
