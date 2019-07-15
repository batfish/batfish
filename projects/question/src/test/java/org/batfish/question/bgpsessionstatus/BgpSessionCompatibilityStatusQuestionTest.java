package org.batfish.question.bgpsessionstatus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.junit.Test;

/** Tests for {@link BgpSessionCompatibilityQuestion} */
public class BgpSessionCompatibilityStatusQuestionTest {

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
            null, null, ConfiguredSessionStatus.UNIQUE_MATCH.toString(), null);

    for (ConfiguredSessionStatus status : ConfiguredSessionStatus.values()) {
      if (status == ConfiguredSessionStatus.UNIQUE_MATCH) {
        assertTrue(status.toString(), q.matchesStatus(status));
      } else {
        assertFalse(status.toString(), q.matchesStatus(status));
      }
    }
  }
}
