load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "grammar",
    srcs = [
        ":A10Lexer.java",
        ":A10Parser.java",
        ":A10ParserBaseListener.java",
        ":A10ParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/vendor/a10/grammar:a10_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "grammar_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "A10Lexer.java",
        "A10Lexer.tokens",
        "A10Parser.java",
        "A10ParserBaseListener.java",
        "A10ParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location A10Lexer.g4) \
       $(location A10Parser.g4) \
    -package org.batfish.vendor.a10.grammar \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
