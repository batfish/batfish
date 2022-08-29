load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "recovery_rule_alts",
    testonly = True,
    srcs = [
        ":RecoveryRuleAltsLexer.java",
        ":RecoveryRuleAltsParser.java",
        ":RecoveryRuleAltsParserBaseListener.java",
        ":RecoveryRuleAltsParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "recovery_rule_alts_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "RecoveryRuleAltsLexer.java",
        "RecoveryRuleAltsParser.java",
        "RecoveryRuleAltsParserBaseListener.java",
        "RecoveryRuleAltsParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location RecoveryRuleAltsLexer.g4) \
       $(location RecoveryRuleAltsParser.g4) \
    -package org.batfish.grammar.recovery_rule_alts \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
