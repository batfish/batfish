package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Vendor-independent model for Cisco PKI. */
public final class Pki implements Serializable {

  private static final String PROP_TRUSTPOINTS = "trustpoints";

  private @Nonnull SortedMap<String, Trustpoint> _trustpoints;

  public Pki() {
    _trustpoints = new TreeMap<>();
  }

  @JsonProperty(PROP_TRUSTPOINTS)
  public @Nonnull SortedMap<String, Trustpoint> getTrustpoints() {
    return _trustpoints;
  }

  @JsonProperty(PROP_TRUSTPOINTS)
  public void setTrustpoints(@Nonnull SortedMap<String, Trustpoint> trustpoints) {
    _trustpoints = trustpoints;
  }

  /** A PKI trustpoint. */
  public static final class Trustpoint implements Serializable {

    private static final String PROP_NAME = "name";
    private static final String PROP_ENROLLMENT = "enrollment";
    private static final String PROP_REVOCATION_CHECK = "revocationCheck";
    private static final String PROP_SUBJECT_ALT_NAME = "subjectAltName";
    private static final String PROP_USAGE = "usage";
    private static final String PROP_SOURCE_VRF = "sourceVrf";
    private static final String PROP_CERTIFICATE_CHAIN = "certificateChain";

    private @Nonnull String _name;
    private @Nullable String _enrollment;
    private @Nullable String _revocationCheck;
    private @Nullable String _subjectAltName;
    private @Nullable String _usage;
    private @Nullable String _sourceVrf;
    private @Nonnull List<String> _certificateChain;

    public Trustpoint(@Nonnull String name) {
      _name = name;
      _certificateChain = new ArrayList<>();
    }

    // JSON constructor
    private Trustpoint() {
      _name = "";
      _certificateChain = new ArrayList<>();
    }

    @JsonProperty(PROP_NAME)
    public @Nonnull String getName() {
      return _name;
    }

    @JsonProperty(PROP_NAME)
    public void setName(@Nonnull String name) {
      _name = name;
    }

    @JsonProperty(PROP_ENROLLMENT)
    public @Nullable String getEnrollment() {
      return _enrollment;
    }

    @JsonProperty(PROP_ENROLLMENT)
    public void setEnrollment(@Nullable String enrollment) {
      _enrollment = enrollment;
    }

    @JsonProperty(PROP_REVOCATION_CHECK)
    public @Nullable String getRevocationCheck() {
      return _revocationCheck;
    }

    @JsonProperty(PROP_REVOCATION_CHECK)
    public void setRevocationCheck(@Nullable String revocationCheck) {
      _revocationCheck = revocationCheck;
    }

    @JsonProperty(PROP_SUBJECT_ALT_NAME)
    public @Nullable String getSubjectAltName() {
      return _subjectAltName;
    }

    @JsonProperty(PROP_SUBJECT_ALT_NAME)
    public void setSubjectAltName(@Nullable String subjectAltName) {
      _subjectAltName = subjectAltName;
    }

    @JsonProperty(PROP_USAGE)
    public @Nullable String getUsage() {
      return _usage;
    }

    @JsonProperty(PROP_USAGE)
    public void setUsage(@Nullable String usage) {
      _usage = usage;
    }

    @JsonProperty(PROP_SOURCE_VRF)
    public @Nullable String getSourceVrf() {
      return _sourceVrf;
    }

    @JsonProperty(PROP_SOURCE_VRF)
    public void setSourceVrf(@Nullable String sourceVrf) {
      _sourceVrf = sourceVrf;
    }

    @JsonProperty(PROP_CERTIFICATE_CHAIN)
    public @Nonnull List<String> getCertificateChain() {
      return _certificateChain;
    }

    @JsonProperty(PROP_CERTIFICATE_CHAIN)
    public void setCertificateChain(@Nonnull List<String> certificateChain) {
      _certificateChain = certificateChain;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Trustpoint)) {
        return false;
      }
      Trustpoint that = (Trustpoint) o;
      return _name.equals(that._name)
          && Objects.equals(_enrollment, that._enrollment)
          && Objects.equals(_revocationCheck, that._revocationCheck)
          && Objects.equals(_subjectAltName, that._subjectAltName)
          && Objects.equals(_usage, that._usage)
          && Objects.equals(_sourceVrf, that._sourceVrf)
          && Objects.equals(_certificateChain, that._certificateChain);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          _name,
          _enrollment,
          _revocationCheck,
          _subjectAltName,
          _usage,
          _sourceVrf,
          _certificateChain);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Pki)) {
      return false;
    }
    Pki pki = (Pki) o;
    return Objects.equals(_trustpoints, pki._trustpoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_trustpoints);
  }
}
