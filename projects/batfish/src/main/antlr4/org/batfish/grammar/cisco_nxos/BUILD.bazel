load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cisco_nxos",
    srcs = [
        ":CiscoNxosLexer.java",
        ":CiscoNxosParser.java",
        ":CiscoNxosParserBaseListener.java",
        ":CiscoNxosParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/cisco_nxos/parsing:cisco_nxos_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cisco_nxos_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CiscoNxosLexer.java",
        "CiscoNxosLexer.tokens",
        "CiscoNxosParser.java",
        "CiscoNxosParserBaseListener.java",
        "CiscoNxosParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CiscoNxosLexer.g4) \
       $(location CiscoNxosParser.g4) \
    -package org.batfish.grammar.cisco_nxos \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
