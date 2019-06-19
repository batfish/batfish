package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:junit.bzl", "junit_tests")

java_library(
    name = "minesweeper",
    srcs = glob([
        "src/main/**/*.java",
    ]),
    plugins = [
        "//:auto_service_plugin",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/bdd",
        "//projects/lib/z3",
        "//projects/question",
        "@auto_service//:compile",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@jackson_annotations//:compile",
        "@jsr305//:compile",
    ],
)

java_library(
    name = "matchers",
    testonly = True,
    srcs = glob(
        [
            "src/test/java/**/*.java",
        ],
        exclude = ["src/test/java/**/*Test.java"],
    ),
    runtime_deps = [
        "@slf4j_jdk14//:runtime",
    ],
    deps = [
        ":minesweeper",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "minesweeper_tests",
    size = "small",
    srcs = glob([
        "src/test/java/**/*Test.java",
    ]),
    runtime_deps = [
        "@slf4j_jdk14//:runtime",
    ],
    deps = [
        ":minesweeper",
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/bdd",
        "//projects/lib/z3",
        "@guava//:compile",
        "@guava_testlib//:compile",
        "@hamcrest//:compile",
        "@junit//:compile",
    ],
)
