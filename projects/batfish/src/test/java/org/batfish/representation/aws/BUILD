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
    resources = ["//projects/batfish/src/test/resources/org/batfish/representation/aws"],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/batfish/src/main/java/org/batfish/representation/aws",
        "//projects/batfish/src/test/java/org/batfish/representation/aws/matchers",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_guava_guava_testlib",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
