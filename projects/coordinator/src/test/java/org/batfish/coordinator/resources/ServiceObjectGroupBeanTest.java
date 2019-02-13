package org.batfish.coordinator.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import org.batfish.referencelibrary.ServiceObjectGroup;
import org.junit.Test;

public class ServiceObjectGroupBeanTest {

  @Test
  public void conversionToAndFrom() {
    ServiceObjectGroup group = new ServiceObjectGroup("sog", ImmutableSortedSet.of("23"));
    ServiceObjectGroupBean bean = new ServiceObjectGroupBean(group);
    assertThat(new ServiceObjectGroupBean(bean.toServiceObjectGroup()), equalTo(bean));
  }
}
