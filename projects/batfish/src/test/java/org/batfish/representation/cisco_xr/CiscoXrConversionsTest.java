package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConversions.toRouteFilterList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import junit.framework.TestCase;
import org.batfish.datamodel.RouteFilterList;
import org.junit.Test;

public class CiscoXrConversionsTest extends TestCase {

  /** Check that source name and type is set when ACL is converted to route filter list */
  @Test
  public void testToRouterFilterList_standardAccessList_source() {
    Ipv4AccessList acl = new Ipv4AccessList("name");
    RouteFilterList rfl = toRouteFilterList(acl);
    assertThat(rfl.getSourceName(), equalTo("name"));
    assertThat(
        rfl.getSourceType(), equalTo(CiscoXrStructureType.IPV4_ACCESS_LIST.getDescription()));
  }

  /** Check that source name and type is set when prefix list is converted to route filter list */
  @Test
  public void testToRouterFilterList_prefixList_source() {
    PrefixList plist = new PrefixList("name");
    RouteFilterList rfl = toRouteFilterList(plist);
    assertThat(rfl.getSourceName(), equalTo("name"));
    assertThat(rfl.getSourceType(), equalTo(CiscoXrStructureType.PREFIX_LIST.getDescription()));
  }
}
