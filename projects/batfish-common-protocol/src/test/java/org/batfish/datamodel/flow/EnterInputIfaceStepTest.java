package org.batfish.datamodel.flow;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

/** Test for {@link EnterInputIfaceStep}. */
public class EnterInputIfaceStepTest {
  @Test
  public void testJsonSerialization() throws IOException {
    EnterInputIfaceStep step =
        EnterInputIfaceStep.builder()
            .setAction(StepAction.RECEIVED)
            .setDetail(
                EnterInputIfaceStepDetail.builder()
                    .setInputInterface(NodeInterfacePair.of("n1", "i1"))
                    .setInputVrf("vrf")
                    .setInputInterfaceStructureId(new VendorStructureId("f", "type", "i1"))
                    .build())
            .build();
    EnterInputIfaceStep clone = (EnterInputIfaceStep) BatfishObjectMapper.clone(step, Step.class);
    assertEquals(step.getAction(), clone.getAction());
    assertEquals(step.getDetail().getInputInterface(), clone.getDetail().getInputInterface());
    assertEquals(step.getDetail().getInputVrf(), clone.getDetail().getInputVrf());
    assertEquals(
        step.getDetail().getInputInterfaceStructureId(),
        clone.getDetail().getInputInterfaceStructureId());
  }
}
