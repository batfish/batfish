package org.batfish.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
            new IspModel(1L, ImmutableList.of(), "name", ImmutableSet.of()),
            new IspModel(1L, ImmutableList.of(), "name", ImmutableSet.of()))
        .addEqualityGroup(new IspModel(2L, ImmutableList.of(), "name", ImmutableSet.of()))
        .addEqualityGroup(
            new IspModel(
                1L,
                ImmutableList.of(
                    new Remote(
                        "a",
                        "b",
                        ConcreteInterfaceAddress.parse("1.1.1.1/32"),
                        BgpActivePeerConfig.builder().build())),
                "name",
                ImmutableSet.of()))
        .addEqualityGroup(new IspModel(1L, ImmutableList.of(), "other", ImmutableSet.of()))
        .addEqualityGroup(
            new IspModel(
                1L, ImmutableList.of(), "name", ImmutableSet.of(Prefix.parse("1.1.1.1/32"))))
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
