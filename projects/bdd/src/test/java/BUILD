load("@rules_java//java:defs.bzl", "java_library")
load("@batfish//skylark:junit.bzl", "junit_tests")
load("@batfish//skylark:pmd_test.bzl", "pmd_test")

package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

java_library(
    name = "bdd_testlib",
    srcs = glob(
        ["**/*.java"],
        exclude = ["**/*Test.java"],
    ),
    deps = [
        "//projects/bdd",
        "@maven//:junit_junit",
    ],
)

pmd_test(
    name = "bdd_testlib_pmd",
    lib = ":bdd_testlib",
)

junit_tests(
    name = "bdd_tests",
    size = "small",
    srcs = glob(["**/*Test.java"]),
    skip_pmd = True,
    deps = [
        ":bdd_testlib",
        "//projects/bdd",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
