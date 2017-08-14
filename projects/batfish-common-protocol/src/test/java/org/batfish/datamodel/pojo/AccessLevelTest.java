package org.batfish.datamodel.pojo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests for {@link AccessLevel}. */
public class AccessLevelTest {
  @Test
  public void testNextLevel() {
    AccessLevel accessLevel = AccessLevel.FULL;
    assertThat(accessLevel.nextLevel(), equalTo(AccessLevel.FULL));
    accessLevel = AccessLevel.SUMMARY;
    assertThat(accessLevel.nextLevel(), equalTo(AccessLevel.ONELINE));
    accessLevel = accessLevel.nextLevel();
    assertThat(accessLevel.nextLevel(), equalTo(AccessLevel.ONELINE));
  }
}
