load("@batfish//skylark:jmh.bzl", "jmh_java_benchmarks")

jmh_java_benchmarks(
    name = "bdd_parallelism_stress_test",
    srcs = ["BDDParallelismStressTest.java"],
    deps = [
        "//projects/allinone",
        "//projects/bdd",
        "//projects/batfish-common-protocol:common",
         "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_apache_logging_log4j_log4j_slf4j_impl",
    ],
)

jmh_java_benchmarks(
    name = "benchmark",
    srcs = ["BDDBenchmark.java"],
    deps = [
        "//projects/allinone",
        "//projects/bdd",
        "//projects/batfish-common-protocol:common",
         "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_apache_logging_log4j_log4j_slf4j_impl",
    ],
)

jmh_java_benchmarks(
    name = "benchOrAll",
    srcs = ["BenchOrAll.java"],
    deps = [
        "//projects/allinone",
        "//projects/bdd",
        "//projects/batfish-common-protocol:common",
         "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_apache_logging_log4j_log4j_slf4j_impl",
    ],
)

jmh_java_benchmarks(
    name = "benchExampleNetworkReachabilityGraph",
    testonly = True,
    srcs = ["BenchExampleNetworkReachabilityGraph.java"],
    deps = [
        "//projects/allinone",
        "//projects/bdd",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
         "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_apache_logging_log4j_log4j_slf4j_impl",
    #    "@maven//:com_github_biboudis_jmh_profilers",
    #"@maven//:org_openjdk_jmh_jmh_core",
    #"@maven//:org_openjdk_jmh_jmh_generator_annprocess",
    ],
)