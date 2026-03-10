package org.batfish.datamodel.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.RouteFilterList;
import org.junit.Test;

public class NamedStructureEquivalenceSetsTest {

  @Test
  public void testWriteObject() throws JsonProcessingException {
    NamedStructureEquivalenceSets.Builder<RouteFilterList> builder =
        NamedStructureEquivalenceSets.builder("rfl");
    RouteFilterList rfl = new RouteFilterList("name", ImmutableList.of());

    // the function is used when passed in
    assertThat(
        builder.writeObject(rfl, RouteFilterList::definitionJson), equalTo(rfl.definitionJson()));

    // default json is used otherwise
    assertThat(builder.writeObject(rfl, null), equalTo(BatfishObjectMapper.writePrettyString(rfl)));
  }
}
