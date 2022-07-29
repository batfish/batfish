load("@rules_jmh//:defs.bzl", "jmh_java_benchmarks")

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