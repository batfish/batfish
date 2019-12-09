package org.batfish.bddreachability;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.util.TracePruner;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.multipath.MultipathConsistencyParameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BDDMultipathInonsistencyWithLoop {

  @Rule public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testLoopInMultipathConsistency() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfish(MPIWithLoopNetwork.testMPIWithLoopNetwork(), temp);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    MultipathConsistencyParameters multipathConsistencyParameters =
        new MultipathConsistencyParameters(
            AclLineMatchExprs.TRUE,
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(snapshot),
            batfish.loadConfigurations(snapshot).keySet(),
            ImmutableSet.of(),
            TracePruner.DEFAULT_MAX_TRACES,
            ImmutableSet.of());

    Set<Flow> flows = batfish.bddMultipathConsistency(snapshot, multipathConsistencyParameters);

    // Flows starting from configuration 1 and configuration 2 should result in inconsistent
    // dispositions
    assertThat(flows, hasSize(2));

    Map<Flow, List<Trace>> flowTraces = batfish.buildFlows(snapshot, flows, false);

    // each flow should give out a list of traces where the list will have inconsistent dispositions
    for (List<Trace> tracePerFlow : flowTraces.values()) {
      Set<FlowDisposition> flowDispositions =
          tracePerFlow.stream().map(Trace::getDisposition).collect(ImmutableSet.toImmutableSet());

      assertThat(
          flowDispositions, containsInAnyOrder(FlowDisposition.ACCEPTED, FlowDisposition.LOOP));
    }
  }
}
