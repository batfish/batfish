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
        "//projects/batfish/src/test/resources/org/batfish/grammar/cisco_nxos/bgp",
        "//projects/batfish/src/test/resources/org/batfish/grammar/cisco_nxos/ospf",
        "//projects/batfish/src/test/resources/org/batfish/grammar/cisco_nxos/snapshots/runtime_data",
        "//projects/batfish/src/test/resources/org/batfish/grammar/cisco_nxos/testconfigs",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/batfish/src/main/java/org/batfish/grammar/cisco_nxos",
        "//projects/batfish/src/main/java/org/batfish/representation/cisco_nxos",
        "//projects/bdd",
        "@antlr4_runtime//:compile",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)
