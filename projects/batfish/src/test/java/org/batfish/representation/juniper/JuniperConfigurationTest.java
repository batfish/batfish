package org.batfish.representation.juniper;

import static org.batfish.datamodel.matchers.AndMatchExprMatchers.hasConjuncts;
import static org.batfish.datamodel.matchers.AndMatchExprMatchers.isAndMatchExprThat;
import static org.batfish.datamodel.matchers.HeaderSpaceMatchers.hasSrcIps;
import static org.batfish.datamodel.matchers.IpAccessListLineMatchers.hasAction;
import static org.batfish.datamodel.matchers.IpAccessListLineMatchers.hasMatchCondition;
import static org.batfish.datamodel.matchers.MatchHeaderSpaceMatchers.hasHeaderSpace;
import static org.batfish.datamodel.matchers.MatchHeaderSpaceMatchers.isMatchHeaderSpaceThat;
import static org.batfish.datamodel.matchers.OrMatchExprMatchers.hasDisjuncts;
import static org.batfish.datamodel.matchers.OrMatchExprMatchers.isOrMatchExprThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Assert;
import org.junit.Test;

public class JuniperConfigurationTest {

  private static JuniperConfiguration createConfig() {
    JuniperConfiguration config = new JuniperConfiguration(Collections.emptySet());
    config._c = new Configuration("host", ConfigurationFormat.JUNIPER);
    return config;
  }

  @Test
  public void testBuildSecurityPolicyAcl() {
    JuniperConfiguration config = createConfig();
    IpAccessList aclNullZone = config.buildSecurityPolicyAcl("name", null);

    // Add zone without any zone policies
    Zone zone = new Zone("zone", new TreeMap<>());
    config.getZones().put("zone", zone);
    IpAccessList aclWithoutPolicy = config.buildSecurityPolicyAcl("name", zone);
    //IpAccessListLine aclLineWithoutPolicy = aclWithoutPolicy.getLines().get(0);

    // Add zone with policies
    zone.getFromZonePolicies().put("policy1", new FirewallFilter("filter", Family.INET, 0));
    zone.getFromZonePolicies().put("policy2", new FirewallFilter("filter", Family.INET, 0));
    IpAccessList aclWithPolicy = config.buildSecurityPolicyAcl("name", zone);
    //IpAccessListLine aclLineWithPolicy = aclWithPolicy.getLines().get(0);

    // Null zone should produce no security policy acl
    assertThat(aclNullZone, is(nullValue()));

    // Zone with no policy should produce a simple deny acl
    IpAccessListLine aclLineWithoutPolicy = Iterables.getOnlyElement(aclWithoutPolicy.getLines());
    // Should match all traffic
    assertThat(
        aclLineWithoutPolicy,
        hasMatchCondition(
            equalTo(TrueExpr.INSTANCE)
        )
    );
    // Should reject matches
    assertThat(
        aclLineWithoutPolicy,
        hasAction(
            equalTo(LineAction.REJECT)
        )
    );

    // Zone with policies should produce match expr that is a logical OR of those policies
    IpAccessListLine aclLineWithPolicy = Iterables.getOnlyElement(aclWithPolicy.getLines());
    // Should be OrMatchExpr (match any policy)
    assertThat(
        aclLineWithPolicy,
        hasMatchCondition(
            isOrMatchExprThat(
                hasDisjuncts(
                    containsInAnyOrder(new PermittedByAcl("policy1"), new PermittedByAcl("policy2"))
                )
            )
        )
    );
    // Should accept matches
    assertThat(
        aclLineWithPolicy,
        hasAction(
            equalTo(LineAction.ACCEPT)
        )
    );
  }

  @Test
  public void testBuildOutgoingFilter() {}

  @Test
  public void testToHeaderSpaceIpAccessList() {}

  @Test
  public void testToIpAccessList() {
    JuniperConfiguration config = createConfig();
    FirewallFilter filter = new FirewallFilter("filter", Family.INET, -1);
    IpAccessList emptyAcl = config.toIpAccessList(filter);

    FwTerm term = new FwTerm("term");
    String ipAddrPrefix = "1.2.3.0/24";
    term.getFroms().add(new FwFromSourceAddress(Prefix.parse(ipAddrPrefix)));
    term.getThens().add(FwThenAccept.INSTANCE);
    filter.getTerms().put("term", term);
    IpAccessList headerSpaceAcl = config.toIpAccessList(filter);

    Zone zone = new Zone("zone", new TreeMap<>());
    String interfaceName = "interface";
    zone.getInterfaces().add(new Interface(interfaceName, -1));
    config.getZones().put("zone", zone);
    filter.getFromZones().add("zone");
    IpAccessList headerSpaceAndSrcInterfaceAcl = config.toIpAccessList(filter);

    // ACL from empty filter should have no lines
    assertThat(emptyAcl.getLines(), iterableWithSize(0));

    // ACL from headerSpace filter should have one line
    IpAccessListLine headerSpaceAclLine = Iterables.getOnlyElement(headerSpaceAcl.getLines());
    // It should have a MatchHeaderSpace match condition, matching the ipAddrPrefix from above
    assertThat(
        headerSpaceAclLine,
        hasMatchCondition(
            isMatchHeaderSpaceThat(
                hasHeaderSpace(hasSrcIps(contains(new IpWildcard(Prefix.parse(ipAddrPrefix))))))));

    // ACL from headerSpace and zone filter should have one line
    IpAccessListLine comboAclLine =
        Iterables.getOnlyElement(headerSpaceAndSrcInterfaceAcl.getLines());
    // It should have an AndMatchExpr match condition, containing both a MatchSrcInterface
    // condition and a MatchHeaderSpace condition
    assertThat(
        comboAclLine,
        hasMatchCondition(
            isAndMatchExprThat(
                hasConjuncts(
                    containsInAnyOrder(
                        new MatchSrcInterface(ImmutableList.of(interfaceName)),
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(
                                    ImmutableSet.of(new IpWildcard(Prefix.parse(ipAddrPrefix))))
                                .build()))))));
  }
}
