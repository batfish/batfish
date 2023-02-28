load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "juniper",
    srcs = [
        ":JuniperLexer.java",
        ":JuniperParser.java",
        ":JuniperParserBaseListener.java",
        ":JuniperParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/juniper/parsing:juniper_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "juniper_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "JuniperLexer.java",
        "JuniperLexer.tokens",
        "JuniperParser.java",
        "JuniperParserBaseListener.java",
        "JuniperParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location JuniperLexer.g4) \
       $(location JuniperParser.g4) \
    -package org.batfish.grammar.juniper \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
