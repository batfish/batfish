package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

load("@batfish//skylark:junit.bzl", "junit_tests")

junit_tests(
    name = "tests",
    srcs = glob([
        "**/*Test.java",
    ]),
    resources = [
        "//projects/batfish/src/test/resources/org/batfish/grammar/cumulus_nclu/testconfigs",
        "//projects/batfish/src/test/resources/org/batfish/grammar/cumulus_nclu/testrigs",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/batfish/src/main/java/org/batfish/grammar/cumulus_nclu",
        "//projects/batfish/src/main/java/org/batfish/representation/cumulus",
        "@antlr4_runtime//:compile",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)
