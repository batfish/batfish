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
    deps = [
        "//projects/batfish-common-protocol:common",
        "//projects/batfish/src/main/java/org/batfish/representation/cumulus",
        "//projects/batfish/src/main/java/org/batfish/representation/cumulus_interfaces",
        "@maven//:com_google_guava_guava",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
