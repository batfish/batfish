package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.TraceElement;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class TraceElementsTest {

  @Test
  public void testPermittedByNamedIpSpace() {
    Ip ip = Ip.parse("1.2.3.4");
    // No metadata, just a string
    assertThat(
        TraceElements.permittedByNamedIpSpace(ip, "source address", null, "displayName"),
        equalTo(TraceElement.of("source address 1.2.3.4 permitted by 'displayName'")));
    // Metadata but no VendorStructureId: use metadata name over "real" name
    IpSpaceMetadata metadataOnly = new IpSpaceMetadata("name", "object-group", null);
    assertThat(
        TraceElements.permittedByNamedIpSpace(ip, "source address", metadataOnly, "name~vsys1"),
        equalTo(TraceElement.of("source address 1.2.3.4 permitted by object-group named 'name'")));
    // Metadata and VSID: add link and use proper fields
    VendorStructureId vsID = new VendorStructureId("configs/pan.txt", "object-group", "name~vsys1");
    IpSpaceMetadata metadataWithVsID = new IpSpaceMetadata("name", "object-group", vsID);
    assertThat(
        TraceElements.permittedByNamedIpSpace(ip, "source address", metadataWithVsID, "name~vsys1"),
        equalTo(
            TraceElement.builder()
                .add("source address 1.2.3.4 permitted by object-group ")
                .add("name", vsID)
                .build()));
  }
}
