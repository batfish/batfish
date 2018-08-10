package org.batfish.representation.juniper;

import static org.batfish.datamodel.matchers.AndMatchExprMatchers.hasConjuncts;
import static org.batfish.datamodel.matchers.AndMatchExprMatchers.isAndMatchExprThat;
import static org.batfish.datamodel.matchers.HeaderSpaceMatchers.hasSrcIps;
import static org.batfish.datamodel.matchers.IpAccessListLineMatchers.hasMatchCondition;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.datamodel.matchers.MatchHeaderSpaceMatchers.hasHeaderSpace;
import static org.batfish.datamodel.matchers.MatchHeaderSpaceMatchers.isMatchHeaderSpaceThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.iterableWithSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.TreeMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.junit.Test;

public class JuniperConfigurationTest {

  private static JuniperConfiguration createConfig() {
    JuniperConfiguration config = new JuniperConfiguration();
    config._c = new Configuration("host", ConfigurationFormat.JUNIPER);
    return config;
  }

  @Test
  public void testToIpAccessList() {
    JuniperConfiguration config = createConfig();
    FirewallFilter filter = new FirewallFilter("filter", Family.INET);
    IpAccessList emptyAcl = config.toIpAccessList(filter);

    FwTerm term = new FwTerm("term");
    String ipAddrPrefix = "1.2.3.0/24";
    term.getFroms().add(new FwFromSourceAddress(new IpWildcard(Prefix.parse(ipAddrPrefix))));
    term.getThens().add(FwThenAccept.INSTANCE);
    filter.getTerms().put("term", term);
    IpAccessList headerSpaceAcl = config.toIpAccessList(filter);

    Zone zone = new Zone("zone", new TreeMap<>());
    String interface1Name = "interface1";
    zone.getInterfaces().add(new Interface(interface1Name, -1));
    String interface2Name = "interface2";
    zone.getInterfaces().add(new Interface(interface2Name, -1));
    config.getZones().put("zone", zone);
    filter.setFromZone("zone");
    IpAccessList headerSpaceAndSrcInterfaceAcl = config.toIpAccessList(filter);

    // ACL from empty filter should have no lines
    assertThat(emptyAcl.getLines(), iterableWithSize(0));

    // ACL from headerSpace filter should have one line
    IpAccessListLine headerSpaceAclLine = Iterables.getOnlyElement(headerSpaceAcl.getLines());
    // It should have a MatchHeaderSpace match condition, matching the ipAddrPrefix from above
    ImmutableList.of("1.2.3.0", "1.2.3.255")
        .stream()
        .map(Ip::new)
        .forEach(
            ip ->
                assertThat(
                    headerSpaceAclLine,
                    hasMatchCondition(
                        isMatchHeaderSpaceThat(hasHeaderSpace(hasSrcIps(containsIp(ip)))))));

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
                        new MatchSrcInterface(ImmutableList.of(interface1Name, interface2Name)),
                        new MatchHeaderSpace(
                            HeaderSpace.builder()
                                .setSrcIps(new IpWildcard(Prefix.parse(ipAddrPrefix)).toIpSpace())
                                .build()))))));
  }
}
