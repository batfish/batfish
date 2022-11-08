load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cisco_asa",
    srcs = [
        ":AsaLexer.java",
        ":AsaParser.java",
        ":AsaParserBaseListener.java",
        ":AsaParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/cisco_asa/parsing:cisco_asa_base_parser",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cisco_asa_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "AsaLexer.java",
        "AsaLexer.tokens",
        "AsaParser.java",
        "AsaParserBaseListener.java",
        "AsaParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location AsaLexer.g4) \
       $(location AsaParser.g4) \
    -package org.batfish.grammar.cisco_asa \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
