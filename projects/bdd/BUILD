package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:junit.bzl", "junit_tests")

java_library(
    name = "bdd",
    srcs = glob([
        "src/main/java/**/*.java",
    ]),
)

java_library(
    name = "bdd_testlib",
    srcs = glob(
        [
            "src/test/java/**/*.java",
        ],
        exclude = ["src/test/java/**/*Test.java"],
    ),
    deps = [":bdd", "@junit//:compile"],
)

junit_tests(
    name = "bdd_tests",
    size = "small",
    srcs = glob([
        "src/test/java/**/*Test.java",
    ]),
    deps = [":bdd", ":bdd_testlib",],
)
