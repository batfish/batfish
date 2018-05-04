package org.batfish.representation.juniper;

import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.hasLines;
import static org.batfish.datamodel.matchers.AclIpSpaceMatchers.isAclIpSpaceThat;
import static org.batfish.datamodel.matchers.HeaderSpaceMatchers.hasSrcOrDstIps;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.junit.Before;
import org.junit.Test;

public class FwFromPrefixListTest {
  private JuniperConfiguration _jc;
  private Warnings _w;
  private Configuration _c;

  private static final String BASE_PREFIX_LIST_NAME = "prefixList";
  private static final String BASE_IP_PREFIX = "1.2.3.4/32";

  @Before
  public void setup() {
    _jc = new JuniperConfiguration(ImmutableSet.of());
    _jc.getPrefixLists().put(BASE_PREFIX_LIST_NAME, new PrefixList(BASE_PREFIX_LIST_NAME, -1));
    _w = new Warnings();
    _c = new Configuration("test", ConfigurationFormat.FLAT_JUNIPER);
    RouteFilterList rflist = new RouteFilterList(BASE_PREFIX_LIST_NAME);
    RouteFilterLine rfline =
        new RouteFilterLine(LineAction.ACCEPT, Prefix.parse(BASE_IP_PREFIX), new SubRange(0, 0));
    rflist.addLine(rfline);
    _c.getRouteFilterLists().put(BASE_PREFIX_LIST_NAME, rflist);
  }

  @Test
  public void testApplyTo() {
    IpSpace additionalIpSpace = new Ip("2.2.2.2").toIpSpace();
    IpSpace baseIpSpace = new IpWildcard(BASE_IP_PREFIX).toIpSpace();

    HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();
    HeaderSpace.Builder headerSpaceBuilderWithIpSpaceFilter =
        HeaderSpace.builder().setSrcOrDstIps(additionalIpSpace);
    FwFromPrefixList fwFrom = new FwFromPrefixList(BASE_PREFIX_LIST_NAME);

    // Apply base IP prefix to headerSpace with null IpSpace
    fwFrom.applyTo(headerSpaceBuilder, _jc, _w, _c);

    // Apply base IP prefix to headerSpace with non-null IpSpace
    fwFrom.applyTo(headerSpaceBuilderWithIpSpaceFilter, _jc, _w, _c);

    // Confirm combining base with null IpSpace results in just base IpSpace
    assertThat(headerSpaceBuilder.build(), hasSrcOrDstIps(equalTo(baseIpSpace)));

    // Confirm combining base with additional IpSpace results in an IpSpace combining both
    assertThat(
        headerSpaceBuilderWithIpSpaceFilter.build(),
        hasSrcOrDstIps(
            isAclIpSpaceThat(
                hasLines(
                    containsInAnyOrder(
                        AclIpSpaceLine.permit(additionalIpSpace),
                        AclIpSpaceLine.permit(baseIpSpace))))));
  }
}
