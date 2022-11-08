load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "cumulus_interfaces",
    srcs = [
        ":CumulusInterfacesLexer.java",
        ":CumulusInterfacesParser.java",
        ":CumulusInterfacesParserBaseListener.java",
        ":CumulusInterfacesParserListener.java",
    ],
    javacopts = ["-XepDisableAllChecks"],
    deps = [
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_interfaces/parsing:cumulus_interfaces_base",
        "@maven//:org_antlr_antlr4_runtime",
    ],
)

genrule(
    name = "cumulus_interfaces_generated",
    srcs = glob([
        "*.g4",
    ]),
    outs = [
        "CumulusInterfacesLexer.java",
        "CumulusInterfacesLexer.tokens",
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
