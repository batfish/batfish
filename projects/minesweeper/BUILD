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
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_auto_service_auto_service_annotations",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_commons_commons_lang3",
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
        "@maven//:org_slf4j_slf4j_jdk14",
    ],
    deps = [
        ":minesweeper",
        "@maven//:org_hamcrest_hamcrest",
    ],
)

junit_tests(
    name = "minesweeper_tests",
    size = "small",
    srcs = glob([
        "src/test/java/**/*Test.java",
    ]),
    runtime_deps = [
        "@maven//:org_slf4j_slf4j_jdk14",
    ],
    deps = [
        ":minesweeper",
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/bdd",
        "//projects/lib/z3",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_guava_guava_testlib",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest",
    ],
)

load("@batfish//skylark:pmd_test.bzl", "pmd_test")

pmd_test(
    name = "pmd",
    lib = ":minesweeper",
)
