package org.batfish.common;

import static org.batfish.common.Warnings.TODO_COMMENT;
import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import org.batfish.common.Warnings.ParseWarning;
import org.junit.Test;

/** Tests of {@link org.batfish.common.matchers.ParseWarningMatchers}. */
public final class ParseWarningMatchersTest {

  @Test
  public void testIsTodo() {
    ParseWarning todo = new ParseWarning(1, "text", "context", TODO_COMMENT);
    assertThat(todo, isTodo());
    assertThat(todo, isTodo("text"));
    assertThat(todo, not(isTodo("other text")));
    assertThat(new ParseWarning(1, "text", "context", "other comment"), not(isTodo()));
  }
}
