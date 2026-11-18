package org.batfish.question.bgpsessionstatus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.junit.jupiter.api.Test;

/** Tests for {@link BgpSessionCompatibilityQuestion} */
class BgpSessionCompatibilityStatusQuestionTest {

  /** Null status should match everything */
  @Test
  void testMatchesStatusNullStatus() {
    BgpSessionCompatibilityQuestion q = new BgpSessionCompatibilityQuestion(null, null, null, null);

    for (ConfiguredSessionStatus status : ConfiguredSessionStatus.values()) {
      assertTrue(q.matchesStatus(status), status.toString());
    }
  }

  /** Specific status string should match specific statuses */
  @Test
  void testMatchesStatusNonNullStatus() {
    BgpSessionCompatibilityQuestion q =
        new BgpSessionCompatibilityQuestion(
            null, null, ConfiguredSessionStatus.UNIQUE_MATCH.toString(), null);

    for (ConfiguredSessionStatus status : ConfiguredSessionStatus.values()) {
      if (status == ConfiguredSessionStatus.UNIQUE_MATCH) {
        assertTrue(q.matchesStatus(status), status.toString());
      } else {
        assertFalse(q.matchesStatus(status), status.toString());
      }
    }
  }
}
