package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.batfish.representation.juniper.NatRuleThenInterface.INSTANCE;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.juniper.Nat.Type;
import org.junit.Test;

/** Tests for {@link NatRuleThenInterface}. */
public final class NatRuleThenInterfaceTest {
  @Test
  public void testToTransformationStep() {
    Ip ip = Ip.parse("1.1.1.1");
    Nat snat = new Nat(Type.SOURCE);
    List<TransformationStep> step = INSTANCE.toTransformationSteps(snat, null, ip, null);
    assertThat(
        step,
        contains(
            new AssignIpAddressFromPool(SOURCE_NAT, SOURCE, ip, ip),
            new AssignPortFromPool(
                SOURCE_NAT, PortField.SOURCE, Nat.DEFAULT_FROM_PORT, Nat.DEFAULT_TO_PORT)));

    snat.setDefaultFromPort(10000);
    snat.setDefaultToPort(20000);
    step = INSTANCE.toTransformationSteps(snat, null, ip, null);
    assertThat(
        step,
        contains(
            new AssignIpAddressFromPool(SOURCE_NAT, SOURCE, ip, ip),
            new AssignPortFromPool(SOURCE_NAT, PortField.SOURCE, 10000, 20000)));
  }
}
