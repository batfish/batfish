package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.junit.Test;

/** Tests for {@link BgpSessionCompatibilityQuestion} */
public class BgpSessionCompatibilityQuestionTest {

  @Test
  public void testDefaultParameters() {
    BgpSessionCompatibilityQuestion question = new BgpSessionCompatibilityQuestion();

    assertThat(question.getDataPlane(), equalTo(false));
    assertThat(question.getNodeSpecifier(), equalTo(AllNodesNodeSpecifier.INSTANCE));
    assertThat(question.getRemoteNodeSpecifier(), equalTo(AllNodesNodeSpecifier.INSTANCE));
    assertThat(question.getName(), equalTo("bgpSessionCompatibility"));
  }

  @Test
  public void testJsonSerialization() {
    BgpSessionCompatibilityQuestion q =
        new BgpSessionCompatibilityQuestion(
            "nodes",
            "remoteNodes",
            ConfiguredSessionStatus.NO_REMOTE_AS.toString(),
            SessionType.EBGP_SINGLEHOP.toString());
    assertThat(BatfishObjectMapper.clone(q, BgpSessionCompatibilityQuestion.class), equalTo(q));
  }

  /** Null status should match everything */
  @Test
  public void testMatchesStatusNullStatus() {
    BgpSessionCompatibilityQuestion q = new BgpSessionCompatibilityQuestion(null, null, null, null);

    for (ConfiguredSessionStatus status : ConfiguredSessionStatus.values()) {
      assertTrue(status.toString(), q.matchesStatus(status));
    }
  }

  /** Specific status string should match specific statuses */
  @Test
  public void testMatchesStatusNonNullStatus() {
    BgpSessionCompatibilityQuestion q =
        new BgpSessionCompatibilityQuestion(
            null, null, ConfiguredSessionStatus.NO_REMOTE_AS.toString(), null);

    for (ConfiguredSessionStatus status : ConfiguredSessionStatus.values()) {
      if (status == ConfiguredSessionStatus.NO_REMOTE_AS) {
        assertTrue(status.toString(), q.matchesStatus(status));
      } else {
        assertFalse(status.toString(), q.matchesStatus(status));
      }
    }
  }
}
