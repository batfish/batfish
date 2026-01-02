package org.batfish.common.autocomplete;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test for {@link NodeCompletionMetadata}. */
public final class NodeCompletionMetadataTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new NodeCompletionMetadata("a"), new NodeCompletionMetadata("a"))
        .addEqualityGroup(new NodeCompletionMetadata(null))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    NodeCompletionMetadata metadata = new NodeCompletionMetadata("a");
    NodeCompletionMetadata clone =
        BatfishObjectMapper.clone(metadata, NodeCompletionMetadata.class);
    assertEquals(metadata, clone);
  }

  @Test
  public void testJavaSerialization() {
    NodeCompletionMetadata metadata = new NodeCompletionMetadata("a");
    assertThat(SerializationUtils.clone(metadata), equalTo(metadata));
  }
}
