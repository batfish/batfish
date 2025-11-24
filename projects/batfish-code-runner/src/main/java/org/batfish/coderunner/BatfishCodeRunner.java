package org.batfish.coderunner;

import org.batfish.datamodel.Configuration;
import org.batfish.vendor.VendorConfiguration;

public class BatfishCodeRunner {
  private final RuntimeCompiler compiler;

  public BatfishCodeRunner() {
    this.compiler = new RuntimeCompiler();
  }

  public String runCode(
      Configuration viConfig,
      VendorConfiguration vendorConfig,
      String originalText,
      String processedText,
      String code)
      throws Exception {
    Class<?> compiledClass = compiler.compile(code);
    Object instance = compiledClass.getDeclaredConstructor().newInstance();
    @SuppressWarnings("unchecked")
    BatfishFunction function = (BatfishFunction) instance;
    return function.apply(viConfig, vendorConfig, originalText, processedText);
  }

  @FunctionalInterface
  public interface BatfishFunction {
    String apply(
        Configuration viConfig,
        VendorConfiguration vendorConfig,
        String originalText,
        String processedText);
  }
}
