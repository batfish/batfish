package org.batfish.coordinator.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.referencelibrary.FilterGroup;
import org.junit.Test;

public class FilterGroupBeanTest {

  @Test
  public void conversionToAndFrom() {
    FilterGroup group =
        new FilterGroup(
            ImmutableList.of(new FiltersSpecifier("abc"), new FiltersSpecifier("ipv4:def")), "fg1");
    FilterGroupBean bean = new FilterGroupBean(group);
    assertThat(new FilterGroupBean(bean.toFilterGroup()), equalTo(bean));
  }
}
