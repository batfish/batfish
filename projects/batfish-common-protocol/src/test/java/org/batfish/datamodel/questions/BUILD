load("@batfish//skylark:junit.bzl", "junit_tests")

package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

java_library(
    name = "TestQuestion",
    testonly = True,
    srcs = glob(
        [
            "TestQuestion.java",
        ],
    ),
    deps = [
        "//projects/batfish-common-protocol:common",
    ],
)

junit_tests(
    name = "tests",
    srcs = glob([
        "**/*Test.java",
    ]),
    resources = [
        "//projects/batfish-common-protocol/src/test/resources",
    ],
    deps = [
        "//projects/batfish-common-protocol:common",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_guava_guava_testlib",
        "@maven//:junit_junit",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
