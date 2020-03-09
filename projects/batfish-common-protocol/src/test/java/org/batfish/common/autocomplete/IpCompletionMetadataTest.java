package org.batfish.common.autocomplete;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class IpCompletionMetadataTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IpCompletionMetadata(
                ImmutableList.of(new IpCompletionRelevance("display", ImmutableList.of("tag")))),
            new IpCompletionMetadata(
                ImmutableList.of(new IpCompletionRelevance("display", ImmutableList.of("tag")))))
        .addEqualityGroup(
            new IpCompletionMetadata(
                ImmutableList.of(new IpCompletionRelevance("display", ImmutableList.of("other")))))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    IpCompletionMetadata metadata = new IpCompletionMetadata(new IpCompletionRelevance("a"));
    IpCompletionMetadata clone = BatfishObjectMapper.clone(metadata, IpCompletionMetadata.class);
    assertEquals(metadata, clone);
  }

  @Test
  public void testJavaSerialization() {
    IpCompletionMetadata metadata = new IpCompletionMetadata(new IpCompletionRelevance("a"));
    assertThat(SerializationUtils.clone(metadata), equalTo(metadata));
  }
}
