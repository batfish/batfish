package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:junit.bzl", "junit_tests")

java_library(
    name = "bdd",
    srcs = glob([
        "src/main/java/**/*.java",
    ]),
    deps = [
        "@maven//:com_google_code_findbugs_jsr305",
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
        "@maven//:org_hamcrest_hamcrest",
    ],
)

junit_tests(
    name = "bdd_tests",
    size = "small",
    srcs = glob([
        "src/test/java/**/*Test.java",
    ]),
    deps = [
        ":bdd",
        ":bdd_testlib",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
