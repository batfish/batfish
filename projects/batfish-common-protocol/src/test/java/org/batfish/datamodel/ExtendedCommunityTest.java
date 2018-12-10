package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public final class ExtendedCommunityTest {

  @Test
  public void testJsonSerialization() throws IOException {
    ExtendedCommunity ec = new ExtendedCommunity(123L);

    assertThat(BatfishObjectMapper.clone(ec, ExtendedCommunity.class), equalTo(ec));
  }

  @Test
  public void testJavaSerialization() {
    ExtendedCommunity ec = new ExtendedCommunity(123L);

    assertThat(SerializationUtils.clone(ec), equalTo(ec));
  }
}
