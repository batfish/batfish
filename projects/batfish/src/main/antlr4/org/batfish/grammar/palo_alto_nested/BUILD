load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "palo_alto_nested",
    srcs = [
        ":PaloAltoNestedLexer.java",
        ":PaloAltoNestedParser.java",
        ":PaloAltoNestedParserBaseListener.java",
        ":PaloAltoNestedParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/palo_alto_nested/parsing:palo_alto_nested_base",
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "palo_alto_nested_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "PaloAltoNestedLexer.java",
        "PaloAltoNestedLexer.tokens",
        "PaloAltoNestedParser.java",
        "PaloAltoNestedParserBaseListener.java",
        "PaloAltoNestedParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location PaloAltoNestedLexer.g4) \
       $(location PaloAltoNestedParser.g4) \
    -package org.batfish.grammar.palo_alto_nested \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
