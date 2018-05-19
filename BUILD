package(default_visibility = ["//visibility:public"])

load("//skylark:junit.bzl", "junit_tests")
#load("//skylark:cmdfile_test.bzl", "cmdfile_test")

java_plugin(
    name = "auto_service_plugin",
    processor_class = "com.google.auto.service.processor.AutoServiceProcessor",
    deps = [
        "@auto_service//:compile",
    ],
)

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
        "projects/allinone/src/main/**/*.java",
    ]),
    resources = glob(["projects/allinone/src/main/resources/**"]),
    runtime_deps = [
    ],
    deps = [
        "//projects/batfish:batfish",
        "//projects/batfish-client:client",
        "//projects/batfish-common-protocol:common",
        "//projects/coordinator:coordinator",
        "@commons_lang3//:compile",
        "@guava//:compile",
        "@jaeger_core//:compile",
    ],
)

#cmdfile_test(
#    name = "basic_test",
#    cmdfile = "tests/basic/commands",
#)
