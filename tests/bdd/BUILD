java_binary(
    name = "snapshotBddLeakTests",
    main_class = "tests.bdd.SnapshotBddLeakTests",
    testonly = True,
    srcs= ["SnapshotBddLeakTests.java"],
    deps = [
        "//projects/allinone",
        "//projects/bdd",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
         "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_apache_logging_log4j_log4j_slf4j_impl",
    ],
)
