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
        "//projects/batfish:batfish",
        ":client",
        "//projects/batfish-common-protocol:common",
        ":coordinator",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@jaeger_core//:compile",
    ],
)

#cmdfile_test(
#    name = "basic_test",
#    cmdfile = "tests/basic/commands",
#)
