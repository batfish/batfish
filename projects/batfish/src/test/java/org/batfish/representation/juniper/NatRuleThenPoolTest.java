package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.juniper.Nat.Type;
import org.junit.Test;

public class NatRuleThenPoolTest {
  @Test
  public void testToTransformationStep() {
    Ip ip = Ip.parse("1.1.1.1");
    int port = 10000;

    NatPool poolWithoutPAT = new NatPool();
    poolWithoutPAT.setFromAddress(ip);
    poolWithoutPAT.setToAddress(ip);
    poolWithoutPAT.setPortAddressTranslation(NoPortTranslation.INSTANCE);

    NatPool poolWithPAT = new NatPool();
    poolWithPAT.setFromAddress(ip);
    poolWithPAT.setToAddress(ip);
    poolWithPAT.setPortAddressTranslation(new PatPool(port, port));

    NatPool poolWithDefaultPAT = new NatPool();
    poolWithDefaultPAT.setFromAddress(ip);
    poolWithDefaultPAT.setToAddress(ip);

    Nat snat = new Nat(Type.SOURCE);
    snat.getPools().put("POOL1", poolWithoutPAT);
    snat.getPools().put("POOL2", poolWithPAT);
    snat.getPools().put("POOL3", poolWithDefaultPAT);

    NatRuleThenPool then = new NatRuleThenPool("POOL1");
    List<TransformationStep> steps = then.toTransformationStep(snat, ip);

    assertThat(steps, contains(new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, ip, ip)));

    then = new NatRuleThenPool("POOL2");
    steps = then.toTransformationStep(snat, ip);

    assertThat(
        steps,
        contains(
            new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, ip, ip),
            new AssignPortFromPool(SOURCE_NAT, PortField.SOURCE, port, port)));

    then = new NatRuleThenPool("POOL3");
    steps = then.toTransformationStep(snat, ip);

    assertThat(
        steps,
        contains(
            new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, ip, ip),
            new AssignPortFromPool(SOURCE_NAT, PortField.SOURCE, 1024, 63487)));

    snat.setDefaultFromPort(10000);
    snat.setDefaultToPort(20000);
    steps = then.toTransformationStep(snat, ip);

    assertThat(
        steps,
        contains(
            new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, ip, ip),
            new AssignPortFromPool(SOURCE_NAT, PortField.SOURCE, 10000, 20000)));
  }
}
