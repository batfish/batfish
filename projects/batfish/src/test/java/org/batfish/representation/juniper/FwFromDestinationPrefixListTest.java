package org.batfish.representation.juniper;

import static org.batfish.datamodel.matchers.HeaderSpaceMatchers.hasDstIps;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.Warnings;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Prefix;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link FwFromDestinationPrefixList} */
public class FwFromDestinationPrefixListTest {
  private JuniperConfiguration _jc;
  private Warnings _w;

  private static final String BASE_PREFIX_LIST_NAME = "prefixList";
  private static final Prefix BASE_IP_PREFIX = Prefix.parse("1.2.3.4/32");

  @Before
  public void setup() {
    _jc = new JuniperConfiguration();
    PrefixList pl = new PrefixList(BASE_PREFIX_LIST_NAME);
    pl.getPrefixes().add(BASE_IP_PREFIX);
    _jc.getMasterLogicalSystem().getPrefixLists().put(pl.getName(), pl);
    _w = new Warnings();
  }

  @Test
  public void testToHeaderSpace() {
    FwFromDestinationPrefixList fwFrom = new FwFromDestinationPrefixList(BASE_PREFIX_LIST_NAME);

    HeaderSpace headerSpace = fwFrom.toHeaderSpace(_jc, _w);

    assertThat(headerSpace, hasDstIps(equalTo(BASE_IP_PREFIX.toIpSpace())));
  }
}
