package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class InterfaceLocationTest {
  @Test
  public void testClone() throws IOException {
    InterfaceLocation orig = new InterfaceLocation("foo", "bar");
    assertThat(orig, equalTo(BatfishObjectMapper.clone(orig, new TypeReference<Location>() {})));
  }
}
