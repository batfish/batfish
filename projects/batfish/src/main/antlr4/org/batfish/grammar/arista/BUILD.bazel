load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "arista",
    srcs = [
        ":AristaLexer.java",
        ":AristaParser.java",
        ":AristaParserBaseListener.java",
        ":AristaParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/arista/parsing:arista_base_parser",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "arista_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "AristaLexer.tokens",
        "AristaLexer.java",
        "AristaParser.java",
        "AristaParserBaseListener.java",
        "AristaParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location AristaLexer.g4) \
       $(location AristaParser.g4) \
    -package org.batfish.grammar.arista \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
