package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class EmptyIpSpaceTest {
  @Test
  public void testInterning() {
    assertThat(
        BatfishObjectMapper.clone(EmptyIpSpace.INSTANCE, IpSpace.class),
        sameInstance(EmptyIpSpace.INSTANCE));
    assertThat(
        SerializationUtils.clone(EmptyIpSpace.INSTANCE), sameInstance(EmptyIpSpace.INSTANCE));
  }
}
