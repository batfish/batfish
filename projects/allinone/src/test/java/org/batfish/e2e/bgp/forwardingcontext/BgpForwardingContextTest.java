package org.batfish.e2e.bgp.forwardingcontext;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.BgpSessionStatus;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer;
import org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityQuestion;
import org.batfish.question.bgpsessionstatus.BgpSessionStatusAnswerer;
import org.batfish.question.bgpsessionstatus.BgpSessionStatusQuestion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * End-to-end test for Junos {@code forwarding-context master}. Two routers each have a BGP session
 * configured in a non-default VRF (MY-VRF) but sourced from the default VRF via forwarding-context.
 * The local IPs (loopbacks) are in the default VRF. Without forwarding-context support, these
 * sessions would be reported as INVALID_LOCAL_IP.
 */
public class BgpForwardingContextTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final String PREFIX = "org/batfish/e2e/bgp/forwardingcontext";

  private Batfish getBatfish() throws IOException {
    return BatfishTestUtils.getBatfishFromTestrigText(
        TestrigText.builder().setConfigurationFiles(PREFIX, "r1", "r2").build(), _folder);
  }

  /** Sessions should be compatible (UNIQUE_MATCH), not INVALID_LOCAL_IP. */
  @Test
  public void testSessionCompatibility() throws IOException {
    Batfish batfish = getBatfish();
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.loadConfigurations(snapshot);

    TableAnswerElement answer =
        (TableAnswerElement)
            new BgpSessionCompatibilityAnswerer(new BgpSessionCompatibilityQuestion(), batfish)
                .answer(snapshot);

    // Both sessions (r1->r2 and r2->r1) should be UNIQUE_MATCH, not INVALID_LOCAL_IP
    assertThat(
        answer.getRowsList(),
        everyItem(hasColumn("Configured_Status", "UNIQUE_MATCH", Schema.STRING)));
    assertThat(
        answer.getRowsList(),
        not(hasItem(hasColumn("Configured_Status", "INVALID_LOCAL_IP", Schema.STRING))));

    // Session_VRF column should show "default" (the session VRF)
    assertThat(answer.getRowsList(), everyItem(hasColumn("Session_VRF", "default", Schema.STRING)));
  }

  /** Sessions should establish (reachable through default VRF). */
  @Test
  public void testSessionEstablishment() throws IOException {
    Batfish batfish = getBatfish();
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.loadConfigurations(snapshot);
    batfish.computeDataPlane(snapshot);

    TableAnswerElement answer =
        (TableAnswerElement)
            new BgpSessionStatusAnswerer(new BgpSessionStatusQuestion(), batfish).answer(snapshot);

    // Both sessions should be ESTABLISHED
    assertThat(
        answer.getRowsList(),
        everyItem(
            hasColumn(
                "Established_Status", BgpSessionStatus.ESTABLISHED.toString(), Schema.STRING)));
  }

  /** Peers in the default VRF should not have sessionVrf set. */
  @Test
  public void testDefaultVrfPeersNoSessionVrf() throws IOException {
    Batfish batfish = getBatfish();
    NetworkSnapshot snapshot = batfish.getSnapshot();
    var configs = batfish.loadConfigurations(snapshot);

    // Default VRF should have no BGP process (BGP is only in MY-VRF)
    assertThat(configs.get("r1").getDefaultVrf().getBgpProcess(), nullValue());
    assertThat(configs.get("r2").getDefaultVrf().getBgpProcess(), nullValue());

    // MY-VRF peers should have sessionVrf = "default"
    assertThat(
        configs
            .get("r1")
            .getVrfs()
            .get("MY-VRF")
            .getBgpProcess()
            .getActiveNeighbors()
            .values()
            .iterator()
            .next()
            .getSessionVrf(),
        equalTo("default"));
  }
}
