package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class TagTest {

  @Test
  public void testJsonSerialization() throws IOException {
    Tag tag = Tag.builder().setKey("key").setValue("value").build();
    assertThat(BatfishObjectMapper.clone(tag, Tag.class), equalTo(tag));
  }
}
