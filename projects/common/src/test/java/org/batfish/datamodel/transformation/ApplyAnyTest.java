package org.batfish.datamodel.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link ApplyAny} */
@ParametersAreNonnullByDefault
final class ApplyAnyTest {

  @Test
  void testSerialization() {
    ApplyAny applyAny = new ApplyAny(Noop.NOOP_DEST_NAT);

    assertEquals(applyAny, SerializationUtils.clone(applyAny));
  }

  @Test
  void testJacksonSerialization() {
    ApplyAny applyAll = new ApplyAny(Noop.NOOP_DEST_NAT);

    assertEquals(applyAll, BatfishObjectMapper.clone(applyAll, ApplyAny.class));
  }
}
