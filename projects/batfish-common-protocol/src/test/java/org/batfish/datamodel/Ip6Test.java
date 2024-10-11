package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Ip6}. */
@RunWith(JUnit4.class)
public class Ip6Test {

  @Test
  public void testToPrefix6() {
    assertThat(
        Ip6.parse("2:8a64:c74e:8a27:f09a:1:2:3").toPrefix6(),
        equalTo(new Prefix6(Ip6.parse("2:8a64:c74e:8a27:f09a:1:2:3"), 128)));
  }
}
