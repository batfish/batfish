package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

public class NamedAsPathSetTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new NamedAsPathSet("A"), new NamedAsPathSet("A"))
        .addEqualityGroup(new NamedAsPathSet("B"))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    NamedAsPathSet expr = new NamedAsPathSet("A");
    assertThat(expr, equalTo(BatfishObjectMapper.clone(expr, AsPathSetExpr.class)));
    assertThat(expr, equalTo(SerializationUtils.clone(expr)));
  }

  @Test
  public void testEvaluate() {
    // Create two AsPathAccessLists, one that permits only the empty AsPath, and one that permits
    // only the AsPath containing ASN 500.
    AsPathAccessList emptyAsPath =
        SerializationUtils.clone(
            new AsPathAccessList(
                "emptyAsPath",
                ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, "^$"))));
    AsPathAccessList singleAsPath =
        SerializationUtils.clone(
            new AsPathAccessList(
                "singleAsPath",
                ImmutableList.of(new AsPathAccessListLine(LineAction.PERMIT, "^500$"))));

    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    c.setAsPathAccessLists(
        ImmutableMap.of(emptyAsPath.getName(), emptyAsPath, singleAsPath.getName(), singleAsPath));

    // Empty
    ConnectedRoute r =
        ConnectedRoute.builder()
            .setNetwork(Prefix.parse("1.2.3.0/24"))
            .setNextHop(NextHopInterface.of("Ethernet1", Ip.parse("1.2.3.4")))
            .build();
    Environment env = Environment.builder(c).setOriginalRoute(r).build();
    assertTrue(
        "Empty AS Path regex matches connected route",
        new NamedAsPathSet(emptyAsPath.getName()).matches(env));
    assertFalse(
        "AS Path of 500 does not match connected route",
        new NamedAsPathSet(singleAsPath.getName()).matches(env));
  }
}
