package org.batfish.datamodel.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.junit.jupiter.api.Test;

/** Test for {@link InboundStep}. */
final class InboundStepTest {
  @Test
  void testBuilderWithValidDetail() {
    String iface = "GigabitEthernet1/0";
    InboundStep step = InboundStep.builder().setDetail(new InboundStepDetail(iface)).build();
    assertEquals(step.getAction(), StepAction.ACCEPTED);
    assertEquals(step.getDetail().getInterface(), iface);
  }

  @Test
  void testBuilderWithoutSetDetail() {
    assertThrows(IllegalStateException.class, () -> InboundStep.builder().build());
  }

  @Test
  void testBuilderSetDetailToNull() {
    assertThrows(IllegalStateException.class, () -> InboundStep.builder().setDetail(null).build());
  }

  @Test
  void testJsonSerialization() {
    String iface = "GigabitEthernet1/0";
    InboundStep step = InboundStep.builder().setDetail(new InboundStepDetail(iface)).build();
    InboundStep clone = BatfishObjectMapper.clone(step, InboundStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(step.getDetail().getInterface(), clone.getDetail().getInterface());
    assertEquals(clone.getDetail().getInterface(), iface);
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            InboundStep.builder().setDetail(new InboundStepDetail("i")).build(),
            InboundStep.builder().setDetail(new InboundStepDetail("i")).build())
        .addEqualityGroup(InboundStep.builder().setDetail(new InboundStepDetail("j")).build())
        .testEquals();
  }
}
