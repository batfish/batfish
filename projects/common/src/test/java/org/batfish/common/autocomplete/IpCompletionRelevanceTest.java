package org.batfish.common.autocomplete;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class IpCompletionRelevanceTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IpCompletionRelevance("a", "b"),
            new IpCompletionRelevance("a", ImmutableList.of("b")))
        .addEqualityGroup(new IpCompletionRelevance("other", "b"))
        .addEqualityGroup(new IpCompletionRelevance("a", "other"))
        .testEquals();
  }

  @Test
  public void testConstructorNullAndEmptyFiltering() {
    new EqualsTester()
        .addEqualityGroup(
            new IpCompletionRelevance("a"),
            new IpCompletionRelevance("a", ImmutableList.of()),
            new IpCompletionRelevance("a", (String) null),
            new IpCompletionRelevance("a", ""))
        .addEqualityGroup(
            new IpCompletionRelevance("a", "b", null),
            new IpCompletionRelevance("a", ImmutableList.of("b")),
            new IpCompletionRelevance("a", "b"),
            new IpCompletionRelevance("a", "", "b"))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    IpCompletionRelevance metadata = new IpCompletionRelevance("a", "b");
    IpCompletionRelevance clone = BatfishObjectMapper.clone(metadata, IpCompletionRelevance.class);
    assertEquals(metadata, clone);
  }

  @Test
  public void testJavaSerialization() {
    IpCompletionRelevance metadata = new IpCompletionRelevance("a", "b");
    assertThat(SerializationUtils.clone(metadata), equalTo(metadata));
  }

  @Test
  public void testMatches() {
    IpCompletionRelevance relevance = new IpCompletionRelevance("a", "tag1", "gat1");
    Ip testIp = Ip.parse("1.1.1.1");

    // each subquery matches ip or tags
    assertTrue(relevance.matches(new String[] {"a"}, testIp));
    assertTrue(relevance.matches(new String[] {"tag", "gat"}, testIp));
    assertTrue(relevance.matches(new String[] {"1.1"}, testIp));
    assertTrue(relevance.matches(new String[] {"1.1", "tag"}, testIp));

    // does not match at least one tag
    assertFalse(relevance.matches(new String[] {"b"}, testIp));
    assertFalse(relevance.matches(new String[] {"tag", "b"}, testIp));
  }

  @Test
  public void testMatches_caseInsensitive() {
    IpCompletionRelevance relevance = new IpCompletionRelevance("a", "TAG");
    Ip testIp = Ip.parse("1.1.1.1");

    assertTrue(relevance.matches(new String[] {"tag"}, testIp));
  }
}
