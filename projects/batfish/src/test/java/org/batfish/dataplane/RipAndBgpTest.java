package org.batfish.dataplane;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class RipAndBgpTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static String TESTRIGS_PREFIX = "org/batfish/dataplane/testrigs/";

  @Test
  public void testOutputRoutes() throws IOException {
    String testrigResourcePrefix = TESTRIGS_PREFIX + "rip";
    Set<String> configurations = ImmutableSet.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(testrigResourcePrefix, configurations)
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibs();
    Set<AbstractRoute> r1Routes = ribs.get("r1").get(Configuration.DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> r2Routes = ribs.get("r2").get(Configuration.DEFAULT_VRF_NAME).getRoutes();
    Set<AbstractRoute> r3Routes = ribs.get("r3").get(Configuration.DEFAULT_VRF_NAME).getRoutes();
    Set<Prefix> r1Prefixes =
        r1Routes.stream()
            .filter(route -> route.getProtocol() != RoutingProtocol.LOCAL)
            .map(AbstractRoute::getNetwork)
            .collect(Collectors.toSet());
    Set<Prefix> r2Prefixes =
        r2Routes.stream()
            .filter(route -> route.getProtocol() != RoutingProtocol.LOCAL)
            .map(AbstractRoute::getNetwork)
            .collect(Collectors.toSet());
    Set<Prefix> r3Prefixes =
        r3Routes.stream()
            .filter(route -> route.getProtocol() != RoutingProtocol.LOCAL)
            .map(AbstractRoute::getNetwork)
            .collect(Collectors.toSet());
    Prefix prefix1 = Prefix.parse("1.0.0.0/8");
    Prefix prefix2 = Prefix.parse("2.0.0.0/8");
    Prefix prefix3 = Prefix.parse("3.0.0.0/8");

    assertThat(r1Prefixes, containsInAnyOrder(prefix1, prefix2, prefix3));
    assertThat(r2Prefixes, containsInAnyOrder(prefix1, prefix2, prefix3));
    assertThat(r3Prefixes, containsInAnyOrder(prefix1, prefix3));
  }
}
