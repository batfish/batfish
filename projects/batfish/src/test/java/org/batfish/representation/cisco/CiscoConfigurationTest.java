package org.batfish.representation.cisco;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CiscoConfigurationTest {
  private CiscoConfiguration _config;
  private Interface _interface;
  private static final String ACL = "acl";
  private static final String POOL = "pool";
  private static final Ip IP = new Ip("1.2.3.4");

  // Initializes an empty CiscoConfiguration with a single Interface and minimal settings to not
  // crash.
  @Before
  public void before() {
    _config = new CiscoConfiguration();
    _config.setVendor(ConfigurationFormat.ARISTA);
    _config.setHostname("host");
    _config.setFilename("configs/host.cfg");
    _config.setAnswerElement(new ConvertConfigurationAnswerElement());
    _interface = new Interface("iface", _config);
  }

  @Test
  public void processSourceNatIsConverted() {
    CiscoSourceNat nat = new CiscoSourceNat();
    nat.setAclName(ACL);
    nat.setNatPool(POOL);
    NatPool pool = new NatPool();
    pool.setFirst(IP);
    pool.setLast(IP);
    _config.getNatPools().put(POOL, pool);

    SourceNat convertedNat =
        _config.processSourceNat(
            nat,
            _interface,
            Collections.singletonMap(
                ACL, IpAccessList.builder().setName(ACL).setLines(ImmutableList.of()).build()));

    assertThat(convertedNat, notNullValue());
    assertThat(convertedNat.getAcl().getName(), equalTo(ACL));
    assertThat(convertedNat.getPoolIpFirst(), equalTo(IP));
    assertThat(_config.getAnswerElement().getUndefinedReferences().size(), equalTo(0));
  }
}
