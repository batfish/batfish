package org.batfish.question.bgpsessionstatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.BgpSessionStatus;
import org.junit.jupiter.api.Test;

/** Tests for {@link BgpSessionStatusQuestion} */
class BgpSessionStatusQuestionTest {

  @Test
  void testJsonSerialization() {
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
  void testMatchesStatusNullStatus() {
    BgpSessionStatusQuestion q = new BgpSessionStatusQuestion(null, null, null, null);

    for (BgpSessionStatus status : BgpSessionStatus.values()) {
      assertTrue(q.matchesStatus(status), status.toString());
    }
  }

  /** Specific status string should match specific statuses */
  @Test
  void testMatchesStatusNonNullStatus() {
    BgpSessionStatusQuestion q =
        new BgpSessionStatusQuestion(null, null, BgpSessionStatus.NOT_COMPATIBLE.toString(), null);

    for (BgpSessionStatus status : BgpSessionStatus.values()) {
      if (status == BgpSessionStatus.NOT_COMPATIBLE) {
        assertTrue(q.matchesStatus(status), status.toString());
      } else {
        assertFalse(q.matchesStatus(status), status.toString());
      }
    }
  }
}
