package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.RoutingProtocol.EIGRP_EX;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class EigrpTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testEigrpDistributeList() throws IOException {
    String advertiser = "advertiser";
    String listener = "listener";
    String farlistener = "farlistener";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    "org/batfish/dataplane/ibdp/eigrp-distribute-lists",
                    advertiser,
                    listener,
                    farlistener)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    IncrementalDataPlane dataplane =
        (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dataplane);

    {
      Set<AbstractRoute> listenerRoutes = routes.get(listener).get(DEFAULT_VRF_NAME);
      // 1.1.1.0/24 and 4.4.4.0/24 is allowed to be exported to listener and 3.3.3.0/24 is filtered
      // by
      // distribute list
      assertThat(listenerRoutes, not(hasItem(hasPrefix(Prefix.parse("3.3.3.0/24")))));
      assertThat(
          listenerRoutes,
          hasItem(allOf(hasPrefix(Prefix.parse("1.1.1.0/24")), hasProtocol(EIGRP_EX))));
      assertThat(
          listenerRoutes,
          hasItem(allOf(hasPrefix(Prefix.parse("4.4.4.0/24")), hasProtocol(EIGRP_EX))));
    }
    {
      Set<AbstractRoute> farListenerRoutes = routes.get(farlistener).get(DEFAULT_VRF_NAME);
      assertThat(farListenerRoutes, not(hasItem(hasPrefix(Prefix.parse("3.3.3.0/24")))));
      assertThat(farListenerRoutes, not(hasItem(hasPrefix(Prefix.parse("4.4.4.0/24")))));
      assertThat(
          farListenerRoutes,
          hasItem(allOf(hasPrefix(Prefix.parse("1.1.1.0/24")), hasProtocol(EIGRP_EX))));
    }
  }
}
