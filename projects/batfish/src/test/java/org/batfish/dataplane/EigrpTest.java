package org.batfish.dataplane;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EigrpTest {
  private static final String SNAPSHOT_PREFIX = "org/batfish/dataplane/ibdp/";
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testRoutePresence() throws IOException {
    String testrigName = "ios-eigrp";
    List<String> configurationNames =
        ImmutableList.of(
            "dc1.cfg", "dc1border.cfg", "dc1lan.cfg", "dc2.cfg", "dc2border.cfg", "dc2lan.cfg");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOT_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);

    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());

    Map<String, Map<String, Set<AbstractRoute>>> routes = getRoutes(dp.getRibs());

    ////////////////////////
    // All assertions based on GNS3 and output of "show ip route"
    ////////////////////////

    // TODO: figure out why some external routes are missing

    ////////////////////////
    // Node: dc1lan
    ////////////////////////
    {
      String node = "dc1lan";
      // 172.16.1.1 is connected.
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.2.1/32"),
          RoutingProtocol.EIGRP,
          130816,
          Ip.parse("11.11.11.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.3.1/32"),
          RoutingProtocol.EIGRP,
          131072,
          Ip.parse("11.11.11.2"));
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("172.16.4.1/32"),
      //          RoutingProtocol.EIGRP_EX,
      //          5632,
      //          Ip.parse("11.11.11.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.5.1/32"),
          RoutingProtocol.EIGRP_EX,
          5632,
          Ip.parse("11.11.11.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.6.1/32"),
          RoutingProtocol.EIGRP_EX,
          5632,
          Ip.parse("11.11.11.2"));
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("33.33.33.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          5632,
      //          Ip.parse("11.11.11.2"));
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("44.44.44.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          5632,
      //          Ip.parse("11.11.11.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("55.55.55.0/24"),
          RoutingProtocol.EIGRP_EX,
          5632,
          Ip.parse("11.11.11.2"));
    }

    ////////////////////////
    // Node: dc1
    ////////////////////////
    {
      String node = "dc1";
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.1.1/32"),
          RoutingProtocol.EIGRP,
          130816,
          Ip.parse("11.11.11.1"));
      // 172.16.1.2 is connected.
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.3.1/32"),
          RoutingProtocol.EIGRP,
          130816,
          Ip.parse("22.22.22.2"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("172.16.4.1/32"),
      //          RoutingProtocol.EIGRP_EX,
      //          5376,
      //          Ip.parse("22.22.22.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.5.1/32"),
          RoutingProtocol.EIGRP_EX,
          5376,
          Ip.parse("22.22.22.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.6.1/32"),
          RoutingProtocol.EIGRP_EX,
          5376,
          Ip.parse("22.22.22.2"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("33.33.33.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          5376,
      //          Ip.parse("22.22.22.2"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("44.44.44.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          5376,
      //          Ip.parse("22.22.22.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("55.55.55.0/24"),
          RoutingProtocol.EIGRP_EX,
          5376,
          Ip.parse("22.22.22.2"));
    }

    ////////////////////////
    // Node: dc1border
    ////////////////////////
    {
      String node = "dc1border";
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.1.1/32"),
          RoutingProtocol.EIGRP,
          131072,
          Ip.parse("22.22.22.1"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.2.1/32"),
          RoutingProtocol.EIGRP,
          130816,
          Ip.parse("22.22.22.1"));
      // 172.16.3.1 is connected.
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("172.16.4.1/32"),
      //          RoutingProtocol.EIGRP_EX,
      //          61440,
      //          Ip.parse("33.33.33.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.5.1/32"),
          RoutingProtocol.EIGRP_EX,
          61440,
          Ip.parse("33.33.33.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.6.1/32"),
          RoutingProtocol.EIGRP_EX,
          61440,
          Ip.parse("33.33.33.2"));

      assertRoute(
          routes,
          node,
          Prefix.parse("11.11.11.0/24"),
          RoutingProtocol.EIGRP,
          3072,
          Ip.parse("22.22.22.1"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("44.44.44.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          61440,
      //          Ip.parse("33.33.33.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("55.55.55.0/24"),
          RoutingProtocol.EIGRP_EX,
          61440,
          Ip.parse("33.33.33.2"));
    }

    ////////////////////////
    // Node: dc2border
    ////////////////////////
    {
      String node = "dc2border";
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.1.1/32"),
          RoutingProtocol.EIGRP_EX,
          61440,
          Ip.parse("33.33.33.1"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.2.1/32"),
          RoutingProtocol.EIGRP_EX,
          61440,
          Ip.parse("33.33.33.1"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("172.16.3.1/32"),
      //          RoutingProtocol.EIGRP_EX,
      //          61440,
      //          Ip.parse("33.33.33.1"));
      // 172.16.4.1 is connected.
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.5.1/32"),
          RoutingProtocol.EIGRP,
          130816,
          Ip.parse("44.44.44.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.6.1/32"),
          RoutingProtocol.EIGRP,
          131072,
          Ip.parse("44.44.44.2"));

      assertRoute(
          routes,
          node,
          Prefix.parse("11.11.11.0/24"),
          RoutingProtocol.EIGRP_EX,
          61440,
          Ip.parse("33.33.33.1"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("22.22.22.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          61440,
      //          Ip.parse("33.33.33.1"));
    }

    ////////////////////////
    // Node: dc2
    ////////////////////////
    {
      String node = "dc2";
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.1.1/32"),
          RoutingProtocol.EIGRP_EX,
          5376,
          Ip.parse("44.44.44.1"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.2.1/32"),
          RoutingProtocol.EIGRP_EX,
          5376,
          Ip.parse("44.44.44.1"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("172.16.3.1/32"),
      //          RoutingProtocol.EIGRP_EX,
      //          5376,
      //          Ip.parse("44.44.44.1"));

      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.4.1/32"),
          RoutingProtocol.EIGRP,
          130816,
          Ip.parse("44.44.44.1"));
      // 172.16.5.1 is connected.
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.6.1/32"),
          RoutingProtocol.EIGRP,
          130816,
          Ip.parse("55.55.55.1"));

      assertRoute(
          routes,
          node,
          Prefix.parse("11.11.11.0/24"),
          RoutingProtocol.EIGRP_EX,
          5376,
          Ip.parse("44.44.44.1"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("22.22.22.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          5376,
      //          Ip.parse("44.44.44.1"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("33.33.33.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          5376,
      //          Ip.parse("44.44.44.1"));
    }

    ////////////////////////
    // Node: dc2lan
    ////////////////////////
    {
      String node = "dc2lan";
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.1.1/32"),
          RoutingProtocol.EIGRP_EX,
          5632,
          Ip.parse("55.55.55.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.2.1/32"),
          RoutingProtocol.EIGRP_EX,
          5632,
          Ip.parse("55.55.55.2"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("172.16.3.1/32"),
      //          RoutingProtocol.EIGRP_EX,
      //          5632,
      //          Ip.parse("55.55.55.2"));

      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.4.1/32"),
          RoutingProtocol.EIGRP,
          131072,
          Ip.parse("55.55.55.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("172.16.5.1/32"),
          RoutingProtocol.EIGRP,
          130816,
          Ip.parse("55.55.55.2"));
      // 172.16.6.1 is connected.

      assertRoute(
          routes,
          node,
          Prefix.parse("11.11.11.0/24"),
          RoutingProtocol.EIGRP_EX,
          5632,
          Ip.parse("55.55.55.2"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("22.22.22.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          5632,
      //          Ip.parse("55.55.55.2"));
      // missing
      //      assertRoute(
      //          routes,
      //          node,
      //          Prefix.parse("33.33.33.0/24"),
      //          RoutingProtocol.EIGRP_EX,
      //          5632,
      //          Ip.parse("55.55.55.2"));
      assertRoute(
          routes,
          node,
          Prefix.parse("44.44.44.0/24"),
          RoutingProtocol.EIGRP,
          3072,
          Ip.parse("55.55.55.2"));
    }
  }

  private void assertRoute(
      Map<String, Map<String, Set<AbstractRoute>>> allRoutes,
      String node,
      Prefix prefix,
      RoutingProtocol protocol,
      long metric,
      @Nullable Ip nextHopIp) {
    Set<AbstractRoute> routes = allRoutes.get(node).get(Configuration.DEFAULT_VRF_NAME);
    assertThat(
        routes,
        hasItem(
            allOf(
                hasPrefix(prefix),
                hasProtocol(protocol),
                hasMetric(metric),
                nextHopIp != null ? hasNextHopIp(nextHopIp) : hasNextHopIp(anything()))));
  }

  private Map<String, Map<String, Set<AbstractRoute>>> getRoutes(
      SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs) {
    return toImmutableMap(
        ribs,
        Entry::getKey,
        nodeEntry ->
            toImmutableMap(
                nodeEntry.getValue(), Entry::getKey, vrfEntry -> vrfEntry.getValue().getRoutes()));
  }
}
