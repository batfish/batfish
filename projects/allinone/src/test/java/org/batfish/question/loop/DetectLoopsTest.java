package org.batfish.question.loop;

import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.util.TracePruner;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DetectLoopsTest {
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
    DetectLoopsAnswerer answerer =
        new DetectLoopsAnswerer(new DetectLoopsQuestion(TracePruner.DEFAULT_MAX_TRACES), _batfish);
    TableAnswerElement ae = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());
    assertThat(ae.getRows(), hasSize(0));
  }

  @Test
  public void testLoops() throws IOException {
    initNetwork(true);
    Set<Flow> flows = _batfish.bddLoopDetection(_batfish.getSnapshot());
    assertThat(flows, Matchers.hasSize(2));

    DetectLoopsAnswerer answerer =
        new DetectLoopsAnswerer(new DetectLoopsQuestion(TracePruner.DEFAULT_MAX_TRACES), _batfish);
    TableAnswerElement ae = (TableAnswerElement) answerer.answer(_batfish.getSnapshot());

    // we find 2 loopy flows, but they are for the same destination, so the answerer
    // only reports 1.
    assertThat(ae.getRows(), hasSize(1));
  }
}
