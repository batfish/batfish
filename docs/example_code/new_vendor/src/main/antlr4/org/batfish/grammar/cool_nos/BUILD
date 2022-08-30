load("@rules_java//java:defs.bzl", "java_library")

package(
    default_visibility = ["//docs/example_code:__subpackages__"],
)

java_library(
    name = "cool_nos",
    srcs = [
        ":CoolNosLexer.java",
        ":CoolNosParser.java",
        ":CoolNosParserBaseListener.java",
        ":CoolNosParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//docs/example_code/new_vendor/src/main/java/org/batfish/grammar/cool_nos/parsing",
        "//projects/batfish-common-protocol:parser_common",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cool_nos_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CoolNosLexer.java",
        "CoolNosLexer.tokens",
        "CoolNosParser.java",
        "CoolNosParserBaseListener.java",
        "CoolNosParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CoolNosLexer.g4) \
       $(location CoolNosParser.g4) \
    -package org.batfish.grammar.cool_nos \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
