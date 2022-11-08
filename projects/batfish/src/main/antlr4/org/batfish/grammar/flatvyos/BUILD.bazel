load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "flatvyos",
    srcs = [
        ":FlatVyosLexer.java",
        ":FlatVyosParser.java",
        ":FlatVyosParserBaseListener.java",
        ":FlatVyosParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "flatvyos_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "FlatVyosLexer.java",
        "FlatVyosLexer.tokens",
        "FlatVyosParser.java",
        "FlatVyosParserBaseListener.java",
        "FlatVyosParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location FlatVyosLexer.g4) \
       $(location FlatVyosParser.g4) \
    -package org.batfish.grammar.flatvyos \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
