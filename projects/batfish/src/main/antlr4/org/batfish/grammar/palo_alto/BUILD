package(default_visibility = ["//visibility:public"])

java_library(
    name = "palo_alto",
    srcs = [
        ":PaloAltoLexer.java",
        ":PaloAltoParser.java",
        ":PaloAltoParserBaseListener.java",
        ":PaloAltoParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "palo_alto_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "PaloAltoLexer.java",
        "PaloAltoParser.java",
        "PaloAltoParserBaseListener.java",
        "PaloAltoParserListener.java",
    ],
    cmd = """
java -cp $(location //third_party/org/antlr4/antlr4-complete) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location PaloAltoLexer.g4) \
       $(location PaloAltoParser.g4) \
    -package org.batfish.grammar.palo_alto \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["//third_party/org/antlr4/antlr4-complete"],
)
