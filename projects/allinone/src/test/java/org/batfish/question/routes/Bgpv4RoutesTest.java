package org.batfish.question.routes;

import static org.batfish.datamodel.RoutingProtocol.IBGP;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.routes.RoutesAnswerer.COL_LOCAL_PREF;
import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK;
import static org.batfish.question.routes.RoutesAnswerer.COL_PROTOCOL;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.BgpRouteStatus;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test of routes answer for BGP routes */
public final class Bgpv4RoutesTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final String NODE1 = "r1";
  private static final String NODE2 = "r2";
  private static final String TESTRIG_NAME = "bgpv4-routes";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of(NODE1, NODE2);
  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";

  private Batfish _batfish;

  @Before
  public void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                .build(),
            _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  @Test
  public void testBestRoutes() {
    RoutesQuestion question =
        new RoutesQuestion(null, NODE1, null, null, BgpRouteStatus.BEST.name(), RibProtocol.BGP);
    TableAnswerElement answer =
        (TableAnswerElement) new RoutesAnswerer(question, _batfish).answer(_batfish.getSnapshot());
    assertThat(
        answer.getRowsList(),
        hasItem(
            allOf(
                hasColumn(COL_NETWORK, Prefix.strict("5.5.5.5/32"), Schema.PREFIX),
                hasColumn(COL_LOCAL_PREF, 1000, Schema.INTEGER))));
  }

  @Test
  public void testBackupRoutes() {
    RoutesQuestion question =
        new RoutesQuestion(null, NODE1, null, null, BgpRouteStatus.BACKUP.name(), RibProtocol.BGP);
    TableAnswerElement answer =
        (TableAnswerElement) new RoutesAnswerer(question, _batfish).answer(_batfish.getSnapshot());
    assertThat(
        answer.getRowsList(),
        contains(
            allOf(
                hasColumn(COL_NETWORK, Prefix.strict("5.5.5.5/32"), Schema.PREFIX),
                hasColumn(COL_PROTOCOL, IBGP.protocolName(), Schema.STRING),
                hasColumn(COL_LOCAL_PREF, 100, Schema.INTEGER))));
  }
}
