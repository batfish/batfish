package org.batfish.common.util;

import static org.batfish.common.util.CommonUtil.asNegativeIpWildcards;
import static org.batfish.common.util.CommonUtil.asPositiveIpWildcards;
import static org.batfish.common.util.CommonUtil.communityStringToLong;
import static org.batfish.common.util.CommonUtil.longToCommunity;
import static org.batfish.common.util.CommonUtil.toOrderedHashCode;
import static org.batfish.common.util.CommonUtil.toUnorderedHashCode;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
  public void testToOrderedHashCode() {
    assertThat(Stream.of().collect(toOrderedHashCode()), equalTo(ImmutableList.of().hashCode()));
    assertThat(Stream.of(1).collect(toOrderedHashCode()), equalTo(ImmutableList.of(1).hashCode()));
    assertThat(
        Stream.of(1, 2).collect(toOrderedHashCode()), equalTo(ImmutableList.of(1, 2).hashCode()));
    assertThat(
        Stream.of(2, 1).collect(toOrderedHashCode()), equalTo(ImmutableList.of(2, 1).hashCode()));
  }

  @Test
  public void testToUnorderedHashCode() {
    assertThat(Stream.of().collect(toUnorderedHashCode()), equalTo(ImmutableSet.of().hashCode()));
    assertThat(Stream.of(1).collect(toUnorderedHashCode()), equalTo(ImmutableSet.of(1).hashCode()));
    assertThat(
        Stream.of(1, 2).collect(toUnorderedHashCode()), equalTo(ImmutableSet.of(2, 1).hashCode()));
  }
}
