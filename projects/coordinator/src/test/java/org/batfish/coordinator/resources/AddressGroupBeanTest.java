package org.batfish.coordinator.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import org.batfish.referencelibrary.AddressGroup;
import org.junit.Test;

public class AddressGroupBeanTest {

  @Test
  public void conversionToAndFrom() {
    AddressGroup group = new AddressGroup(ImmutableSortedSet.of("1.1.1.1:255.255.255.1"), "ag1");
    AddressGroupBean bean = new AddressGroupBean(group);
    assertThat(new AddressGroupBean(bean.toAddressGroup()), equalTo(bean));
  }
}
