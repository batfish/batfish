package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.vendor.VendorStructureId;

/**
 * An {@link AclLine} that matches packets that explicitly match a given ACL, and takes the same
 * action as that ACL would take. Functionally equivalent to in-lining the referenced ACL.
 */
@ParametersAreNonnullByDefault
public final class AclAclLine extends AclLine {
  private static final String PROP_ACL_NAME = "aclName";

  private final String _aclName;

  @JsonCreator
  private static AclAclLine jsonCreator(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_ACL_NAME) @Nullable String aclName,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement,
      @JsonProperty(PROP_VENDOR_STRUCTURE_ID) @Nullable VendorStructureId vendorStructureId) {
    checkNotNull(name, "%s must be provided", PROP_NAME);
    checkNotNull(aclName, "%s must be provided", PROP_ACL_NAME);
    return new AclAclLine(name, aclName, traceElement, vendorStructureId);
  }

  public AclAclLine(
      String name,
      String aclName,
      @Nullable TraceElement traceElement,
      @Nullable VendorStructureId vendorStructureId) {
    super(name, traceElement, vendorStructureId);
    _aclName = aclName;
  }

  public AclAclLine(String name, String aclName) {
    this(name, aclName, null, null);
  }

  @JsonProperty(PROP_ACL_NAME)
  public @Nonnull String getAclName() {
    return _aclName;
  }

  @Override
  public <R> R accept(GenericAclLineVisitor<R> visitor) {
    return visitor.visitAclAclLine(this);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AclAclLine)) {
      return false;
    }
    AclAclLine other = (AclAclLine) obj;
    return Objects.equals(_aclName, other._aclName)
        && Objects.equals(_name, other._name)
        && Objects.equals(_traceElement, other._traceElement)
        && Objects.equals(_vendorStructureId, other._vendorStructureId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aclName, _name, _traceElement, _vendorStructureId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_NAME, _name)
        .add(PROP_ACL_NAME, _aclName)
        .toString();
  }
}
