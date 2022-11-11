load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cisco_xr",
    srcs = [
        ":CiscoXrLexer.java",
        ":CiscoXrParser.java",
        ":CiscoXrParserBaseListener.java",
        ":CiscoXrParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/cisco_xr/parsing:cisco_xr_base_parser",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cisco_xr_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CiscoXrLexer.java",
        "CiscoXrLexer.tokens",
        "CiscoXrParser.java",
        "CiscoXrParserBaseListener.java",
        "CiscoXrParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CiscoXrLexer.g4) \
       $(location CiscoXrParser.g4) \
    -package org.batfish.grammar.cisco_xr \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
