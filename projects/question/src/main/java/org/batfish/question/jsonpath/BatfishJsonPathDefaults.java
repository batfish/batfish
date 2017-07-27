package org.batfish.question.jsonpath;

import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import java.util.EnumSet;
import java.util.Set;

public class BatfishJsonPathDefaults implements Defaults {

  public static final BatfishJsonPathDefaults INSTANCE = new BatfishJsonPathDefaults();

  private BatfishJsonPathDefaults() {}

  @Override
  public JsonProvider jsonProvider() {
    return new JacksonJsonNodeJsonProvider();
  }

  @Override
  public MappingProvider mappingProvider() {
    return new JacksonMappingProvider();
  }

  @Override
  public Set<Option> options() {
    return EnumSet.noneOf(Option.class);
  }
}
