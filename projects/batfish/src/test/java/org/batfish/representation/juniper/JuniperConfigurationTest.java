package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Test;

public class JuniperConfigurationTest {

  private JuniperConfiguration createConfig() {
    JuniperConfiguration config = new JuniperConfiguration(Collections.emptySet());
    config._c = new Configuration("host", ConfigurationFormat.JUNIPER);
    return config;
  }

  @Test
  public void testBuildSecurityPolicyAclWithNoZone() {
    JuniperConfiguration config = createConfig();
    IpAccessList aclNullZone = config.buildSecurityPolicyAcl("name", null);

    // Null zone should produce no security policy acl
    assertThat(aclNullZone, is(nullValue()));
  }

  @Test
  public void testBuildSecurityPolicyAclWithNoPolicy() {
    JuniperConfiguration config = createConfig();

    // Add zone without any zone policies
    Zone zone = new Zone("zone", new TreeMap<>());
    config.getZones().put("zone", zone);
    IpAccessList aclWithoutPolicy = config.buildSecurityPolicyAcl("name", zone);
    IpAccessListLine aclLineWithoutPolicy = aclWithoutPolicy.getLines().get(0);

    // Zone with no policy should produce a simple deny acl
    assertThat(aclWithoutPolicy.getLines(), iterableWithSize(1));
    // Should match all traffic
    assertThat(aclLineWithoutPolicy.getMatchCondition(), equalTo(TrueExpr.INSTANCE));
    // Should reject matches
    assertThat(aclLineWithoutPolicy.getAction(), equalTo(LineAction.REJECT));
  }

  @Test
  public void testBuildSecurityPolicyAclWithPolicy() {
    JuniperConfiguration config = createConfig();

    // Add zone with policies
    Zone zone = new Zone("zone", new TreeMap<>());
    config.getZones().put("zone", zone);
    zone.getFromZonePolicies().put("policy1", new FirewallFilter("filter", Family.INET, 0));
    zone.getFromZonePolicies().put("policy2", new FirewallFilter("filter", Family.INET, 0));
    IpAccessList aclWithPolicy = config.buildSecurityPolicyAcl("name", zone);
    IpAccessListLine aclLineWithPolicy = aclWithPolicy.getLines().get(0);

    // Zone with policies should produce match expr that is a logical OR of those policies
    assertThat(aclWithPolicy.getLines(), iterableWithSize(1));
    // Should be OrMatchExpr (match any policy)
    assertThat(aclLineWithPolicy.getMatchCondition(), is(instanceOf(OrMatchExpr.class)));
    Set<AclLineMatchExpr> disjuncts =
        ((OrMatchExpr) aclLineWithPolicy.getMatchCondition()).getDisjuncts();
    // Disjuncts should contain PermittedByAcls referencing both of the policies
    assertThat(
        disjuncts,
        containsInAnyOrder(new PermittedByAcl("policy1"), new PermittedByAcl("policy2")));
    // Should accept matches
    assertThat(aclLineWithPolicy.getAction(), equalTo(LineAction.ACCEPT));
  }
}
