load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "fortios",
    srcs = [
        ":FortiosLexer.java",
        ":FortiosParser.java",
        ":FortiosParserBaseListener.java",
        ":FortiosParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/fortios/parsing:fortios_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "fortios_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "FortiosLexer.java",
        "FortiosLexer.tokens",
        "FortiosParser.java",
        "FortiosParserBaseListener.java",
        "FortiosParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location FortiosLexer.g4) \
       $(location FortiosParser.g4) \
    -package org.batfish.grammar.fortios \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
