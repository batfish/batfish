package org.batfish.datamodel.answers;

import static org.batfish.datamodel.answers.ParseStatus.FAILED;
import static org.batfish.datamodel.answers.ParseStatus.PARTIALLY_UNRECOGNIZED;
import static org.batfish.datamodel.answers.ParseStatus.PASSED;
import static org.batfish.datamodel.answers.ParseStatus.UNKNOWN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/** Tests of {@link ParseStatus}. */
public class ParseStatusTest {

  @Test
  public void resolve() {
    assertThat(ParseStatus.resolve(FAILED, UNKNOWN), is(FAILED));
    assertThat(ParseStatus.resolve(UNKNOWN, FAILED), is(FAILED));

    assertThat(ParseStatus.resolve(FAILED, PASSED), is(FAILED));
    assertThat(ParseStatus.resolve(PASSED, FAILED), is(FAILED));

    assertThat(ParseStatus.resolve(FAILED, PARTIALLY_UNRECOGNIZED), is(FAILED));
    assertThat(ParseStatus.resolve(PARTIALLY_UNRECOGNIZED, FAILED), is(FAILED));

    assertThat(ParseStatus.resolve(PASSED, PARTIALLY_UNRECOGNIZED), is(PARTIALLY_UNRECOGNIZED));
    assertThat(ParseStatus.resolve(PARTIALLY_UNRECOGNIZED, PASSED), is(PARTIALLY_UNRECOGNIZED));
  }
}
