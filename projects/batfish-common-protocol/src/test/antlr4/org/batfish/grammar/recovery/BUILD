load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "recovery",
    testonly = True,
    srcs = [
        ":RecoveryLexer.java",
        ":RecoveryParser.java",
        ":RecoveryParserBaseListener.java",
        ":RecoveryParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "recovery_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "RecoveryLexer.java",
        "RecoveryParser.java",
        "RecoveryParserBaseListener.java",
        "RecoveryParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location RecoveryLexer.g4) \
       $(location RecoveryParser.g4) \
    -package org.batfish.grammar.recovery \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
