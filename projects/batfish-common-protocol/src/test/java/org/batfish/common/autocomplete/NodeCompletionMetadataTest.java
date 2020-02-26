package org.batfish.common.autocomplete;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
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
  public void testJsonSerialization() throws IOException {
    NodeCompletionMetadata metadata = new NodeCompletionMetadata("a");
    NodeCompletionMetadata clone =
        BatfishObjectMapper.clone(metadata, NodeCompletionMetadata.class);
    assertEquals(metadata, clone);
  }
}
