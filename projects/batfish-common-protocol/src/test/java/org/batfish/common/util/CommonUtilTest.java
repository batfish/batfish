package org.batfish.common.util;

import static org.batfish.common.util.CommonUtil.asNegativeIpWildcards;
import static org.batfish.common.util.CommonUtil.asPositiveIpWildcards;
import static org.batfish.common.util.CommonUtil.communityStringToLong;
import static org.batfish.common.util.CommonUtil.iPow;
import static org.batfish.common.util.CommonUtil.longToCommunity;
import static org.batfish.common.util.CommonUtil.toHashcode;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;

/** Tests of utility methods from {@link org.batfish.common.util.CommonUtil} */
public class CommonUtilTest {

  /** Test that asPositiveIpWildcards handles null */
  @Test
  public void testAsPositiveIpWildcards() {
    assertThat(asPositiveIpWildcards(null), nullValue());
  }

  /** Test that asNegativeIpWildcards handles null */
  @Test
  public void testAsNegativeIpWildcards() {
    assertThat(asNegativeIpWildcards(null), nullValue());
  }

  @Test
  public void testCommunityStringToLong() {
    assertThat(communityStringToLong("0:0"), equalTo(0L));
    assertThat(communityStringToLong("65535:65535"), equalTo(4294967295L));
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testCommunityStringToLongInvalidInput() {
    communityStringToLong("111");
  }

  @Test(expected = NumberFormatException.class)
  public void testCommunityStringToLongNoInput() {
    communityStringToLong("");
  }

  @Test(expected = IllegalArgumentException.class)
  @Ignore("https://github.com/batfish/batfish/issues/2103")
  public void testCommunityStringHighTooBig() {
    communityStringToLong("65537:1");
  }

  @Test(expected = IllegalArgumentException.class)
  @Ignore("https://github.com/batfish/batfish/issues/2103")
  public void testCommunityStringLowTooBig() {
    communityStringToLong("1:65537");
  }

  @Test
  public void testLongToCommunity() {
    assertThat(longToCommunity(0L), equalTo("0:0"));
    assertThat(longToCommunity(4294967295L), equalTo("65535:65535"));
  }

  @Test
  public void testiPow() {
    assertThat(iPow(0, 0), equalTo(1));
    assertThat(iPow(-3, 0), equalTo(1));
    assertThat(iPow(0, 1), equalTo(0));
    assertThat(iPow(1, 1), equalTo(1));
    assertThat(iPow(1, 999), equalTo(1));
    assertThat(iPow(2, 2), equalTo(4));
    assertThat(iPow(2, 3), equalTo(8));
    assertThat(iPow(46340, 2), equalTo(2147395600));
    assertThat(iPow(-2, 2), equalTo(4));
    assertThat(iPow(-2, 3), equalTo(-8));
  }

  @Test
  public void testToHashcode() {
    assertThat(Stream.of().collect(toHashcode()), equalTo(1));
    assertThat(Stream.of(1).collect(toHashcode()), equalTo(ImmutableList.of(1).hashCode()));
    assertThat(Stream.of(1, 2).collect(toHashcode()), equalTo(ImmutableList.of(1, 2).hashCode()));
    assertThat(Stream.of(2, 1).collect(toHashcode()), equalTo(ImmutableList.of(2, 1).hashCode()));
  }
}
