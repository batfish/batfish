load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "grammar",
    srcs = [
        ":SonicLexer.java",
        ":SonicParser.java",
        ":SonicParserBaseListener.java",
        ":SonicParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "grammar_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "SonicLexer.java",
        "SonicLexer.tokens",
        "SonicParser.java",
        "SonicParserBaseListener.java",
        "SonicParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location SonicLexer.g4) \
       $(location SonicParser.g4) \
    -package org.batfish.vendor.sonic.grammar \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
