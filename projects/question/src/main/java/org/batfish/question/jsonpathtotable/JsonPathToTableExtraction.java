package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.internal.path.PathCompiler;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.answers.Schema;

/** Describes how to extract a column in {@link JsonPathToTableQuery}. */
public class JsonPathToTableExtraction extends JsonPathToTableColumn {

  public enum Method {
    PREFIX,
    FUNCOFSUFFIX,
    PREFIXOFSUFFIX,
    SUFFIXOFSUFFIX,
  }

  private static final String PROP_FILTER = "filter";
  private static final String PROP_INDEX = "index";
  private static final String PROP_METHOD = "method";

  private String _filter;

  private Integer _index;

  @Nonnull private Method _method;

  @JsonCreator
  public JsonPathToTableExtraction(
      @JsonProperty(PROP_SCHEMA) Schema schema,
      @JsonProperty(PROP_METHOD) Method method,
      @JsonProperty(PROP_FILTER) String filter,
      @JsonProperty(PROP_INDEX) Integer index,
      @JsonProperty(PROP_DESCRIPTION) String description,
      @JsonProperty(PROP_INCLUDE) Boolean include,
      @JsonProperty(PROP_IS_KEY) Boolean isKey,
      @JsonProperty(PROP_IS_VALUE) Boolean isValue) {
    super(schema, description, include, isKey, isValue);
    // sanity check what we got
    if (method == null) {
      throw new IllegalArgumentException("Method not specified in JsonPathToTable extraction");
    }
    switch (method) {
      case PREFIX:
        if (index == null) {
          throw new BatfishException("Index should be specified in prefix-based extraction hint");
        }
        if (filter != null) {
          throw new BatfishException("Filter should not specified in prefix-based extraction hint");
        }
        break;
      case FUNCOFSUFFIX:
        if (index != null) {
          throw new BatfishException(
              "Index should not be specified in funcofsuffix-based extraction hint");
        }
        if (filter == null) {
          throw new BatfishException(
              "Filter should be specified in funcofsuffix-based extraction hint");
        }
        if (!PathCompiler.compile(filter).isFunctionPath()) {
          throw new BatfishException(
              "Filter should be a path function in funcofsuffix-based extraction hint");
        }
        break;
      case PREFIXOFSUFFIX:
        if (index == null) {
          throw new BatfishException(
              "Index should be specified in prefixofsuffix-based extraction hint");
        }
        if (filter == null) {
          throw new BatfishException(
              "Filter should be specified in prefixofsuffix-based extraction hint");
        }
        break;
      case SUFFIXOFSUFFIX:
        if (index != null) {
          throw new BatfishException(
              "Index should not be specified in suffixofsuffix-based extraction hint");
        }
        if (filter == null) {
          throw new BatfishException(
              "Filter should be specified in suffixofsuffix-based extraction hint");
        }
        break;
      default:
        throw new BatfishException("Unknown method type " + method);
    }

    _filter = filter;
    _index = index;
    _method = method;
  }

  @JsonProperty(PROP_FILTER)
  public String getFilter() {
    return _filter;
  }

  @JsonProperty(PROP_INDEX)
  public Integer getIndex() {
    return _index;
  }

  @JsonProperty(PROP_METHOD)
  public Method getMethod() {
    return _method;
  }
}
