package(default_visibility = ["//visibility:public"])

load("//skylark:junit.bzl", "junit_tests")

java_library(
    name = "common",
    srcs = glob([
        "src/main/**/*.java",
    ]),
    resources = glob(["src/main/resources/**"]),
    runtime_deps = [
        "@commons_beanutils//:runtime",
    ],
    deps = [
        "@antlr4//:compile",
        "@commons_cli//:compile",
        "@commons_configuration2//:compile",
        "@commons_io//:compile",
        "@commons_lang3//:compile",
        "@errorprone_annotations//:compile",
        "@grizzly_framework//:compile",
        "@grizzly_server//:compile",
        "@guava//:compile",
        "//projects/lib/jsonpath",
        "//third_party/com/kjetland/mbknor-jackson-jsonschema_2.12:jackson_jsonschema",
        "//projects/lib/z3",
        "@jackson_annotations//:compile",
        "@jackson_core//:compile",
        "@jackson_databind//:compile",
        "@jackson_guava//:compile",
        "@jackson_jdk8//:compile",
        "@jackson_jsr310//:compile",
        "@jaxrs_api//:compile",
        "@jersey_container_grizzly2//:compile",
        "@jersey_server//:compile",
        "@jettison//:compile",
        "@jsonassert//:compile",
        "@jsr305//:compile",
        "@lz4//:compile",
        "@maven_artifact//:compile",
        "@opentracing_api//:compile",
        "@opentracing_contrib_jaxrs//:compile",
        "@opentracing_util//:compile",
        "@xstream//:compile",
    ],
)

java_library(
    name = "common_testlib",
    testonly = True,
    srcs = glob(
        [
            "src/test/java/**/*.java",
            "target/generated-test-sources/**/*.java",
        ],
        exclude = ["src/test/java/**/*Test.java"],
    ),
    deps = [
        ":common",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
        "@jsr305//:compile",
        "@junit//:compile",
    ],
)

junit_tests(
    name = "common_tests",
    srcs = glob([
        "src/test/java/**/*Test.java",
    ]),
    resources = glob([
        "src/test/resources/**",
    ]),
    deps = [
        ":common",
        ":common_testlib",
        "@antlr4_runtime//:compile",
        "@guava//:compile",
        "@guava_testlib//:compile",
        "@hamcrest//:compile",
        "@jackson_annotations//:compile",
        "@jackson_core//:compile",
        "@jackson_databind//:compile",
        "@opentracing_api//:compile",
        "@opentracing_mock//:compile",
        "@opentracing_util//:compile",
    ],
)
