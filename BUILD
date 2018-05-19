package(default_visibility = ["//visibility:public"])

load("//skylark:junit.bzl", "junit_tests")
#load("//skylark:cmdfile_test.bzl", "cmdfile_test")

java_plugin(
    name = "auto_service_plugin",
    processor_class = "com.google.auto.service.processor.AutoServiceProcessor",
    deps = [
        "@auto_service//:compile",
    ],
)

java_library(
    name = "batfish_grammars",
    srcs = glob(
        [
            "projects/batfish/target/generated-sources/**/*.java",
        ],
        exclude = [
            "projects/batfish/target/generated-sources/**/Cisco*.java",
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
        "projects/batfish/src/main/**/*.java",
    ]),
    plugins = [
        ":auto_service_plugin",
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
            "projects/batfish/src/test/**/*.java",
        ],
        exclude = ["projects/batfish/src/test/**/*Test.java"],
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
        "projects/batfish/src/test/**/*Test.java",
    ]),
    resources = glob([
        "projects/batfish/src/test/resources/**",
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

java_binary(
    name = "coordinator_main",
    main_class = "org.batfish.coordinator.Main",
    runtime_deps = [
        ":coordinator",
        "@slf4j_jdk14//:runtime",
    ],
)

java_library(
    name = "coordinator",
    srcs = glob([
        "projects/coordinator/src/main/**/*.java",
    ]),
    resources = glob(["projects/coordinator/src/main/resources/**"]),
    runtime_deps = [
        "//projects/question:question",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/question:question",
        "@azure_storage//:compile",
        "@commons_io//:compile",
        "@commons_lang3//:compile",
        "@grizzly_server//:compile",
        "@guava//:compile",
        "@jackson_core//:compile",
        "@jackson_databind//:compile",
        "@jaeger_core//:compile",
        "@javax_annotation//:compile",
        "@jaxrs_api//:compile",
        "@jersey_common//:compile",
        "@jersey_container_grizzly2//:compile",
        "@jersey_media_jackson//:compile",
        "@jersey_media_jettison//:compile",
        "@jersey_media_multipart//:compile",
        "@jersey_server//:compile",
        "@jettison//:compile",
        "@jsr305//:compile",
        "@opentracing_api//:compile",
        "@opentracing_contrib_jaxrs//:compile",
        "@opentracing_util//:compile",
    ],
)

java_library(
    name = "coordinator_testlib",
    testonly = True,
    srcs = glob(
        [
            "projects/coordinator/src/test/**/*.java",
        ],
        exclude = ["projects/coordinator/src/test/**/*Test.java"],
    ),
    resources = glob(["projects/coordinator/src/test/resources/**"]),
    deps = [
        ":coordinator",
    ],
)

junit_tests(
    name = "coordinator_tests",
    srcs = glob([
        "projects/coordinator/src/test/java/**/*Test.java",
    ]),
    runtime_deps = [
        "@jersey_test_framework_grizzly2//:compile",
        "@sqlite_jdbc//:compile",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        ":coordinator",
        ":coordinator_testlib",
        "//projects/question:question",
        "@commons_io//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
        "@jackson_core//:compile",
        "@jaxrs_api//:compile",
        "@jersey_client//:compile",
        "@jersey_media_jackson//:compile",
        "@jersey_server//:compile",
        "@jersey_test_framework//:compile",
        "@jettison//:compile",
        "@junit//:compile",
    ],
)

java_binary(
    name = "client_main",
    main_class = "org.batfish.client.Main",
    runtime_deps = [
        ":client",
        "@slf4j_jdk14//:runtime",
    ],
)

java_library(
    name = "client",
    srcs = glob([
        "projects/batfish-client/src/main/**/*.java",
    ]),
    resources = glob(["projects/batfish-client/src/main/resources/**"]),
    runtime_deps = [
        "//projects/question:question",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        "@commons_io//:compile",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "//third_party/com/kjetland/mbknor-jackson-jsonschema_2.12:jackson_jsonschema",
        "@jackson_annotations//:compile",
        "@jackson_core//:compile",
        "@jackson_databind//:compile",
        "@jaeger_core//:compile",
        "@jaxrs_api//:compile",
        "@jersey_media_multipart//:compile",
        "@jettison//:compile",
        "@jline3//:compile",
        "@jsr305//:compile",
        "@opentracing_api//:compile",
        "@opentracing_util//:compile",
        "@scala_library//:compile",
    ],
)

junit_tests(
    name = "client_tests",
    srcs = glob([
        "projects/batfish-client/src/test/java/**/*Test.java",
    ]),
    runtime_deps = [
    ],
    deps = [
        ":client",
        "//projects/batfish-common-protocol:common",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@jackson_databind//:compile",
        "@jettison//:compile",
    ],
)

java_binary(
    name = "allinone_main",
    main_class = "org.batfish.allinone.Main",
    runtime_deps = [
        ":allinone",
    ],
)

java_library(
    name = "allinone",
    srcs = glob([
        "projects/allinone/src/main/**/*.java",
    ]),
    resources = glob(["projects/allinone/src/main/resources/**"]),
    runtime_deps = [
    ],
    deps = [
        ":batfish",
        ":client",
        "//projects/batfish-common-protocol:common",
        ":coordinator",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@jaeger_core//:compile",
    ],
)

genrule(
    name = "cisco_grammar",
    srcs = glob([
        "projects/batfish/src/main/antlr4/org/batfish/grammar/cisco/*.g4",
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
       $(location projects/batfish/src/main/antlr4/org/batfish/grammar/cisco/CiscoLexer.g4) \
       $(location projects/batfish/src/main/antlr4/org/batfish/grammar/cisco/CiscoParser.g4) \
    -package org.batfish.grammar.cisco \
    -encoding UTF-8 \
    -o $(@D)""",
    tools = ["//third_party/org/antlr4/antlr4-complete"],
)

#cmdfile_test(
#    name = "basic_test",
#    cmdfile = "tests/basic/commands",
#)
