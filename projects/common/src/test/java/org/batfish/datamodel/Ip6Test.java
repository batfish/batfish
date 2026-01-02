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
        equalTo(Prefix6.create(Ip6.parse("2:8a64:c74e:8a27:f09a:1:2:3"), 128)));
  }

  @Test
  public void testToString() {
    assertThat("zero address", Ip6.parse("::").toString(), equalTo("::"));
    assertThat("leading zero", Ip6.parse("0:1:2:3:4:5:6:7").toString(), equalTo("0:1:2:3:4:5:6:7"));
    assertThat(
        "trailing zero", Ip6.parse("1:2:3:4:5:6:7:0").toString(), equalTo("1:2:3:4:5:6:7:0"));
    assertThat("leading zeros", Ip6.parse("0:0:0:5:0:0:6:0").toString(), equalTo("::5:0:0:6:0"));
    assertThat("longest zeros", Ip6.parse("4:0:0:5:0:0:0:0").toString(), equalTo("4:0:0:5::"));
    assertThat("longest zeros", Ip6.parse("4:0:0:5:6:0:0:0").toString(), equalTo("4:0:0:5:6::"));
    assertThat("tie-break left", Ip6.parse("4:0:0:5:6:7:0:0").toString(), equalTo("4::5:6:7:0:0"));
    assertThat("localhost", Ip6.parse("::1").toString(), equalTo("::1"));
  }
}
