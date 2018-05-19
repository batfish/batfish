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

#cmdfile_test(
#    name = "basic_test",
#    cmdfile = "tests/basic/commands",
#)
