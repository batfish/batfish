load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "f5_bigip_imish",
    srcs = [
        ":F5BigipImishLexer.java",
        ":F5BigipImishParser.java",
        ":F5BigipImishParserBaseListener.java",
        ":F5BigipImishParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/f5_bigip_imish/parsing:f5_bigip_imish_base_parser",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "f5_bigip_imish_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "F5BigipImishLexer.java",
        "F5BigipImishLexer.tokens",
        "F5BigipImishParser.java",
        "F5BigipImishParserBaseListener.java",
        "F5BigipImishParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location F5BigipImishLexer.g4) \
       $(location F5BigipImishParser.g4) \
    -package org.batfish.grammar.f5_bigip_imish \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
