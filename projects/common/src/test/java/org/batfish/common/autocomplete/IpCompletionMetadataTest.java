package org.batfish.common.autocomplete;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class IpCompletionMetadataTest {

  @Test
  public void testEquals() {
    IpCompletionRelevance rel1 = new IpCompletionRelevance("display", ImmutableList.of("tag"));
    IpCompletionRelevance rel2 = new IpCompletionRelevance("display", ImmutableList.of("other"));
    new EqualsTester()
        .addEqualityGroup(
            new IpCompletionMetadata(null, ImmutableList.of(rel1)),
            new IpCompletionMetadata(null, ImmutableList.of(rel1)))
        .addEqualityGroup(new IpCompletionMetadata(null, ImmutableList.of(rel2)))
        .addEqualityGroup(new IpCompletionMetadata(ImmutableRangeSet.of(), ImmutableList.of(rel1)))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    IpCompletionMetadata metadata = new IpCompletionMetadata(new IpCompletionRelevance("a"));
    assertThat(SerializationUtils.clone(metadata), equalTo(metadata));
  }
}
