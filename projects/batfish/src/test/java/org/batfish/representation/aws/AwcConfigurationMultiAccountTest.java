package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.testSetup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AwcConfigurationMultiAccountTest {
  private static final String TESTCONFIGS_DIR = "org/batfish/representation/aws/test-multi-account";
  private static final List<String> fileNames =
      ImmutableList.of("accounts/111/us-west-1/Vpcs.json", "accounts/222/us-west-2/Vpcs.json");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish = testSetup(TESTCONFIGS_DIR, fileNames, _folder);
  }

  @Test
  public void testBothVpcsExist() {
    SortedMap<String, Configuration> configs = _batfish.loadConfigurations(_batfish.getSnapshot());
    assertThat(configs.get("vpc-015b20578b48c1349"), notNullValue());
    assertThat(configs.get("vpc-0008a7b45e3ddf1dd"), notNullValue());
  }
}
