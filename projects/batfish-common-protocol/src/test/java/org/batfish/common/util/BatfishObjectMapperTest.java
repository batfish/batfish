package org.batfish.common.util;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatfishObjectMapperTest {
  private static class Foo {
    @JsonProperty("map")
    public Map<String, String[]> _map = new HashMap<>();
  }

  @Test
  public void testMapFieldWithEmptyValue() throws JsonProcessingException {
    Foo foo = new Foo();
    foo._map.put("key", new String[0]);

    ObjectMapper mapper =
        new ObjectMapper()
            .setDefaultPropertyInclusion(
                JsonInclude.Value.construct(Include.NON_EMPTY, Include.ALWAYS));
    assertThat(mapper.writeValueAsString(foo), allOf(containsString("map"), containsString("key")));
  }
}
