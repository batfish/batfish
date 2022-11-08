load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "palo_alto",
    srcs = [
        ":PaloAltoLexer.java",
        ":PaloAltoParser.java",
        ":PaloAltoParserBaseListener.java",
        ":PaloAltoParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/palo_alto/parsing:palo_alto_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "palo_alto_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "PaloAltoLexer.java",
        "PaloAltoLexer.tokens",
        "PaloAltoParser.java",
        "PaloAltoParserBaseListener.java",
        "PaloAltoParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location PaloAltoLexer.g4) \
       $(location PaloAltoParser.g4) \
    -package org.batfish.grammar.palo_alto \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
