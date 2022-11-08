load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_ports",
    srcs = [
        ":CumulusPortsLexer.java",
        ":CumulusPortsParser.java",
        ":CumulusPortsParserBaseListener.java",
        ":CumulusPortsParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_ports/parsing:cumulus_ports_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cumulus_ports_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusPortsLexer.java",
        "CumulusPortsLexer.tokens",
        "CumulusPortsParser.java",
        "CumulusPortsParserBaseListener.java",
        "CumulusPortsParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CumulusPortsLexer.g4) \
       $(location CumulusPortsParser.g4) \
    -package org.batfish.grammar.cumulus_ports \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
