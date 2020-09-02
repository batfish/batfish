load("@rules_java//java:defs.bzl", "java_library")
load("@batfish//skylark:junit.bzl", "junit_tests")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "bdd",
    srcs = glob([
        "src/main/java/**/*.java",
    ]),
    deps = [
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:org_apache_logging_log4j_log4j_api",
    ],
)

java_library(
    name = "bdd_testlib",
    srcs = glob(
        [
            "src/test/java/**/*.java",
        ],
        exclude = ["src/test/java/**/*Test.java"],
    ),
    deps = [
        ":bdd",
        "@maven//:junit_junit",
    ],
)

junit_tests(
    name = "bdd_tests",
    size = "small",
    srcs = glob([
        "src/test/java/**/*Test.java",
    ]),
    skip_pmd = True,
    deps = [
        ":bdd",
        ":bdd_testlib",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
