# Batfish Code Runner

CLI tool to run user-provided Java code against parsed Batfish configurations.

## Usage

```bash
bazel run //projects/batfish-code-runner:cli -- <snapshot-dir> <code-file>
```

## Function Signature

User code must implement:
```java
String func(Configuration viConfig, VendorConfiguration vendorConfig, String originalText, String processedText)
```

## Output

CSV with columns: `hostname,result`

## Example

```java
import org.batfish.coderunner.BatfishCodeRunner.BatfishFunction;
import org.batfish.datamodel.Configuration;
import org.batfish.vendor.VendorConfiguration;

public class Example implements BatfishFunction {
  @Override
  public String apply(
      Configuration viConfig,
      VendorConfiguration vendorConfig,
      String originalText,
      String processedText) {
    return viConfig.getHostname() + " has " + viConfig.getAllInterfaces().size() + " interfaces";
  }
}
```
