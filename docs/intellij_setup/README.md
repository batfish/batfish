# Set up the IntelliJ IDE

There are a few steps to make sure that IntelliJ can build Batfish correctly.

## Pre-requisites

* Cloned and compiled Batfish per [these instructions](../building_and_running/README.md)

## 1. Install IntelliJ plugins

Install the following plugins by opening `Preferences`(macOS) or `Settings`(
Ubuntu) > `Plugins` > `Marketplace` and then
searching for the relevant plugin:

![](intellij_install_plugins.png)

### google-java-format

Batfish uses the `google-java-format` plugin to format the code, so that all users end up with code
the same style (in IntelliJ as well as in any other editors). Install it from the marketplace.

### Bazel

You'll need the [`bazel`](https://plugins.jetbrains.com/plugin/8609-bazel) plugin in order to build.
Install it from the marketplace.

## 2. Raise the default IDE file size limits

The ANTLR4-generated Java files that contain configuration parsers are very large. To enable
IntelliJ to index them, we need to raise IntelliJ's default file size limits.

From the menu bar, choose `Help` > `Edit Custom Properties` and then enter the following in
the `idea.properties` file that is opened.

```
# custom IntelliJ IDEA properties

idea.max.intellisense.file.length=500000

#---------------------------------------------------------------------
# Maximum file size (kilobytes) IDE should provide code assistance for.
# The larger file is the slower its editor works and higher overall system memory requirements are
# if code assistance is enabled. Remove this property or set to very large number if you need
# code assistance for any files available regardless their size.
#---------------------------------------------------------------------
idea.max.intellisense.filesize=50000
```

## 3. Import the Batfish Bazel project

1. Open IntelliJ and choose `Import Bazel Project` (under `File`, or from the main splash screen).
1. In the file dialog that results, choose the `batfish` directory that is the root of the cloned
   git repository.
1. Just hit `Next` until you get to the main window; the default settings work fine.

## 4. (Optional) Add an allinone run configuration for end-to-end testing with an external client

1. Open Intellij and choose `Run` > `Edit Configurations...`
2. Add a new bazel command configuration by clicking the `+` icon. See sample in below screenshot.
   Note that the sample uses a custom log4j2.properties file in order to adjust logging when
   running `allinone`. You can omit this file/option. However if you want custom logging, you
   can use a copy of `projects/batfish/src/test/resources/log4j2.properties` as a base.
   ![](intellij-allinone-run-configuration.png)

## 5. Go

On opening, IntelliJ will run a `bazel sync` to learn the project structure and targets.

For synced files, IntelliJ's bazel plugin run unit tests, debug, add new Run Configurations, etc. in
the usual way.

For more information or advanced use cases, see the Bazel
IntelliJ [documentation](https://ij.bazel.build/docs/bazel-plugin.html).
