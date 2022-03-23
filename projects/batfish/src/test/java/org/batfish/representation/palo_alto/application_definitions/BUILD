load("@batfish//skylark:junit.bzl", "junit_tests")

package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

junit_tests(
    name = "tests",
    srcs = glob([
        "**/*Test.java",
    ]),
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/batfish-common-protocol/src/test/java/org/batfish/common/matchers",
        "//projects/batfish/src/main/java/org/batfish/representation/palo_alto",
        "//projects/batfish/src/main/java/org/batfish/representation/palo_alto/application_definitions",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
