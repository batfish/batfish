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
        "//projects/batfish-common-protocol/src/test/java/org/batfish/datamodel/matchers",
        "//projects/batfish/src/main/java/org/batfish/grammar/cisco_nxos",
        "//projects/batfish/src/main/java/org/batfish/representation/cisco_nxos",
        "//projects/bdd",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_antlr_antlr4_runtime",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
