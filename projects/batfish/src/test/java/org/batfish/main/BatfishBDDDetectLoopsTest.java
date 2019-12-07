package org.batfish.main;

import static org.batfish.datamodel.FlowDisposition.LOOP;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.Trace;
import org.batfish.question.loop.LoopNetwork;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link Batfish#bddLoopDetection(NetworkSnapshot)}. */
public class BatfishBDDDetectLoopsTest {
  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();

  Batfish _batfish;

  private void initNetwork(boolean includeLoop) throws IOException {
    SortedMap<String, Configuration> configs = LoopNetwork.testLoopNetwork(includeLoop);
    _batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  @Test
  public void testNoLoops() throws IOException {
    initNetwork(false);
    Set<Flow> flows = _batfish.bddLoopDetection(_batfish.getSnapshot());
    assertThat(flows, empty());
  }

  @Test
  public void testLoops() throws IOException {
    initNetwork(true);
    Set<Flow> flows = _batfish.bddLoopDetection(_batfish.getSnapshot());
    assertThat(flows, hasSize(2));

    SortedMap<Flow, List<Trace>> flowTraces =
        _batfish.getTracerouteEngine(_batfish.getSnapshot()).computeTraces(flows, false);
    Set<FlowDisposition> dispositions =
        flowTraces.values().stream()
            .flatMap(Collection::stream)
            .map(Trace::getDisposition)
            .collect(Collectors.toSet());
    assertThat(dispositions, equalTo(ImmutableSet.of(LOOP)));
  }
}
