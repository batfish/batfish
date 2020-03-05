package org.batfish.representation.aws;

import static org.batfish.representation.aws.Utils.createPublicIpsRefBook;
import static org.batfish.representation.aws.Utils.publicIpAddressGroupName;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collections;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.referencelibrary.ReferenceBook;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UtilsTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCreatePublicIpsRefBook() {
    NetworkInterface networkInterface =
        new NetworkInterface(
            "interface",
            "subnet",
            "vpc",
            ImmutableList.of(),
            ImmutableList.of(
                new PrivateIpAddress(true, Ip.parse("10.10.10.10"), Ip.parse("5.5.5.5"))),
            "desc2",
            null);

    NetworkInterface networkInterface2 =
        new NetworkInterface(
            "interface2",
            "subnet",
            "vpc",
            ImmutableList.of(),
            ImmutableList.of(
                new PrivateIpAddress(true, Ip.parse("10.10.10.10"), Ip.parse("3.3.3.3")),
                new PrivateIpAddress(true, Ip.parse("10.10.10.10"), Ip.parse("4.4.4.4"))),
            "desc",
            null);

    NetworkInterface networkInterface0 =
        new NetworkInterface(
            "interface0",
            "subnet",
            "vpc",
            ImmutableList.of(),
            ImmutableList.of(new PrivateIpAddress(true, Ip.parse("10.10.10.10"), null)),
            "desc",
            null);

    Configuration cfgNode = new Configuration("cfg", ConfigurationFormat.AWS);
    String bookName = GeneratedRefBookUtils.getName(cfgNode.getHostname(), BookType.PublicIps);

    // book is not created if we don't have public IPs in the interface list
    createPublicIpsRefBook(Collections.singleton(networkInterface0), cfgNode);
    assertTrue(cfgNode.getGeneratedReferenceBooks().isEmpty());

    // the correct ref book is created from the three interfaces
    createPublicIpsRefBook(
        ImmutableList.of(networkInterface, networkInterface0, networkInterface2), cfgNode);
    assertThat(
        cfgNode.getGeneratedReferenceBooks().get(bookName),
        equalTo(
            ReferenceBook.builder(bookName)
                .setAddressGroups(
                    ImmutableList.of(
                        new AddressGroup(
                            ImmutableSortedSet.of("5.5.5.5"),
                            publicIpAddressGroupName(networkInterface)),
                        new AddressGroup(
                            ImmutableSortedSet.of("3.3.3.3", "4.4.4.4"),
                            publicIpAddressGroupName(networkInterface2))))
                .build()));

    // running it again should barf since we already have the reference book
    _thrown.expect(IllegalArgumentException.class);
    createPublicIpsRefBook(ImmutableList.of(networkInterface), cfgNode);
  }
}
