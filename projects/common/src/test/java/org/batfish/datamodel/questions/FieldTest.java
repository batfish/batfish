package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

final class FieldTest {

  @Test
  void testFieldOptionalDefaultFalse() throws IOException {
    Field field = BatfishObjectMapper.mapper().readValue("{}", Field.class);

    assertThat(field.getOptional(), equalTo(false));
  }

  @Test
  void testFieldOptionalFalse() throws IOException {
    Field field =
        BatfishObjectMapper.mapper()
            .readValue(String.format("{\"%s\":false}", BfConsts.PROP_OPTIONAL), Field.class);

    assertThat(field.getOptional(), equalTo(false));
  }

  @Test
  void testFieldOptionalTrue() throws IOException {
    Field field =
        BatfishObjectMapper.mapper()
            .readValue(String.format("{\"%s\":true}", BfConsts.PROP_OPTIONAL), Field.class);

    assertThat(field.getOptional(), equalTo(true));
  }

  @Test
  void testFieldType() throws IOException {
    Field field =
        BatfishObjectMapper.mapper()
            .readValue(String.format("{\"%s\":\"ipSpaceSpec\"}", BfConsts.PROP_TYPE), Field.class);

    assertThat(field.getType(), equalTo(Variable.Type.IP_SPACE_SPEC));
  }
}
