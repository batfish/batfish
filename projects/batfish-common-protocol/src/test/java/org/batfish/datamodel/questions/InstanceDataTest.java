package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.Variable.Type;
import org.junit.Test;

/** Tests of {@link InstanceData} */
public class InstanceDataTest {

  /**
   * Test that getOrderedVariableNames returns an empty list when orderedVariableNames are not
   * provided
   */
  @Test
  public void testGetOrderedVariableNamesMissing() throws IOException {
    InstanceData instanceData = BatfishObjectMapper.mapper().readValue("{}", InstanceData.class);
    assertThat(instanceData.getOrderedVariableNames(), empty());
  }

  /**
   * Test that getOrderedVariableNames returns an empty list when orderedVariableNames is an empty
   * list
   */
  @Test
  public void testGetOrderedVariableNamesEmptyList() throws IOException {
    InstanceData instanceData =
        BatfishObjectMapper.mapper().readValue("{\"orderedVariableNames\":[]}", InstanceData.class);
    assertThat(instanceData.getOrderedVariableNames(), empty());
  }

  /** Test that getOrderedVariableNames returns orderedVariableNames in the correct order */
  @Test
  public void testGetOrderedVariableNamesNonEmptyList() throws IOException {
    InstanceData instanceData =
        BatfishObjectMapper.mapper()
            .readValue("{\"orderedVariableNames\":[\"r\", \"a\", \"d\"]}", InstanceData.class);
    assertThat(instanceData.getOrderedVariableNames(), equalTo(ImmutableList.of("r", "a", "d")));
  }

  private static InstanceData clone(InstanceData instanceData) {
    return BatfishObjectMapper.clone(instanceData, InstanceData.class);
  }

  @Test
  public void testEquals() {
    InstanceData instanceData = new InstanceData();
    InstanceData initialInstance = clone(instanceData);
    EqualsTester equalsTester = new EqualsTester();
    equalsTester.addEqualityGroup(initialInstance, initialInstance).addEqualityGroup(new Object());
    instanceData.setInstanceName("instanceName");
    equalsTester.addEqualityGroup(clone(instanceData));
    instanceData.setDescription("The description");
    equalsTester.addEqualityGroup(clone(instanceData));
    instanceData.setLongDescription("The long description");
    equalsTester.addEqualityGroup(clone(instanceData));
    instanceData.setOrderedVariableNames(ImmutableList.of("b", "a"));
    equalsTester.addEqualityGroup(clone(instanceData));
    instanceData.setTags(new TreeSet<>(Arrays.asList("tag1", "tag2")));
    equalsTester.addEqualityGroup(clone(instanceData));
    Variable variable = new Variable();
    variable.setType(Type.INTEGER);
    instanceData.setVariables(ImmutableSortedMap.of("v", variable));
    equalsTester.addEqualityGroup(clone(instanceData));
    equalsTester.testEquals();
  }

  /**
   * Test that translating an InstanceData object to JSON and using that JSON to create a new
   * InstanceData object produces an object equal to the original instance
   */
  @Test
  public void testJsonSerialization() {
    InstanceData instanceData = new InstanceData();
    instanceData.setInstanceName("instanceName");
    instanceData.setDescription("The description");
    instanceData.setLongDescription("The long description");
    instanceData.setOrderedVariableNames(ImmutableList.of("b", "a"));
    instanceData.setTags(new TreeSet<>(Arrays.asList("tag1", "tag2")));
    instanceData.setVariables(ImmutableSortedMap.of("v", new Variable()));
    assertThat(clone(instanceData), equalTo(instanceData));
  }
}
