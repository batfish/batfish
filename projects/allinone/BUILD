package(default_visibility = ["//visibility:public"])

load("@batfish//skylark:junit.bzl", "junit_tests")

java_binary(
    name = "allinone_main",
    main_class = "org.batfish.allinone.Main",
    runtime_deps = [
        ":allinone",
        "@slf4j_jdk14//:runtime",
    ],
)

java_library(
    name = "allinone",
    srcs = glob([
        "src/main/**/*.java",
    ]),
    resources = glob(["src/main/resources/**"]),
    runtime_deps = [
        "//projects/minesweeper",
        "//projects/question",
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
    name = "DetectLoopsTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/loop/DetectLoopsTest.java",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/question",
        "@hamcrest//:compile",
    ],
)

junit_tests(
    name = "F5BigipVipConfigurationTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/f5_bigip/F5BigipVipConfigurationTest.java",
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
    name = "TracerouteAnswererHelperTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/traceroute/TracerouteAnswererHelperTest.java",
    ],
    resources = glob(["src/test/resources/org/batfish/allinone/testrigs/specifiers-reachability/**"]),
    deps = [
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
    name = "TraceroutePolicyBasedRoutingTest",
    size = "small",
    srcs = [
        "src/test/java/org/batfish/question/traceroute/TraceroutePolicyBasedRoutingTest.java",
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
    name = "smt_tests",
    size = "small",
    srcs = glob([
        "src/test/java/org/batfish/minesweeper/smt/*.java",
    ]),
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol:common_testlib",
        "//projects/minesweeper",
        "//projects/minesweeper:matchers",
        "//projects/question",
        "@guava//:compile",
        "@hamcrest//:compile",
    ],
)
