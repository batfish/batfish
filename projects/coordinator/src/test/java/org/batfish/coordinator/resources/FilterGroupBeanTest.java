package org.batfish.coordinator.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.referencelibrary.FilterGroup;
import org.junit.Test;

public class FilterGroupBeanTest {

  @Test
  public void conversionToAndFrom() {
    FilterGroup group = new FilterGroup(ImmutableList.of("abc", "def"), "fg1");
    FilterGroupBean bean = new FilterGroupBean(group);
    assertThat(new FilterGroupBean(bean.toFilterGroup()), equalTo(bean));
  }
}
