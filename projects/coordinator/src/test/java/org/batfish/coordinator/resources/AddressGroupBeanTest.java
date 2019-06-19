package org.batfish.coordinator.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import org.batfish.referencelibrary.AddressGroup;
import org.junit.Test;

public class AddressGroupBeanTest {

  @Test
  public void conversionToAndFromAddresses() {
    AddressGroup group = new AddressGroup(ImmutableSortedSet.of("1.1.1.1:255.255.255.1"), "ag1");
    AddressGroupBean bean = new AddressGroupBean(group);
    assertThat(new AddressGroupBean(bean.toAddressGroup()), equalTo(bean));
  }

  @Test
  public void conversionToAndFromAddressesAndGroups() {
    AddressGroup group =
        new AddressGroup(
            "ag1", ImmutableSortedSet.of("1.1.1.1:255.255.255.1"), ImmutableSortedSet.of("sub"));
    AddressGroupBean bean = new AddressGroupBean(group);
    assertThat(new AddressGroupBean(bean.toAddressGroup()), equalTo(bean));
  }
}
