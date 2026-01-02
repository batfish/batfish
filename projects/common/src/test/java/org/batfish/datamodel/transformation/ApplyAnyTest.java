package org.batfish.datamodel.transformation;

import static org.junit.Assert.assertEquals;

import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ApplyAny} */
@ParametersAreNonnullByDefault
public final class ApplyAnyTest {

  @Test
  public void testSerialization() {
    ApplyAny applyAny = new ApplyAny(Noop.NOOP_DEST_NAT);

    assertEquals(applyAny, SerializationUtils.clone(applyAny));
  }

  @Test
  public void testJacksonSerialization() {
    ApplyAny applyAll = new ApplyAny(Noop.NOOP_DEST_NAT);

    assertEquals(applyAll, BatfishObjectMapper.clone(applyAll, ApplyAny.class));
  }
}
