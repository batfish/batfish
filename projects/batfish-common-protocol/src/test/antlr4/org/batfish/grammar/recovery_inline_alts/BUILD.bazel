load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "recovery_inline_alts",
    testonly = True,
    srcs = [
        ":RecoveryInlineAltsLexer.java",
        ":RecoveryInlineAltsParser.java",
        ":RecoveryInlineAltsParserBaseListener.java",
        ":RecoveryInlineAltsParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "recovery_inline_alts_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "RecoveryInlineAltsLexer.java",
        "RecoveryInlineAltsParser.java",
        "RecoveryInlineAltsParserBaseListener.java",
        "RecoveryInlineAltsParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location RecoveryInlineAltsLexer.g4) \
       $(location RecoveryInlineAltsParser.g4) \
    -package org.batfish.grammar.recovery_inline_alts \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
