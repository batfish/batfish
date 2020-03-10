package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.BgpSessionStatus;
import org.junit.Test;

/** Tests for {@link BgpSessionStatusQuestion} */
public class BgpSessionStatusQuestionTest {

  @Test
  public void testJsonSerialization() {
    BgpSessionStatusQuestion q =
        new BgpSessionStatusQuestion(
            "nodes",
            "remoteNodes",
            BgpSessionStatus.ESTABLISHED.toString(),
            SessionType.EBGP_SINGLEHOP.toString());
    assertThat(BatfishObjectMapper.clone(q, BgpSessionStatusQuestion.class), equalTo(q));
  }

  /** Null status should match everything */
  @Test
  public void testMatchesStatusNullStatus() {
    BgpSessionStatusQuestion q = new BgpSessionStatusQuestion(null, null, null, null);

    for (BgpSessionStatus status : BgpSessionStatus.values()) {
      assertTrue(status.toString(), q.matchesStatus(status));
    }
  }

  /** Specific status string should match specific statuses */
  @Test
  public void testMatchesStatusNonNullStatus() {
    BgpSessionStatusQuestion q =
        new BgpSessionStatusQuestion(null, null, BgpSessionStatus.NOT_COMPATIBLE.toString(), null);

    for (BgpSessionStatus status : BgpSessionStatus.values()) {
      if (status == BgpSessionStatus.NOT_COMPATIBLE) {
        assertTrue(status.toString(), q.matchesStatus(status));
      } else {
        assertFalse(status.toString(), q.matchesStatus(status));
      }
    }
  }
}
