package(default_visibility = ["//visibility:public"])

load("//skylark:junit.bzl", "junit_tests")

java_library(
    name = "batfish_grammars",
    srcs = glob(
        [
            "target/generated-sources/**/*.java",
        ],
        exclude = [
            "target/generated-sources/**/Cisco*.java",
        ],
    ) + [
        ":CiscoLexer.java",
        ":CiscoParser.java",
        ":CiscoParserBaseListener.java",
        ":CiscoParserListener.java",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        "@antlr4//:compile",
    ],
)

java_binary(
    name = "batfish_main",
    main_class = "org.batfish.main.Driver",
    runtime_deps = [
        ":batfish",
    ],
)

java_library(
    name = "batfish",
    srcs = glob([
        "src/main/**/*.java",
    ]),
    plugins = [
        "//:auto_service_plugin",
    ],
    runtime_deps = [
        "//projects/question:question",
    ],
    deps = [
        ":batfish_grammars",
        "//projects/batfish-common-protocol:common",
        "//projects/question:question",
        "@antlr4//:compile",
        "@auto_service//:compile",
        "@commons_collections4//:compile",
        "@commons_configuration2//:compile",
        "@commons_lang3//:compile",
        "@grizzly_server//:compile",
        "@guava//:compile",
        "//third_party/com/microsoft/z3",
        "//third_party/net/sf/javabdd/bdd",
        "@jackson_annotations//:compile",
        "@jackson_core//:compile",
        "@jackson_databind//:compile",
        "@jaeger_core//:compile",
        "@jaxrs_api//:compile",
        "@jersey_common//:compile",
        "@jersey_container_grizzly2//:compile",
        "@jersey_media_jettison//:compile",
        "@jersey_server//:compile",
        "@jettison//:compile",
        "@jgrapht//:compile",
        "@jsr305//:compile",
        "@lz4//:compile",
        "@opentracing_api//:compile",
        "@opentracing_contrib_jaxrs//:compile",
        "@opentracing_util//:compile",
    ],
)

java_library(
    name = "batfish_testlib",
    testonly = True,
    srcs = glob(
        [
            "src/test/**/*.java",
        ],
        exclude = ["src/test/**/*Test.java"],
    ),
    deps = [
        ":batfish",
        ":batfish_grammars",
        "//projects/batfish-common-protocol:common",
        "@antlr4_runtime//:compile",
        "@commons_collections4//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
        "//third_party/com/microsoft/z3",
        "@jsr305//:compile",
        "@junit//:compile",
    ],
)

junit_tests(
    name = "batfish_tests",
    srcs = glob([
        "src/test/**/*Test.java",
    ]),
    resources = glob([
        "src/test/resources/**",
    ]),
    deps = [
        ":batfish",
        ":batfish_grammars",
        ":batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
        "//third_party/com/microsoft/z3",
        "//third_party/net/sf/javabdd/bdd",
        "@jackson_databind//:compile",
        "@jettison//:compile",
        "@junit//:compile",
    ],
)

genrule(
    name = "cisco_grammar",
    srcs = glob([
        "src/main/antlr4/org/batfish/grammar/cisco/*.g4",
    ]),
    outs = [
        "CiscoLexer.java",
        "CiscoParser.java",
        "CiscoParserBaseListener.java",
        "CiscoParserListener.java",
    ],
    cmd = """
java -cp $(location //third_party/org/antlr4/antlr4-complete) \
    org.antlr.v4.Tool \
    -Xexact-output-dir \
       $(location src/main/antlr4/org/batfish/grammar/cisco/CiscoLexer.g4) \
       $(location src/main/antlr4/org/batfish/grammar/cisco/CiscoParser.g4) \
    -package org.batfish.grammar.cisco \
    -encoding UTF-8 \
    -o $(@D)""",
    tools = ["//third_party/org/antlr4/antlr4-complete"],
)

