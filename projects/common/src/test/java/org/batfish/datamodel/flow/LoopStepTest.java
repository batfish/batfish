package org.batfish.datamodel.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

class LoopStepTest {
  @Test
  void testJsonSerialization() {
    {
      LoopStep clone = BatfishObjectMapper.clone(LoopStep.INSTANCE, LoopStep.class);
      assertEquals(clone, LoopStep.INSTANCE);
    }

    {
      LoopStep clone = (LoopStep) BatfishObjectMapper.clone(LoopStep.INSTANCE, Step.class);
      assertEquals(clone, LoopStep.INSTANCE);
    }
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            LoopStep.INSTANCE, BatfishObjectMapper.clone(LoopStep.INSTANCE, LoopStep.class))
        .testEquals();
  }
}
