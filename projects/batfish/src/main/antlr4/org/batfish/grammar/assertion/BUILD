package(default_visibility = ["//visibility:public"])

java_library(
    name = "assertion",
    srcs = [
        ":AssertionLexer.java",
        ":AssertionParser.java",
        ":AssertionParserBaseListener.java",
        ":AssertionParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        "@antlr4//:compile",
    ],
)

genrule(
    name = "assertion_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "AssertionLexer.java",
        "AssertionParser.java",
        "AssertionParserBaseListener.java",
        "AssertionParserListener.java",
    ],
    cmd = """
java -cp $(location //third_party/org/antlr4/antlr4-complete) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location AssertionLexer.g4) \
       $(location AssertionParser.g4) \
    -package org.batfish.grammar.assertion \
    -encoding UTF-8 \
    -o $(@D)""",
    tools = ["//third_party/org/antlr4/antlr4-complete"],
)
