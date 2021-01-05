load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_frr",
    srcs = [
        ":CumulusFrrLexer.java",
        ":CumulusFrrParser.java",
        ":CumulusFrrParserBaseListener.java",
        ":CumulusFrrParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_frr/parsing:cumulus_frr_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cumulus_frr_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusFrrLexer.java",
        "CumulusFrrLexer.tokens",
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
