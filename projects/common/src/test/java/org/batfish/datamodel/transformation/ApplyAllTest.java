package org.batfish.datamodel.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link ApplyAll} */
@ParametersAreNonnullByDefault
final class ApplyAllTest {

  @Test
  void testSerialization() {
    ApplyAll applyAll = new ApplyAll(Noop.NOOP_DEST_NAT);

    assertEquals(applyAll, SerializationUtils.clone(applyAll));
  }

  @Test
  void testJacksonSerialization() {
    ApplyAll applyAll = new ApplyAll(Noop.NOOP_DEST_NAT);

    assertEquals(applyAll, BatfishObjectMapper.clone(applyAll, ApplyAll.class));
  }
}
