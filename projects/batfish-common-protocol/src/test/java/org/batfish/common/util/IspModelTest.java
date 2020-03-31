package org.batfish.common.util;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.IspModel.Remote;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class IspModelTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            IspModel.builder().setAsn(1L).setName("name").build(),
            IspModel.builder().setAsn(1L).setName("name").build())
        .addEqualityGroup(IspModel.builder().setAsn(2L).setName("name").build())
        .addEqualityGroup(
            IspModel.builder()
                .setAsn(1L)
                .setName("name")
                .setRemotes(
                    new Remote(
                        "a",
                        "b",
                        ConcreteInterfaceAddress.parse("1.1.1.1/32"),
                        BgpActivePeerConfig.builder().build()))
                .build())
        .addEqualityGroup(IspModel.builder().setAsn(1L).setName("other").build())
        .addEqualityGroup(
            IspModel.builder()
                .setAsn(1L)
                .setName("name")
                .setAdditionalPrefixesToInternet(Prefix.parse("1.1.1.1/32"))
                .build())
        .testEquals();
  }

  @Test
  public void testEqualsRemote() {
    new EqualsTester()
        .addEqualityGroup(
            new Remote(
                "a",
                "b",
                ConcreteInterfaceAddress.parse("1.1.1.1/32"),
                BgpActivePeerConfig.builder().build()),
            new Remote(
                "a",
                "b",
                ConcreteInterfaceAddress.parse("1.1.1.1/32"),
                BgpActivePeerConfig.builder().build()))
        .addEqualityGroup(
            new Remote(
                "other",
                "b",
                ConcreteInterfaceAddress.parse("1.1.1.1/32"),
                BgpActivePeerConfig.builder().build()))
        .addEqualityGroup(
            new Remote(
                "a",
                "other",
                ConcreteInterfaceAddress.parse("1.1.1.1/32"),
                BgpActivePeerConfig.builder().build()))
        .addEqualityGroup(
            new Remote(
                "a",
                "b",
                ConcreteInterfaceAddress.parse("2.2.2.2/32"),
                BgpActivePeerConfig.builder().build()))
        .addEqualityGroup(
            new Remote(
                "other",
                "b",
                ConcreteInterfaceAddress.parse("1.1.1.1/32"),
                BgpActivePeerConfig.builder().setDescription("other").build()))
        .testEquals();
  }
}
