package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.batfish.representation.juniper.NatRuleThenInterface.INSTANCE;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;
import org.junit.Test;

/** Tests for {@link NatRuleThenInterface}. */
public final class NatRuleThenInterfaceTest {
  @Test
  public void testToTransformationStep() {
    Ip ip = Ip.parse("1.1.1.1");
    List<TransformationStep> step =
        INSTANCE.toTransformationStep(SOURCE_NAT, SOURCE, PortField.SOURCE, ImmutableMap.of(), ip);
    assertThat(step, contains(new AssignIpAddressFromPool(SOURCE_NAT, SOURCE, ip, ip)));
  }
}
