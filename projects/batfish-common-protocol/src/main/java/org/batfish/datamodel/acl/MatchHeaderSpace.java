package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;

public class MatchHeaderSpace extends AclLineMatchExpr {
  private static final String PROP_HEADER_SPACE = "headerSpace";
  private static final long serialVersionUID = 1L;

  private final HeaderSpace _headerSpace;

  public MatchHeaderSpace(HeaderSpace headerSpace) {
    this(headerSpace, null);
  }

  @JsonCreator
  public MatchHeaderSpace(
      @JsonProperty(PROP_HEADER_SPACE) HeaderSpace headerSpace,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    super(description);
    _headerSpace = headerSpace;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchHeaderSpace(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return _headerSpace.compareTo(((MatchHeaderSpace) o)._headerSpace);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_headerSpace, ((MatchHeaderSpace) o)._headerSpace);
  }

  @JsonProperty(PROP_HEADER_SPACE)
  public HeaderSpace getHeaderspace() {
    return _headerSpace;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_headerSpace);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_HEADER_SPACE, _headerSpace).toString();
  }
}
