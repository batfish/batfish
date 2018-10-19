package org.batfish.common.util;

import static org.batfish.common.util.CommonUtil.asNegativeIpWildcards;
import static org.batfish.common.util.CommonUtil.asPositiveIpWildcards;
import static org.batfish.common.util.CommonUtil.communityStringToLong;
import static org.batfish.common.util.CommonUtil.longToCommunity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Ignore;
import org.junit.Test;

/** Tests of utility methods from {@link org.batfish.common.util.CommonUtil} */
public class CommonUtilTest {

  /** Test that asPostiveIpWildcards handles null */
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
}
