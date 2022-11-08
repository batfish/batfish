load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "frr",
    srcs = [
        ":FrrLexer.java",
        ":FrrParser.java",
        ":FrrParserBaseListener.java",
        ":FrrParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/frr/parsing:frr_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "frr_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "FrrLexer.java",
        "FrrLexer.tokens",
        "FrrParser.java",
        "FrrParserBaseListener.java",
        "FrrParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location FrrLexer.g4) \
       $(location FrrParser.g4) \
    -package org.batfish.grammar.frr \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
