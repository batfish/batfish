package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.type.TypeReference;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

class InterfaceLocationTest {
  @Test
  void testClone() {
    InterfaceLocation orig = new InterfaceLocation("foo", "bar");
    assertThat(orig, equalTo(BatfishObjectMapper.clone(orig, new TypeReference<Location>() {})));
  }
}
