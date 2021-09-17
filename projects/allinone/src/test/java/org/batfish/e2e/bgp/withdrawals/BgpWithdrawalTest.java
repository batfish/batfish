package org.batfish.e2e.bgp.withdrawals;

import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasAsPath;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Table;
import java.io.IOException;
import java.util.Set;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** A test that learned and exported BGP routes can be successfully withdrawn and replaced. */
public class BgpWithdrawalTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /**
   * Set up a 5-node topology as follows:
   *
   * <pre>
   *     A*--S--M1--M2--B*
   *         |
   *         L
   * </pre>
   *
   * <ul>
   *   <li>Nodes, A [AS 1] and B [AS 3] both originate a route for 5.5.5.5/32.
   *   <li>At the sender, S [AS 2], the route from A is learned first. Later, the route from B is
   *       learned, and replaces the route from A.
   *   <li>We make the route from B learned later by moving B two hops away from S, through M [AS
   *       4]. We need two hops to guarantee L has received the route from A no matter the schedule.
   *   <li>Although B->M1->M2->S has a longer AS-Path than A->S, S prefers routes learned from M1,
   *       set via weight in route-map in.
   *   <li>We also have a listener, L, receiving from S. It should 1) learn the route from A->S, 2)
   *       have that route withdrawn and replaced by B->M->S later, when S replaces
   * </ul>
   *
   * The test is to confirm that L has the right route, and only the right route.
   *
   * <p>We have two intermediate nodes M1 and M2 to delay the route from B, ensuring that S sends
   * A's route to L before S learns B's route. This guarantees that S will send a withdrawal to L,
   * rather than only having sent B's route. This is finicky based on schedule.
   */
  @Test
  public void testWithdrawals() throws IOException {
    String prefix = "org/batfish/e2e/bgp/withdrawals";
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(prefix, "a", "b", "m1", "m2", "s", "l")
                .build(),
            _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.loadConfigurations(snapshot);
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    Table<String, String, Set<Bgpv4Route>> routes = dp.getBgpRoutes();
    Table<String, String, Set<Bgpv4Route>> backupRoutes = dp.getBgpBackupRoutes();
    // B should choose its route and receives no backup from M2.
    assertThat(routes.get("b", "default"), contains(hasAsPath(equalTo(AsPath.empty()))));
    assertThat(backupRoutes.get("b", "default"), empty());

    // M2 should have the route from B, with no backup from M1.
    assertThat(
        routes.get("m2", "default"), contains(hasAsPath(equalTo(AsPath.ofSingletonAsSets(3L)))));
    assertThat(backupRoutes.get("m2", "default"), empty());

    // M1 should have the route from B->M2, with no backup from S.
    assertThat(
        routes.get("m1", "default"),
        contains(hasAsPath(equalTo(AsPath.ofSingletonAsSets(5L, 3L)))));
    assertThat(backupRoutes.get("m1", "default"), empty());

    // S should prefer B->M2->M1 and have A as a backup.
    assertThat(
        routes.get("s", "default"),
        contains(hasAsPath(equalTo(AsPath.ofSingletonAsSets(4L, 5L, 3L)))));
    assertThat(
        backupRoutes.get("s", "default"),
        contains(hasAsPath(equalTo(AsPath.ofSingletonAsSets(1L)))));

    // A should choose its route, but have the B->M2->M1->S route as a backup.
    assertThat(routes.get("a", "default"), contains(hasAsPath(equalTo(AsPath.empty()))));
    assertThat(
        backupRoutes.get("a", "default"),
        contains(hasAsPath(equalTo(AsPath.ofSingletonAsSets(2L, 4L, 5L, 3L)))));

    // L should have the route B->M2->M1->S and no backup.
    assertThat(
        routes.get("l", "default"),
        containsInAnyOrder(hasAsPath(equalTo(AsPath.ofSingletonAsSets(2L, 4L, 5L, 3L)))));
    assertThat(backupRoutes.get("l", "default"), empty());
  }
}
