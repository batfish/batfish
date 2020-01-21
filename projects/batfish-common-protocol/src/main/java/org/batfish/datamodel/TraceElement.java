package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.vendor.VendorStructureId;

/** Metadata used to create human-readable traces. */
@ParametersAreNonnullByDefault
public final class TraceElement implements Serializable {
  private static final String PROP_FRAGMENTS = "fragments";
  private static final String PROP_TEXT = "text";
  private static final String PROP_VENDOR_STRUCTURE_ID = "vendorStructureId";

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
  public interface Fragment extends Serializable {
    String getText();
  }

  /** A plain text {@link Fragment}. */
  public static final class TextFragment implements Fragment {
    private final String _text;

    TextFragment(String text) {
      checkArgument(!text.isEmpty(), "%s cannot be empty", PROP_TEXT);
      _text = text;
    }

    @JsonCreator
    private static TextFragment jsonCreator(@Nullable @JsonProperty(PROP_TEXT) String text) {
      checkNotNull(text, "%s cannot be null", PROP_TEXT);
      return new TextFragment(text);
    }

    @Override
    @JsonProperty(PROP_TEXT)
    public String getText() {
      return _text;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TextFragment)) {
        return false;
      }
      TextFragment other = (TextFragment) o;
      return _text.equals(other._text);
    }

    @Override
    public int hashCode() {
      return _text.hashCode();
    }
  }

  /** A {@link Fragment} that is linked to a vendor structure. */
  public static final class LinkFragment implements Fragment {
    private final String _text;
    private final VendorStructureId _vendorStructureId;

    LinkFragment(String text, VendorStructureId vendorStructureId) {
      checkArgument(!text.isEmpty(), "%s cannot be empty", PROP_TEXT);
      _text = text;
      _vendorStructureId = vendorStructureId;
    }

    @JsonCreator
    public static LinkFragment jsonCreator(
        @Nullable @JsonProperty(PROP_TEXT) String text,
        @Nullable @JsonProperty(PROP_VENDOR_STRUCTURE_ID) VendorStructureId vendorStructureId) {
      checkNotNull(text, "%s cannot be null", PROP_TEXT);
      checkNotNull(vendorStructureId, "%s cannot be null", PROP_VENDOR_STRUCTURE_ID);
      return new LinkFragment(text, vendorStructureId);
    }

    @Override
    @JsonProperty(PROP_TEXT)
    public String getText() {
      return _text;
    }

    @JsonProperty(PROP_VENDOR_STRUCTURE_ID)
    public VendorStructureId getVendorStructureId() {
      return _vendorStructureId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof LinkFragment)) {
        return false;
      }
      LinkFragment other = (LinkFragment) o;
      return _text.equals(other._text) && _vendorStructureId.equals(other._vendorStructureId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_text, _vendorStructureId);
    }
  }

  /** A {@link TraceElement} builder. */
  public static final class Builder {
    private List<Fragment> _fragments = new ArrayList<>();

    public Builder add(String text) {
      _fragments.add(new TextFragment(text));
      return this;
    }

    public Builder add(String text, VendorStructureId vendorStructureId) {
      _fragments.add(new LinkFragment(text, vendorStructureId));
      return this;
    }

    public TraceElement build() {
      return new TraceElement(_fragments);
    }
  }

  private final @Nonnull List<Fragment> _fragments;

  TraceElement(List<Fragment> fragments) {
    checkArgument(
        !fragments.isEmpty(),
        "TraceElement fragments must be non-empty. Use null TraceElement instead.");
    _fragments = ImmutableList.copyOf(fragments);
  }

  @JsonCreator
  private static TraceElement jsonCreator(
      @Nullable @JsonProperty(PROP_FRAGMENTS) List<Fragment> fragments) {
    return new TraceElement(firstNonNull(fragments, ImmutableList.of()));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static TraceElement of(String text) {
    return TraceElement.builder().add(text).build();
  }

  @JsonProperty(PROP_FRAGMENTS)
  public List<Fragment> getFragments() {
    return _fragments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TraceElement)) {
      return false;
    }
    TraceElement other = (TraceElement) o;
    return this._fragments.equals(other._fragments);
  }

  @Override
  public int hashCode() {
    return _fragments.hashCode();
  }

  @Override
  public String toString() {
    return _fragments.stream().map(Fragment::getText).collect(Collectors.joining());
  }
}
