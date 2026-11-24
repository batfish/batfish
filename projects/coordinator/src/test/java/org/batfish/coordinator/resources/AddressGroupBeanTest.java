package org.batfish.coordinator.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSortedSet;
import org.batfish.referencelibrary.AddressGroup;
import org.junit.jupiter.api.Test;

class AddressGroupBeanTest {

  @Test
  void conversionToAndFromAddresses() {
    AddressGroup group = new AddressGroup(ImmutableSortedSet.of("1.1.1.1:255.255.255.1"), "ag1");
    AddressGroupBean bean = new AddressGroupBean(group);
    assertThat(new AddressGroupBean(bean.toAddressGroup()), equalTo(bean));
  }

  @Test
  void conversionToAndFromAddressesAndGroups() {
    AddressGroup group =
        new AddressGroup(
            "ag1", ImmutableSortedSet.of("1.1.1.1:255.255.255.1"), ImmutableSortedSet.of("sub"));
    AddressGroupBean bean = new AddressGroupBean(group);
    assertThat(new AddressGroupBean(bean.toAddressGroup()), equalTo(bean));
  }
}
