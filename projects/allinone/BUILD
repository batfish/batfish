package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:junit.bzl", "junit_tests")

java_binary(
    name = "allinone_main",
    main_class = "org.batfish.allinone.Main",
    runtime_deps = [
        ":allinone",
    ],
)

java_library(
    name = "allinone",
    srcs = glob([
        "src/main/**/*.java",
    ]),
    resources = glob(["src/main/resources/**"]),
    runtime_deps = [
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish-client:client",
        "//projects/batfish-common-protocol:common",
        "//projects/coordinator",
        "@guava//:compile",
        "@jaeger_core//:compile",
    ],
)

junit_tests(
    name = "AaaAuthenticationLoginTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/aaaauthenticationlogin/AaaAuthenticationLoginTest.java",
    ],
    resources = glob(["src/test/resources/org/batfish/allinone/testconfigs/*"]),
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "CompareSameNameTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/CompareSameNameTest.java",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "MultipathConsistencyTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/multipath/MultipathConsistencyTest.java",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "SpecifiersReachabilityTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/specifiers/SpecifiersReachabilityTest.java",
    ],
    resources = glob(["src/test/resources/org/batfish/allinone/testrigs/specifiers-reachability/**"]),
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "SearchFiltersDifferentialTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/searchfilters/SearchFiltersDifferentialTest.java",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "SearchFiltersTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/searchfilters/SearchFiltersTest.java",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "DifferentialReachabilityTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/differentialreachability/DifferentialReachabilityTest.java",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "TracerouteTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/traceroute/TracerouteTest.java",
    ],
    resources = glob(["src/test/resources/org/batfish/allinone/testrigs/specifiers-reachability/**"]),
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "smt_tests",
    size = "small",
    srcs = glob([
        "src/test/java/org/batfish/symbolic/smt/*.java",
    ]),
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)
