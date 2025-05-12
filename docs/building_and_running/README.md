# Building and running Batfish and its tests

Batfish runs as a service that can be accessed via RESTful APIs.
A [Python client](https://github.com/batfish/pybatfish) comes bundled
with
the [suggested allinone Docker container](https://github.com/batfish/batfish#how-do-i-get-started)
or can be [set up separately](https://github.com/batfish/pybatfish#how-do-i-get-started).

## Prerequisites

- Java 17 JDK
- Python 3.9 or later (for Pybatfish)
- git
- [`bazelisk`](https://github.com/bazelbuild/bazelisk#installation)

We provide OS-specific advice below.

- `Ubuntu`: Read [Ubuntu notes](#ubuntu) first
- `macOS`: Read [macOS notes](#macos) first

### Operation-system specific prerequisite installation

### macOS

Do the following before doing anything

1. Install XCode command-line tools.

   - `xcode-select --install`

1. Install Homebrew. Follow [these steps](https://brew.sh/).

1. Open a fresh terminal to ensure the utilities are correctly picked up.

1. If you don't already have the Java 17 JDK installed, first install homebrew cask and then Java 17
   using the following commands.

   - `brew tap homebrew/cask-versions`
   - `brew install --cask temurin17`

1. If you don't already have it, install Bazelisk.
   - `brew install bazelisk`

### Ubuntu

Do the following before doing anything

1. Install Java 17 and corresponding debug symbols

   - `sudo apt install openjdk-17-jdk openjdk-17-dbg`

1. If you don't already have it, install `wget`:

   - `sudo apt-get install wget`

1. If you don't already have it, install Bazelisk.

   - Open the [bazelisk release page](https://github.com/bazelbuild/bazelisk/releases)
   - Copy the link for the `bazelisk-linux-amd64` release asset for the latest version
   - Download and install bazelisk using the copied link (example url below is for `v1.12.2`, but
     you should use the latest version):

     `wget -O- https://github.com/bazelbuild/bazelisk/releases/download/v1.12.2/bazelisk-linux-amd64 | sudo tee /usr/local/bin/bazelisk > /dev/null`

   - Make bazelisk executable:

     `sudo chmod +x /usr/local/bin/bazelisk`

   - Symlink bazel to bazelisk:

     `sudo ln -s bazelisk /usr/local/bin/bazel`

### Note: multiple versions of Java

If you have multiple versions of Java installed on your machine, the default `java`/`javac` commands may still not be
using JVM 17. In that case, you can force the version of Java in use by setting `JAVA_HOME`. @dhalperi has these aliases
in his `.zshrc` to control which Java is running in a given shell on macOS:

```sh
# Java options
# j11q switches to Java 11, quietly. Could make a loud version that runs `java -version` after.
function j11q() {
    export JAVA_HOME=`/usr/libexec/java_home -v 11`
}
function j17q() {
    export JAVA_HOME=`/usr/libexec/java_home -v 17`
}
# Default to Java 17.
j17q
```

## Installation steps

1. Check out the Batfish code
   - `git clone https://github.com/batfish/batfish.git`
   - `cd batfish`

## Running (simple)

1. To build any unbuilt changes and run the Batfish service:
   - `tools/bazel_run.sh`

## Running (advanced)

1. (Optional, done automatically in next step) Compile Batfish

   - ```
     bazel build //projects/allinone:allinone_main
     ```

1. Run the Batfish service

   - ```
     bazel run //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
     ```
     will start Batfish in service mode using the library of questions within the Batfish
     repository.

   Data stored by the service to analyze a network will be stored in a `containers` folder.

   If you wish to provide Java arguments, use the `--jvmopt` option once for each java argument in
   between `run` and `//projects/allinone:allinone_main`:

   To set the RAM:

   ```
   bazel run --jvmopt=-Xmx2g //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
   ```

   To turn on assertions and enable a debugger to attach:

   ```
   bazel run --jvmopt=-ea --jvmopt=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5009 //projects/allinone:allinone_main -- -runclient false -coordinatorargs "-templatedirs $(git rev-parse --show-toplevel)/questions -containerslocation $(git rev-parse --show-toplevel)/containers"
   ```

   For more info, see the Bazel documentation.

1. Explore using Pybatfish

   Once the service is running, you can
   use [Pybatfish](https://github.com/batfish/pybatfish) to analyze
   your network.

## Running tests

You can run all tests in this repository by running:

```
bazel test //...
```

The above is a specific instance of the general form for running all tests in all Bazel packages
in a subtree. The above uses the entire repository `//` as the root; to test only the `coordinator`:

```
bazel test //projects/coordinator/...
```

To run a specific test, just use its Bazel target:

```
bazel test //projects/batfish:pmd
```

You can also combine multiple test target expressions:

```
bazel test //projects/coordinator/... //projects/batfish:pmd
```

If you want to test just a single function of a rule of type `junit_test`, do:

```
bazel test --test_filter=<fully-qualified-class-name>#test-function$ -- <test-target-containing-test-class>
```

For example, to run just the `getNonExistNetwork` test function of the `WorkMgrServiceTest` class,
do:

```
bazel test --test_filter=org.batfish.coordinator.WorkMgrServiceTest#getNonExistNetwork$ -- //projects/coordinator:coordinator_tests
```

## Dependency management and upgrades

We use Bazel's [`rules_jvm_external`](https://github.com/bazelbuild/rules_jvm_external) to manage our Java dependencies
from Maven. Refer to that
project's [documentation](https://github.com/bazelbuild/rules_jvm_external#updating-maven_installjson)
to understand how it works, how Maven dependencies are versioned and captured in `maven_install.json`, and
other information.

Commonly, some Java library will have a CVE and we will want to upgrade our dependence on it. While the true reference
for upgrading should be
the [`rules_jvm_external` instructions on re-pinning](https://github.com/bazelbuild/rules_jvm_external#updating-maven_installjson),
here is a summary of the steps involved:

1. Edit [`library_deps.bzl`](https://github.com/batfish/batfish/blob/master/library_deps.bzl) to update the version of
   the library used to a non-vulnerable release.
2. Run [`bazel run @unpinned_maven//:pin`](https://github.com/bazelbuild/rules_jvm_external#updating-maven_installjson)
   to re-generate `maven_install.json`.
3. Manually review the changes (e.g., with `git diff`) to ensure all needed upgrades are achieved.
4. Run all the tests (e.g., `bazel test //...`) to ensure that Batfish still builds and the tests still pass.
5. Submit a PR to this repository as usual.

## Building a deployable batfish or allinone docker image

- Use the batfish image if you just want the batfish service.
- Use the allinone image if you want the batfish service and a built-in jupyter notebook server.

### Building images locally

This section of the doc is still in progress. Check back later!

### Building images via GitHub actions

This section of the doc is still in progress. Check back later!
