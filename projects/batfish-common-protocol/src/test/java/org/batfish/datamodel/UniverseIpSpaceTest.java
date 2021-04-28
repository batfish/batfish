package org.batfish.datamodel;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class UniverseIpSpaceTest {
  @Test
  public void testInterning() {
    assertThat(
        BatfishObjectMapper.clone(UniverseIpSpace.INSTANCE, IpSpace.class),
        sameInstance(UniverseIpSpace.INSTANCE));
    assertThat(
        SerializationUtils.clone(UniverseIpSpace.INSTANCE), sameInstance(UniverseIpSpace.INSTANCE));
  }
}
