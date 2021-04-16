package org.batfish.representation.iptables;

import static org.batfish.representation.iptables.IptablesVendorConfiguration.toIpAccessList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Ip;
import org.batfish.representation.iptables.IptablesMatch.MatchType;
import org.batfish.representation.iptables.IptablesRule.IptablesActionType;
import org.junit.Test;

public class IpTablesVendorConfigurationTest {

  /**
   * Batfish only supports permit and drop IpTablesActions. Check that we drop those and let others
   * pass.
   */
  @Test
  public void toIpAccessList_skipUnsupportedActions() {
    Map<AclLine, String> lineInInterfaces = new HashMap<>();
    Map<AclLine, String> lineOutInterfaces = new HashMap<>();

    // create a chain with only unsupported rule
    IptablesRule unsupportedRule = new IptablesRule();
    unsupportedRule.setAction(IptablesActionType.CHAIN, null);

    IptablesChain chain = new IptablesChain("chain");
    chain.addRule(unsupportedRule, -1);

    {
      Warnings warnings = new Warnings(true, true, true);

      toIpAccessList(
          "acl",
          chain,
          new IptablesVendorConfiguration(),
          warnings,
          lineInInterfaces,
          lineOutInterfaces);

      // nothing should be converted and we should get a warning
      assertTrue(lineInInterfaces.isEmpty());
      assertTrue(lineOutInterfaces.isEmpty());
      assertThat(
          Iterables.getOnlyElement(warnings.getRedFlagWarnings()).getText(),
          containsString("not supported"));
    }

    // add a supported rule to the chain
    IptablesRule supportedRule = new IptablesRule();
    supportedRule.setAction(IptablesActionType.ACCEPT, null);
    supportedRule.addMatch(false, MatchType.DESTINATION, Ip.parse("1.1.1.1"));

    chain.addRule(supportedRule, -1);

    {
      Warnings warnings = new Warnings(true, true, true);
      toIpAccessList(
          "acl",
          chain,
          new IptablesVendorConfiguration(),
          warnings,
          lineInInterfaces,
          lineOutInterfaces);

      // the supported rule should be converted and we should get a warning for the unsupported rule
      assertThat(lineInInterfaces.size(), equalTo(1));
      assertThat(lineOutInterfaces.size(), equalTo(1));
      assertThat(
          Iterables.getOnlyElement(warnings.getRedFlagWarnings()).getText(),
          containsString("not supported"));
    }
  }
}
