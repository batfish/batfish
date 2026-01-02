package org.batfish.datamodel.transformation;

import static org.junit.Assert.assertEquals;

import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ApplyAll} */
@ParametersAreNonnullByDefault
public final class ApplyAllTest {

  @Test
  public void testSerialization() {
    ApplyAll applyAll = new ApplyAll(Noop.NOOP_DEST_NAT);

    assertEquals(applyAll, SerializationUtils.clone(applyAll));
  }

  @Test
  public void testJacksonSerialization() {
    ApplyAll applyAll = new ApplyAll(Noop.NOOP_DEST_NAT);

    assertEquals(applyAll, BatfishObjectMapper.clone(applyAll, ApplyAll.class));
  }
}
