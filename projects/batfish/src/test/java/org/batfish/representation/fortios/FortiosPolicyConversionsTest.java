package org.batfish.representation.fortios;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class FortiosPolicyConversionsTest {
  private static final BatfishUUID UUID = new BatfishUUID(4);

  @Test
  public void testAddressIpSpaceMetadata() {
    Address address = new Address("foo", UUID);
    assertThat(
        FortiosPolicyConversions.toIpSpaceMetadata(address, "configs/file.txt"),
        equalTo(
            new IpSpaceMetadata(
                "foo", "address", new VendorStructureId("configs/file.txt", "address", "foo"))));
    Address addressComment = new Address("bar", UUID);
    addressComment.setComment("My special servers");
    assertThat(
        FortiosPolicyConversions.toIpSpaceMetadata(addressComment, "configs/file.txt"),
        equalTo(
            new IpSpaceMetadata(
                "bar (My special servers)",
                "address",
                new VendorStructureId("configs/file.txt", "address", "bar"))));
  }

  @Test
  public void testAddressGroupIpSpaceMetadata() {
    Addrgrp group = new Addrgrp("foo", UUID);
    assertThat(
        FortiosPolicyConversions.toIpSpaceMetadata(group, "configs/file.txt"),
        equalTo(
            new IpSpaceMetadata(
                "foo", "addrgrp", new VendorStructureId("configs/file.txt", "addrgrp", "foo"))));
    Addrgrp groupComment = new Addrgrp("bar", UUID);
    groupComment.setComment("My special servers");
    assertThat(
        FortiosPolicyConversions.toIpSpaceMetadata(groupComment, "configs/file.txt"),
        equalTo(
            new IpSpaceMetadata(
                "bar (My special servers)",
                "addrgrp",
                new VendorStructureId("configs/file.txt", "addrgrp", "bar"))));
  }
}
