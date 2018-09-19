package org.batfish.representation.cisco;

import static org.batfish.representation.cisco.CiscoConfiguration.computeDynamicNatAclName;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.batfish.datamodel.transformation.Transformation.Direction;
import org.batfish.datamodel.transformation.Transformation.RuleAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CiscoConfigurationTest {
  private static final String ACL = "acl";
  private static final String POOL = "pool";
  private static final Ip IP = new Ip("1.2.3.4");
  private CiscoConfiguration _config;

  // Initializes an empty CiscoConfiguration with a single Interface and minimal settings to not
  // crash.
  @Before
  public void before() {
    _config = new CiscoConfiguration();
    _config.setVendor(ConfigurationFormat.ARISTA);
    _config.setHostname("host");
    _config.setFilename("configs/host.cfg");
    _config.setAnswerElement(new ConvertConfigurationAnswerElement());
    _config.getStandardAcls().put(ACL, new StandardAccessList(ACL));
  }

  @Test
  public void processSourceNatIsConverted() {
    CiscoDynamicNat nat = new CiscoDynamicNat();
    nat.setAclName(ACL);
    nat.setAction(RuleAction.SOURCE_INSIDE);
    nat.setNatPool(POOL);
    NatPool pool = new NatPool();
    pool.setFirst(IP);
    pool.setLast(IP);
    _config.getNatPools().put(POOL, pool);
    Set<String> insideInterfaces = new TreeSet<>();
    insideInterfaces.add("Ethernet0");

    DynamicNatRule convertedNat =
        (DynamicNatRule)
            _config.processNat(
                null,
                nat,
                Collections.singletonMap(
                    ACL, IpAccessList.builder().setName(ACL).setLines(ImmutableList.of()).build()),
                insideInterfaces,
                Direction.EGRESS);

    assertThat(convertedNat, notNullValue());
    assertThat(convertedNat.getAcl(), notNullValue());
    assertThat(convertedNat.getAcl().getName(), equalTo(computeDynamicNatAclName(ACL)));
    assertThat(convertedNat.getPoolIpFirst(), equalTo(IP));
  }
}
