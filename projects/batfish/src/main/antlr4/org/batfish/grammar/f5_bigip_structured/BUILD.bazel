load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "f5_bigip_structured",
    srcs = [
        ":F5BigipStructuredLexer.java",
        ":F5BigipStructuredParser.java",
        ":F5BigipStructuredParserBaseListener.java",
        ":F5BigipStructuredParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/f5_bigip_structured/parsing:f5_bigip_structured_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "f5_bigip_structured_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "F5BigipStructuredLexer.java",
        "F5BigipStructuredLexer.tokens",
        "F5BigipStructuredParser.java",
        "F5BigipStructuredParserBaseListener.java",
        "F5BigipStructuredParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location F5BigipStructuredLexer.g4) \
       $(location F5BigipStructuredParser.g4) \
    -package org.batfish.grammar.f5_bigip_structured \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
