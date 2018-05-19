package(default_visibility = ["//visibility:public"])

load("//skylark:junit.bzl", "junit_tests")
#load("//skylark:cmdfile_test.bzl", "cmdfile_test")

java_library(
    name = "question",
    srcs = glob([
        "src/main/**/*.java",
    ]),
    plugins = [
        "//:auto_service_plugin",
    ],
    resources = glob(["src/main/resources/**"]),
    runtime_deps = [
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        "@auto_service//:compile",
        "//projects/lib/jsonpath",
        "@jackson_annotations//:compile",
        "@jackson_core//:compile",
        "@jackson_databind//:compile",
    ],
)

junit_tests(
    name = "question_tests",
    srcs = glob([
        "src/test/java/**/*Test.java",
    ]),
    resources = glob([
        "src/test/resources/**",
    ]),
    runtime_deps = [
        "@slf4j_jdk14//:runtime",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        ":question",
        "@guava//:compile",
        "@hamcrest//:compile",
        "//projects/lib/jsonpath",
        "@jackson_databind//:compile",
        "@junit//:compile",
    ],
)
