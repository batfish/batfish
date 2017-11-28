package org.batfish.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.Writer;

public class BatfishObjectMapper extends ObjectMapper {

  private static class Factory extends JsonFactory {
    /** */
    private static final long serialVersionUID = 1L;

    @Override
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
      return super._createGenerator(out, ctxt).setPrettyPrinter(new PrettyPrinter());
    }
  }

  private static class PrettyPrinter extends DefaultPrettyPrinter {

    /** */
    private static final long serialVersionUID = 1L;

    public PrettyPrinter() {
      _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  public BatfishObjectMapper() {
    this(true);
  }

  public BatfishObjectMapper(boolean indent) {
    super(indent ? new Factory() : new JsonFactory());
    if (indent) {
      enable(SerializationFeature.INDENT_OUTPUT);
    }
    enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    // Next two lines make Java.time.Instant class serialize as an RFC-3339 timestamp
    registerModule(new JavaTimeModule());
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // See https://groups.google.com/forum/#!topic/jackson-user/WfZzlt5C2Ww
    //  This fixes issues in which non-empty maps with keys with empty values would get omitted
    //  entirely. See also https://github.com/batfish/batfish/issues/256
    setDefaultPropertyInclusion(JsonInclude.Value.construct(Include.NON_EMPTY, Include.ALWAYS));
  }

  public BatfishObjectMapper(ClassLoader cl) {
    this();
    TypeFactory tf = TypeFactory.defaultInstance().withClassLoader(cl);
    setTypeFactory(tf);
  }
}
