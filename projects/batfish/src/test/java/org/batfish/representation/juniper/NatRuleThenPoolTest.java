package org.batfish.representation.juniper;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.batfish.common.Warnings;
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

    // when using a pool with PAT disabled, only IPs need to be transformed
    NatRuleThenPool then = new NatRuleThenPool("POOL1");
    List<TransformationStep> steps = then.toTransformationSteps(snat, null, ip, null);

    assertThat(steps, contains(new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, ip, ip)));

    // when using a pool with PAT enable, both IPs and ports need to be transformed
    then = new NatRuleThenPool("POOL2");
    steps = then.toTransformationSteps(snat, null, ip, null);

    assertThat(
        steps,
        contains(
            new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, ip, ip),
            new AssignPortFromPool(SOURCE_NAT, PortField.SOURCE, port, port)));

    // when using a pool where PAT is not mentioned, PAT should be applied by default
    then = new NatRuleThenPool("POOL3");
    steps = then.toTransformationSteps(snat, null, ip, null);

    assertThat(
        steps,
        contains(
            new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, ip, ip),
            new AssignPortFromPool(
                SOURCE_NAT, PortField.SOURCE, Nat.DEFAULT_FROM_PORT, Nat.DEFAULT_TO_PORT)));

    // when default port range is changed, ports should be transform to the specified default range
    snat.setDefaultFromPort(10000);
    snat.setDefaultToPort(20000);
    steps = then.toTransformationSteps(snat, null, ip, null);

    assertThat(
        steps,
        contains(
            new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, ip, ip),
            new AssignPortFromPool(SOURCE_NAT, PortField.SOURCE, 10000, 20000)));
  }

  @Test
  public void testInvalidPool() {
    Ip from = Ip.parse("2.2.2.2");
    Ip to = Ip.parse("1.1.1.1");

    NatPool invalidPool = new NatPool();
    invalidPool.setFromAddress(from);
    invalidPool.setToAddress(to);
    invalidPool.setPortAddressTranslation(NoPortTranslation.INSTANCE);

    Nat snat = new Nat(Type.SOURCE);
    snat.getPools().put("POOL", invalidPool);

    Warnings warnings = new Warnings(false, true, false);
    assertTrue(
        new NatRuleThenPool("POOL").toTransformationSteps(snat, null, Ip.ZERO, warnings).isEmpty());
    assertThat(
        warnings, hasRedFlags(contains(hasText(containsString("NAT pool POOL is invalid")))));
  }
}
