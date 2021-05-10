package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConversions.toRouteFilterList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import junit.framework.TestCase;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class CiscoXrConversionsTest extends TestCase {

  /** Check that vendorStructureId is set when ACL is converted to route filter list */
  @Test
  public void testToRouterFilterList_standardAccessList_vendorStructureId() {
    Ipv4AccessList acl = new Ipv4AccessList("name");
    RouteFilterList rfl = toRouteFilterList(acl, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", "name", CiscoXrStructureType.IPV4_ACCESS_LIST.getDescription())));
  }

  /** Check that vendorStructureId is set when prefix list is converted to route filter list */
  @Test
  public void testToRouterFilterList_prefixList_vendorStructureId() {
    PrefixList plist = new PrefixList("name");
    RouteFilterList rfl = toRouteFilterList(plist, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", "name", CiscoXrStructureType.PREFIX_LIST.getDescription())));
  }
}
