package org.batfish.common;

import static org.batfish.common.Warnings.TAG_RED_FLAG;
import static org.batfish.common.Warnings.TAG_UNIMPLEMENTED;
import static org.batfish.common.matchers.WarningsMatchers.hasPedanticWarnings;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.common.matchers.WarningsMatchers.hasUnimplementedWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests of {@link Warnings}. */
public class WarningsTest {
  @Test
  public void testRedFlagWarnings() {
    Warnings ws = new Warnings(true, true, true);
    String message1 = "message1";
    String message2 = "message2";

    // Add one warning twice and another once
    ws.redFlag(message1);
    ws.redFlag(message2);
    ws.redFlag(message1);

    // Only unique warnings should show up
    assertThat(
        ws,
        hasRedFlags(
            contains(
                equalTo(new Warning(message1, TAG_RED_FLAG)),
                equalTo(new Warning(message2, TAG_RED_FLAG)))));
  }

  @Test
  public void testPedanticWarnings() {
    Warnings ws = new Warnings(true, true, true);
    String message1 = "message1";
    String message2 = "message2";
    String tag1 = "tag1";
    String tag2 = "tag2";

    // Add one warning twice and another once
    ws.pedantic(message1, tag1);
    ws.pedantic(message2, tag2);
    ws.pedantic(message1, tag1);

    // Only unique warnings should show up
    assertThat(
        ws,
        hasPedanticWarnings(
            contains(equalTo(new Warning(message1, tag1)), equalTo(new Warning(message2, tag2)))));
  }

  @Test
  public void testUnimplementedWarnings() {
    Warnings ws = new Warnings(true, true, true);
    String message1 = "message1";
    String message2 = "message2";

    // Add one warning twice and another once
    ws.unimplemented(message1);
    ws.unimplemented(message2);
    ws.unimplemented(message1);

    // Only unique warnings should show up
    assertThat(
        ws,
        hasUnimplementedWarnings(
            contains(
                equalTo(new Warning(message1, TAG_UNIMPLEMENTED)),
                equalTo(new Warning(message2, TAG_UNIMPLEMENTED)))));
  }
}
