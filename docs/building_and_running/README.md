# Building and running Batfish and its tests

Batfish runs as a service that can be accessed via RESTful APIs.
A [Python client](https://github.com/batfish/pybatfish) comes bundled
with
the [suggested allinone Docker container](https://github.com/batfish/batfish#how-do-i-get-started)
or can be [set up separately](https://github.com/batfish/pybatfish#how-do-i-get-started).

## Prerequisites

- Java 11 JDK
- git
- [bazelisk](https://github.com/bazelbuild/bazelisk#installation)

We provide OS-specific advice below.

* `Ubuntu`: Read [Ubuntu notes](#ubuntu) first
* `macOS`: Read [macOS notes](#macos) first

### Operation-system specific prerequisite installation

### macOS

Do the following before doing anything

1. Install [XCode from Apple Store](https://itunes.apple.com/us/app/xcode/id497799835)

2. Install XCode command-line tools.
    - `xcode-select --install`

3. Install Homebrew. Follow [these steps](https://brew.sh/).

4. Open a fresh terminal to ensure the utilities are correctly picked up.

5. If you don't already have the Java 11 JDK installed, first install homebrew cask and then Java 11
   using the following commands.
    - `brew tap homebrew/cask-versions`
    - `brew install --cask temurin11`

6. If you don't already have it, install Bazelisk.
    - `brew install bazelisk`

### Ubuntu

Do the following before doing anything

1. Install Java 11 and corresponding debug symbols
    - `sudo apt install openjdk-11-jdk openjdk-11-dbg`

2. If you don't already have it, install Bazelisk.
    - Open the [bazelisk release page](https://github.com/bazelbuild/bazelisk/releases)
    - Copy the link for the `bazelisk-linux-amd64` release asset for the latest version
    - Download and install bazelisk using the copied link (example url below is for `v1.12.2`, but
      you should use the latest version):

      `curl -s https://github.com/bazelbuild/bazelisk/releases/download/v1.12.2/bazelisk-linux-amd64 | sudo tee /usr/local/bin/bazelisk`
    - Make bazelisk executable:

      `sudo chmod +x /usr/local/bin/bazelisk`
    - Symlink bazel to bazelisk:

      `sudo ln -s bazelisk /usr/local/bin/bazel`

## Installation steps

1. Check out the Batfish code
    - `git clone https://github.com/batfish/batfish.git`
    - `cd batfish`

2. Compile Batfish
    - `bazel build //projects/allinone:allinone_main`

3. Run the Batfish service
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

4. Explore using Pybatfish

   Once the service is running, you can
   use [Pybatfish](https://github.com/batfish/pybatfish) to analyze
   your network.

## Running tests

You can run all tests in this repository by running:

```
bazel test //...
```

The above is a specific instance of the general form for running all tests in all `BUILD` files
rooted under a specific subdirectory.

For example, to run all tests in all `BUILD` files rooted at `projects/coordinator` do:

```
bazel test //projects/coordinator/...
```

To run a specific test rule from a `BUILD` file:

```
bazel test //<path-to-dir-containing-BUILD>:<name-of-test-rule>
```

For example, `projects/batfish/BUILD` has a test rule called `pmd`. To run that test, do:

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

## Building a deployable batfish or allinone docker image

- Use the batfish image if you just want the batfish service.
- Use the allinone image if you want the batfish service and a built-in jupyter notebook server.

### Building images locally

This section of the doc is still in progress. Check back later!

### Building images via github actions

First, you will need to fork the batfish/docker repo on github.

1. Visit https://github.com/batfish/docker
2. Log in if you have not already done so.
3. Click the fork button towards the top right.
4. Select your desired owner, and click "Create fork".
5. Follow any remaining dialogues to complete the creation of your fork.

Next, you will need to clone the repo and add your fork as a remote.

In the example below, replace:

- `fork-owner` with your chosen fork owner
- `fork-repo-name` with your chosen repository name
- `fork-branch-name` with the name of the branch you want to create on your fork

```
git clone https://github.com/batfish/docker
cd docker
git remote add fork-owner git@github.com:fork-owner/fork-repo-name
git fetch --all
git checkout -b fork-branch-name
git push -u fork-owner
```

**N.B. The rest of these instructions assume you have already forked the batfish and/or pybatfish
repos in a similar fashion, and made your changes available in a branch on each forked repo. **

Modify the pointer(s) in precommit.yml to point to your batfish / pybatfish repos and branches:

- Open `.github/workflows/precommit.yml` in the editor of your choice
- Change the value of `BATFISH_GITHUB_BATFISH_REPO` to the github repo whose batfish source you want
  to use.
- Change the value of `BATFISH_GITHUB_BATFISH_REF` to the branch of your selected repo whose batfish
  source you want to use.
- Change the value of `BATFISH_GITHUB_PYBATFISH_REPO` to the github repo whose pybatfish source you
  want
  to use. Skip this step if you are only interested in obtaining a `batfish` image.
- Change the value of `BATFISH_GITHUB_PYBATFISH_REF` to the branch of your selected repo whose
  batfish
  source you want to use. Skip this step if you are only interested in obtaining a `batfish` image.
- Commit and push the changes to your batfish/docker fork and branch:
   ```
   git commit .github/workflows/precommit.yml -m "(Your commit message here)"
   git push -u fork-owner fork-branch-name
   ```
- Open a pull request from your branch into your own fork's master.
    - Visit https://github.com/fork-owner/fork-repo-name/pull/new/fork-branch-name
    - Change "base repository" to fork-owner/fork-repo-name via the button toward the top left under
      "Comparing changes".
    - Ensure that the value of `base` is now `master`.
    - Click `Create pull request` on the right. Note that you do not need to actually merge this
      pull request, but creating it has the side effect of triggering Github actions which will
      build the image(s) you need.
    - Once the PR has been created, click on the "Actions" tab of your repo.
    - Click on the "Pre-commit" workflow
    - Click on the name of the run with your commit message and branch name.
    - Wait for all Jobs on the left to complete. **If any job does not pass and become green, it is
      not safe to proceed!**
    - Click on "Summary" toward the top left if you are not already there, or the artifacts have not
      appeared yet following completion of the workflow.
    - Download the image you want (either `allinone_image` or `bf_image`). You will get a zip file.
    - Unzip the file, and you should get a `.tar` file out.
    - Load the tar file in docker via `docker load -i <tarfilename>`. You should get output like
      the following:
      ```
      arifogel@nova:~/scratch$docker load -i bf.tar
      58ff1e19a041: Loading layer [==================================================>]     100B/100B
      caab24035869: Loading layer [==================================================>]     100B/100B
      87639d5c0dd6: Loading layer [==================================================>]  60.74MB/60.74MB
      c7a736a5bb60: Loading layer [==================================================>]  92.78MB/92.78MB
      Loaded image: batfish/batfish:dev-3
      ```
    - Note the tag on the image. In the above example, the tag is `dev-3`.
    - Now you can launch a container from the image. Note that launching a container will create
      a `batfish-data` directory under the current directory, which will be used for persistent
      storage. If there is already such a directory created by a previous version, you should remove
      it first.
    - To launch a container, execute the appropriate command below, replacing `dev-3` with the tag
      you recorded earlier:
        - For batfish, run:
          ```
          docker run --name batfish -v batfish-data:/data -p 9997:9997 -p 9996:9996 batfish/batfish:dev-3
          ```
        - For allinone, instead run:
          ```
          docker run --name allinone -v batfish-data:/data -p 8888:8888 -p 9997:9997 -p 9996:9996 batfish/allinone:dev-3
          ```

### Building the batfish docker image

This section of the doc is still in progress. Check back later!

### Building the allinone docker image

*The allinone image uses the batfish image as a base. So before creating an allinone image, first
create a batfish image using the instructions above.*

This section of the doc is still in progress. Check back later!
