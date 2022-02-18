package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link FwFromSourcePrefixListExcept} */
public class FwFromSourcePrefixListExceptTest {
  private JuniperConfiguration _jc;
  private Warnings _w;
  private Configuration _c;

  private static final String BASE_PREFIX_LIST_NAME = "prefixList";
  private static final Prefix BASE_IP_PREFIX = Prefix.parse("1.2.3.4/32");

  @Before
  public void setup() {
    _jc = new JuniperConfiguration();
    PrefixList pl = new PrefixList(BASE_PREFIX_LIST_NAME);
    pl.getPrefixes().add(BASE_IP_PREFIX);
    _jc.getMasterLogicalSystem().getPrefixLists().put(pl.getName(), pl);
    _w = new Warnings();
    _c = new Configuration("test", ConfigurationFormat.FLAT_JUNIPER);
  }

  @Test
  public void testToHeaderSpace() {
    FwFromSourcePrefixListExcept fwFrom = new FwFromSourcePrefixListExcept(BASE_PREFIX_LIST_NAME);

    // Apply base IP prefix to headerSpace with null IpSpace
    assertEquals(
        fwFrom.toHeaderSpace(_jc, _w),
        HeaderSpace.builder().setNotSrcIps(BASE_IP_PREFIX.toIpSpace()).build());
  }

  @Test
  public void testToHeaderSpace_notExist() {
    FwFromSourcePrefixListExcept fwFrom = new FwFromSourcePrefixListExcept("noName");

    assertEquals(
        fwFrom.toHeaderSpace(_jc, _w),
        HeaderSpace.builder().setNotSrcIps(EmptyIpSpace.INSTANCE).build());
  }

  @Test
  public void testToAclLineMatchExpr() {
    FwFromSourcePrefixListExcept fwFrom = new FwFromSourcePrefixListExcept(BASE_PREFIX_LIST_NAME);

    assertEquals(
        fwFrom.toAclLineMatchExpr(_jc, _c, _w),
        new MatchHeaderSpace(
            fwFrom.toHeaderSpace(_jc, _w),
            TraceElement.of("Matched source-prefix-list prefixList except")));
  }
}
