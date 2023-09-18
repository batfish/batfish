package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.vendor.VendorStructureId;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
/** A line in an {@link IpAccessList} */
public abstract class AclLine implements Serializable {
  protected static final String PROP_NAME = "name";
  protected static final String PROP_TRACE_ELEMENT = "traceElement";
  protected static final String PROP_VENDOR_STRUCTURE_ID = "vendorStructureId";

  protected final @Nullable String _name;
  protected final @Nullable TraceElement _traceElement;
  protected final @Nullable VendorStructureId _vendorStructureId;

  AclLine(
      @Nullable String name,
      @Nullable TraceElement traceElement,
      @Nullable VendorStructureId vendorStructureId) {
    _name = name;
    _traceElement = traceElement;
    _vendorStructureId = vendorStructureId;
  }

  /** The name of this line in the list */
  @JsonProperty(PROP_NAME)
  public final String getName() {
    return _name;
  }

  @JsonProperty(PROP_TRACE_ELEMENT)
  public final TraceElement getTraceElement() {
    return _traceElement;
  }

  @JsonProperty(PROP_VENDOR_STRUCTURE_ID)
  public final Optional<VendorStructureId> getVendorStructureId() {
    return Optional.ofNullable(_vendorStructureId);
  }

  public abstract <R> R accept(GenericAclLineVisitor<R> visitor);
}
