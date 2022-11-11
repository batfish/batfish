load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_nclu",
    srcs = [
        ":CumulusNcluLexer.java",
        ":CumulusNcluParser.java",
        ":CumulusNcluParserBaseListener.java",
        ":CumulusNcluParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_nclu/parsing:cumulus_nclu_base_parser",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cumulus_nclu_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusNcluLexer.java",
        "CumulusNcluLexer.tokens",
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
