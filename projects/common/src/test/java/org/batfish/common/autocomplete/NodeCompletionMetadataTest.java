package org.batfish.common.autocomplete;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test for {@link NodeCompletionMetadata}. */
final class NodeCompletionMetadataTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new NodeCompletionMetadata("a"), new NodeCompletionMetadata("a"))
        .addEqualityGroup(new NodeCompletionMetadata(null))
        .testEquals();
  }

  @Test
  void testJsonSerialization() {
    NodeCompletionMetadata metadata = new NodeCompletionMetadata("a");
    NodeCompletionMetadata clone =
        BatfishObjectMapper.clone(metadata, NodeCompletionMetadata.class);
    assertEquals(metadata, clone);
  }

  @Test
  void testJavaSerialization() {
    NodeCompletionMetadata metadata = new NodeCompletionMetadata("a");
    assertThat(SerializationUtils.clone(metadata), equalTo(metadata));
  }
}
