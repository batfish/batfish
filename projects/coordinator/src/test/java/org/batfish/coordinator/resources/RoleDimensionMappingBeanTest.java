package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.batfish.role.RoleDimensionMapping;
import org.junit.Test;

public class RoleDimensionMappingBeanTest {

  @Test
  public void testProperties() {
    String regex = "(.*)";
    List<Integer> groups = ImmutableList.of(3, 4, 1);
    Map<String, String> canonicalRoleNames = ImmutableMap.of("baz", "bar", "bar", "foo");
    RoleDimensionMapping rdMap = new RoleDimensionMapping(regex, groups, canonicalRoleNames);
    RoleDimensionMappingBean bean = new RoleDimensionMappingBean(rdMap);

    assertThat(bean.regex, equalTo(regex));
    assertThat(bean.groups, equalTo(groups));
    assertThat(bean.canonicalRoleNames, equalTo(canonicalRoleNames));
  }

  @Test
  public void toRoleDimensionMap() {
    String regex = "(.*)";
    List<Integer> groups = ImmutableList.of(3, 4, 1);
    Map<String, String> canonicalRoleNames = ImmutableMap.of("baz", "bar", "bar", "foo");
    RoleDimensionMapping rdMap = new RoleDimensionMapping(regex, groups, canonicalRoleNames);
    RoleDimensionMappingBean bean = new RoleDimensionMappingBean(rdMap);

    assertThat(bean.toRoleDimensionMapping(), equalTo(rdMap));
  }
}
