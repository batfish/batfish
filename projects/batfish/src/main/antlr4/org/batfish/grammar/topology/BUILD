package(default_visibility = ["//visibility:public"])

java_library(
    name = "topology",
    srcs = [
        ":GNS3TopologyLexer.java",
        ":GNS3TopologyParser.java",
        ":GNS3TopologyParserBaseListener.java",
        ":GNS3TopologyParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        "@antlr4//:compile",
    ],
)

genrule(
    name = "topology_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "GNS3TopologyLexer.java",
        "GNS3TopologyParser.java",
        "GNS3TopologyParserBaseListener.java",
        "GNS3TopologyParserListener.java",
    ],
    cmd = """
java -cp $(location //third_party/org/antlr4/antlr4-complete) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location GNS3TopologyLexer.g4) \
       $(location GNS3TopologyParser.g4) \
    -package org.batfish.grammar.topology \
    -encoding UTF-8 \
    -o $(@D)""",
    tools = ["//third_party/org/antlr4/antlr4-complete"],
)
