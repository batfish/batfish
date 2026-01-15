package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class PkiTrustpoint implements Serializable {

  private final String _name;
  private String _enrollment;
  private String _revocationCheck;
  private String _subjectAltName;
  private String _usage;
  private String _sourceVrf;
  private final List<String> _certificateChain;

  public PkiTrustpoint(String name) {
    _name = name;
    _certificateChain = new ArrayList<>();
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getEnrollment() {
    return _enrollment;
  }

  public void setEnrollment(@Nullable String enrollment) {
    _enrollment = enrollment;
  }

  public @Nullable String getRevocationCheck() {
    return _revocationCheck;
  }

  public void setRevocationCheck(@Nullable String revocationCheck) {
    _revocationCheck = revocationCheck;
  }

  public @Nullable String getSubjectAltName() {
    return _subjectAltName;
  }

  public void setSubjectAltName(@Nullable String subjectAltName) {
    _subjectAltName = subjectAltName;
  }

  public @Nullable String getUsage() {
    return _usage;
  }

  public void setUsage(@Nullable String usage) {
    _usage = usage;
  }

  public @Nullable String getSourceVrf() {
    return _sourceVrf;
  }

  public void setSourceVrf(@Nullable String sourceVrf) {
    _sourceVrf = sourceVrf;
  }

  public List<String> getCertificateChain() {
    return _certificateChain;
  }

  public void addCertificateLine(String line) {
    _certificateChain.add(line);
  }
}
