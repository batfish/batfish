package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.identifiers.SnapshotId;
import org.batfish.job.FinalizeShim;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** The injection seam: hand-built models stored through storage get the full load path. */
public final class ModelInjectorTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private EngineBatfish engine() throws Exception {
    return new EngineBatfish(_folder.newFolder().toPath(), 4);
  }

  private static Set<AbstractRoute> ospfRoutes(DataPlane dp, String node, String prefix) {
    return dp
        .getRibs()
        .get(node, Configuration.DEFAULT_VRF_NAME)
        .getRoutes(Prefix.parse(prefix))
        .stream()
        .filter(r -> r.getProtocol() == RoutingProtocol.OSPF)
        .collect(Collectors.toSet());
  }

  @Test
  public void injectedOspfNetworkConverges() throws Exception {
    EngineBatfish engine = engine();
    SnapshotId snap = EngineBatfish.freshSnapshotId("seam");

    ModelInjector.Result result =
        new ModelInjector(engine).inject(snap, TwoRouterOspf.build(), null);

    assertThat(result.invalid(), empty());
    Batfish bf = engine.batfish(snap, null);
    bf.computeDataPlane(EngineBatfish.snapshot(snap));
    DataPlane dp = bf.loadDataPlane(EngineBatfish.snapshot(snap));
    assertThat(ospfRoutes(dp, TwoRouterOspf.R1, TwoRouterOspf.R2_LOOPBACK), hasSize(1));
    assertThat(ospfRoutes(dp, TwoRouterOspf.R2, TwoRouterOspf.R1_LOOPBACK), hasSize(1));
  }

  /**
   * Regression guard on the seam choice: the same models seeded straight into the config cache skip
   * post-processing, so OSPF neighbors are never initialized and no OSPF route appears.
   */
  @Test
  public void cacheSeededOspfNetworkDoesNotConverge() throws Exception {
    EngineBatfish engine = engine();
    SnapshotId snap = EngineBatfish.freshSnapshotId("cache");
    SortedMap<String, Configuration> configs = TwoRouterOspf.build();
    configs.values().forEach(c -> FinalizeShim.finalizeConfiguration(c, new Warnings()));
    engine.seedConfigurationCacheForTesting(snap, configs);

    Batfish bf = engine.batfish(snap, null);
    bf.computeDataPlane(EngineBatfish.snapshot(snap));
    DataPlane dp = bf.loadDataPlane(EngineBatfish.snapshot(snap));

    assertThat(ospfRoutes(dp, TwoRouterOspf.R1, TwoRouterOspf.R2_LOOPBACK), empty());
  }

  @Test
  public void undefinedPolicyReferenceIsRefusedAndNothingIsStored() throws Exception {
    EngineBatfish engine = engine();
    SnapshotId snap = EngineBatfish.freshSnapshotId("invalid");
    SortedMap<String, Configuration> configs = TwoRouterOspf.build();
    Configuration r1 = configs.get(TwoRouterOspf.R1);
    RoutingPolicy.builder()
        .setOwner(r1)
        .setName("caller")
        .setStatements(
            ImmutableList.of(
                new If(
                    new CallExpr("missing-policy"),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()))))
        .build();

    ModelInjector.Result result = new ModelInjector(engine).inject(snap, configs, null);

    assertThat(result.ok(), equalTo(false));
    assertThat(
        result.invalid().stream().map(ModelInjector.Invalid::node).toList(),
        contains(TwoRouterOspf.R1));
    assertThat(result.invalid().get(0).reason(), containsString("missing-policy"));
    Path snapshotDir =
        engine
            .storageBase()
            .resolve("networks")
            .resolve(EngineBatfish.NETWORK.getId())
            .resolve("snapshots")
            .resolve(snap.getId());
    assertThat(Files.exists(snapshotDir), equalTo(false));
  }

  @Test
  public void mapKeyMustEqualHostname() throws Exception {
    EngineBatfish engine = engine();
    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of("other-name", TwoRouterOspf.build().get(TwoRouterOspf.R1));

    ModelInjector.Result result =
        new ModelInjector(engine).inject(EngineBatfish.freshSnapshotId("key"), configs, null);

    assertThat(result.invalid(), hasSize(1));
  }

  /**
   * Batfish's topology caches are static. Two revisions of the network in one JVM must each be
   * computed against their own topology: here r2 moves off the shared subnet in the second
   * revision, so r1 must lose the OSPF route it had in the first.
   */
  @Test
  public void revisionsInOneJvmDoNotShareTopology() throws Exception {
    EngineBatfish engine = engine();
    ModelInjector injector = new ModelInjector(engine);
    SnapshotId before = EngineBatfish.freshSnapshotId("rev");
    SnapshotId after = EngineBatfish.freshSnapshotId("rev");
    assertThat(injector.inject(before, TwoRouterOspf.build(), null).ok(), equalTo(true));
    SortedMap<String, Configuration> moved = TwoRouterOspf.build();
    ConcreteInterfaceAddress elsewhere = ConcreteInterfaceAddress.parse("10.99.0.2/30");
    Interface r2Link = moved.get(TwoRouterOspf.R2).getAllInterfaces().get("Ethernet1");
    r2Link.setAddress(elsewhere);
    r2Link.setAllAddresses(ImmutableList.of(elsewhere));
    assertThat(injector.inject(after, moved, null).ok(), equalTo(true));

    Batfish bfBefore = engine.batfish(before, null);
    bfBefore.computeDataPlane(EngineBatfish.snapshot(before));
    Batfish bfAfter = engine.batfish(after, null);
    bfAfter.computeDataPlane(EngineBatfish.snapshot(after));

    assertThat(
        ospfRoutes(
            bfBefore.loadDataPlane(EngineBatfish.snapshot(before)),
            TwoRouterOspf.R1,
            TwoRouterOspf.R2_LOOPBACK),
        hasSize(1));
    assertThat(
        ospfRoutes(
            bfAfter.loadDataPlane(EngineBatfish.snapshot(after)),
            TwoRouterOspf.R1,
            TwoRouterOspf.R2_LOOPBACK),
        empty());
  }

  @Test
  public void snapshotIdsAreNeverReused() {
    assertThat(
        EngineBatfish.freshSnapshotId("x"), not(equalTo(EngineBatfish.freshSnapshotId("x"))));
  }
}
