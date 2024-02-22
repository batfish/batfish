workspace(name = "batfish")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")

##########################################################
# Bazel helpers                                          #
##########################################################
http_archive(
    name = "bazel_skylib",
    sha256 = "cd55a062e763b9349921f0f5db8c3933288dc8ba4f76dd9416aac68acee3cb94",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.5.0/bazel-skylib-1.5.0.tar.gz",
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.5.0/bazel-skylib-1.5.0.tar.gz",
    ],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

##########################################################
# External Java dependencies (from Maven)                #
##########################################################

RULES_JVM_EXTERNAL_TAG = "6.0"

RULES_JVM_EXTERNAL_SHA = "85fd6bad58ac76cc3a27c8e051e4255ff9ccd8c92ba879670d195622e7c0a9b7"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/releases/download/%s/rules_jvm_external-%s.tar.gz" % (RULES_JVM_EXTERNAL_TAG, RULES_JVM_EXTERNAL_TAG),
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")
load(":library_deps.bzl", "BATFISH_MAVEN_ARTIFACTS")

maven_install(
    artifacts = BATFISH_MAVEN_ARTIFACTS,
    excluded_artifacts = ["org.hamcrest:hamcrest-core"],
    fetch_sources = True,
    maven_install_json = "@batfish//:maven_install.json",
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
    strict_visibility = True,
)

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

##########################################################
## GPL'ed JOL tool
load("//tools/jol:jol.bzl", "setup_jol_maven")

setup_jol_maven()

load("@jol_maven//:defs.bzl", jol_pinned_maven_install = "pinned_maven_install")

jol_pinned_maven_install()

##########################################################
## GPL'ed JMH tool
load("//tools/benchmarks:jmh.bzl", "setup_jmh_maven")

setup_jmh_maven()

load("@jmh_maven//:defs.bzl", jmh_pinned_maven_install = "pinned_maven_install")

jmh_pinned_maven_install()

##########################################################
## Python setup

http_archive(
    name = "rules_python",
    sha256 = "c68bdc4fbec25de5b5493b8819cfc877c4ea299c0dcb15c244c5a00208cde311",
    strip_prefix = "rules_python-0.31.0",
    url = "https://github.com/bazelbuild/rules_python/releases/download/0.31.0/rules_python-0.31.0.tar.gz",
)

load("@rules_python//python:repositories.bzl", "py_repositories", "python_register_toolchains")

py_repositories()

python_register_toolchains(
    name = "python3_9",
    # Available versions are listed in @rules_python//python:versions.bzl.
    python_version = "3.9",
)

load("@python3_9//:defs.bzl", "interpreter")
load("@rules_python//python:pip.bzl", "pip_parse")

pip_parse(
    name = "pip",
    python_interpreter_target = interpreter,
    requirements_lock = "//python:requirements.txt",
)

load("@pip//:requirements.bzl", "install_deps")

install_deps()

##########################################################
## Third section: tools

# ANTLR4 tool
http_jar(
    name = "antlr4_tool",
    sha256 = "6852386d7975eff29171dae002cc223251510d35f291ae277948f381a7b380b4",
    url = "https://search.maven.org/remotecontent?filepath=org/antlr/antlr4/4.7.2/antlr4-4.7.2-complete.jar",
)

# Buildifier
http_archive(
    name = "buildifier_prebuilt",
    sha256 = "8ada9d88e51ebf5a1fdff37d75ed41d51f5e677cdbeafb0a22dda54747d6e07e",
    strip_prefix = "buildifier-prebuilt-6.4.0",
    urls = [
        "https://github.com/keith/buildifier-prebuilt/archive/6.4.0.tar.gz",
    ],
)

load("@buildifier_prebuilt//:deps.bzl", "buildifier_prebuilt_deps")

buildifier_prebuilt_deps()

bazel_skylib_workspace()

load("@buildifier_prebuilt//:defs.bzl", "buildifier_prebuilt_register_toolchains")

buildifier_prebuilt_register_toolchains()
