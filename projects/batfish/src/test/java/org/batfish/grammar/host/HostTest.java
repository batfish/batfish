package org.batfish.grammar.host;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class HostTest {

  private static class InterfaceMatchers {
    private static class IsActive extends FeatureMatcher<Interface, Boolean> {

      public IsActive() {
        super(Matchers.equalTo(true), "active", "active");
      }

      @Override
      protected Boolean featureValueOf(Interface actual) {
        return actual.getActive();
      }
    }

    public static IsActive isActive() {
      return new IsActive();
    }
  }

  private static String TESTRIG_PREFIX = "org/batfish/grammar/host/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  public Map<String, Configuration> testNatIpsecVpnsHelper(String hostFilename) throws IOException {
    String testrigResourcePrefix = TESTRIG_PREFIX + "ipsec-vpn-host-aws-cisco";
    List<String> awsFilenames =
        ImmutableList.of(
            "AvailabilityZones-us-west-2.json",
            "CustomerGateways-us-west-2.json",
            "InternetGateways-us-west-2.json",
            "NetworkAcls-us-west-2.json",
            "NetworkInterfaces-us-west-2.json",
            "RouteTables-us-west-2.json",
            "SecurityGroups-us-west-2.json",
            "Subnets-us-west-2.json",
            "VpcEndpoints-us-west-2.json",
            "Vpcs-us-west-2.json",
            "VpnConnections-us-west-2.json",
            "VpnGateways-us-west-2.json");
    List<String> configurationFilenames = ImmutableList.of("cisco_host");
    List<String> hostFilenames = ImmutableList.of(hostFilename);

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setAwsText(testrigResourcePrefix, awsFilenames)
                .setConfigurationText(testrigResourcePrefix, configurationFilenames)
                .setHostsText(testrigResourcePrefix, hostFilenames)
                .build(),
            _folder);
    return batfish.loadConfigurations();
  }

  @Test
  public void testNatIpsecVpnsNotShared() throws IOException {
    Map<String, Configuration> configurations = testNatIpsecVpnsHelper("host1-not-shared.json");

    /*
     * NAT settings on host1 (not-shared version) should result in Tunnel1 on cisco_host being down
     */
    assertThat(
        configurations.get("cisco_host").getInterfaces().get("Tunnel1"),
        not(InterfaceMatchers.isActive()));
  }

  @Test
  public void testNatIpsecVpnsShared() throws IOException {
    Map<String, Configuration> configurations = testNatIpsecVpnsHelper("host1-shared.json");

    /*
     * NAT settings on host1 (shared version) should result in Tunnel1 on cisco_host being up
     */
    assertThat(
        configurations.get("cisco_host").getInterfaces().get("Tunnel1"),
        InterfaceMatchers.isActive());
  }
}
