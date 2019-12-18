package org.batfish.datamodel.flow;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.junit.Test;

/** Test for {@link InboundStep}. */
public final class InboundStepTest {
  @Test
  public void testBuilderWithValidDetail() {
    String iface = "GigabitEthernet1/0";
    InboundStep step = InboundStep.builder().setDetail(new InboundStepDetail(iface)).build();
    assertEquals(step.getAction(), StepAction.ACCEPTED);
    assertEquals(step.getDetail().getInterface(), iface);
  }

  @Test(expected = IllegalStateException.class)
  public void testBuilderWithoutSetDetail() {
    InboundStep.builder().build();
  }

  @Test(expected = IllegalStateException.class)
  public void testBuilderSetDetailToNull() {
    InboundStep.builder().setDetail(null).build();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    String iface = "GigabitEthernet1/0";
    InboundStep step = InboundStep.builder().setDetail(new InboundStepDetail(iface)).build();
    InboundStep clone = BatfishObjectMapper.clone(step, InboundStep.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(step.getDetail().getInterface(), clone.getDetail().getInterface());
    assertEquals(clone.getDetail().getInterface(), iface);
  }
}
