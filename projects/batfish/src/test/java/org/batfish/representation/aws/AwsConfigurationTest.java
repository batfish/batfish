package org.batfish.representation.aws;

import java.util.Arrays;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AwsConfigurationTest {

  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/representation/aws/test-internet-connectivity/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /** Test that we have internet connectivity from 'internet' to public IPs and back. */
  @Test
  public void testInternetConnectivity() {

    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }
}
