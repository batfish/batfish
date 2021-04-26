load("@rules_java//java:defs.bzl", "java_library")
load("@batfish//skylark:pmd_test.bzl", "pmd_test")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "annotate",
    srcs = glob(
        ["**/*.java"],
    ),
    deps = [
        "//projects/batfish",
        "//projects/batfish-common-protocol:common",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_api",
    ],
)

pmd_test(
    name = "pmd",
    lib = ":annotate",
)
