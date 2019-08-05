package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_interfaces",
    srcs = [
        ":CumulusInterfacesLexer.java",
        ":CumulusInterfacesParser.java",
        ":CumulusInterfacesParserBaseListener.java",
        ":CumulusInterfacesParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:parser_common",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_interfaces/parsing:cumulus_interfaces_base",
        "@antlr4_runtime//:compile",
    ],
)

genrule(
    name = "cumulus_interfaces_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusInterfacesLexer.java",
        "CumulusInterfacesParser.java",
        "CumulusInterfacesParserBaseListener.java",
        "CumulusInterfacesParserListener.java",
    ],
    cmd = """
java -cp $(location @antlr4_tool//jar) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location CumulusInterfacesLexer.g4) \
       $(location CumulusInterfacesParser.g4) \
    -package org.batfish.grammar.cumulus_interfaces \
    -encoding UTF-8 \
    -Werror \
    -o $(@D)""",
    tools = ["@antlr4_tool//jar"],
)
