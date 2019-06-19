package org.batfish.datamodel.visitors;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclTracer;

public class IpSpaceDescriber implements GenericIpSpaceVisitor<String> {

  private final AclTracer _aclTracer;

  public IpSpaceDescriber(AclTracer aclTracer) {
    _aclTracer = aclTracer;
  }

  @Override
  public String castToGenericIpSpaceVisitorReturnType(Object o) {
    return (String) o;
  }

  private @Nullable String computeMetadataDescription(IpSpace ipSpace) {
    IpSpaceMetadata ipSpaceMetadata = _aclTracer.getIpSpaceMetadata().get(ipSpace);
    if (ipSpaceMetadata != null) {
      return String.format(
          "'%s' named '%s'", ipSpaceMetadata.getSourceType(), ipSpaceMetadata.getSourceName());
    }
    return null;
  }

  @Override
  public String visitAclIpSpace(AclIpSpace aclIpSpace) {
    String metadataDescription = computeMetadataDescription(aclIpSpace);
    if (metadataDescription != null) {
      return metadataDescription;
    }
    ImmutableList.Builder<String> lineDescs = ImmutableList.builder();
    CommonUtil.<AclIpSpaceLine>forEachWithIndex(
        aclIpSpace.getLines(),
        (i, line) -> lineDescs.add(String.format("%d: %s", i, line.getIpSpace().accept(this))));
    return lineDescs.build().toString();
  }

  @Override
  public String visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    String metadataDescription = computeMetadataDescription(emptyIpSpace);
    if (metadataDescription != null) {
      return metadataDescription;
    }
    return emptyIpSpace.toString();
  }

  @Override
  public String visitIpIpSpace(IpIpSpace ipIpSpace) {
    String metadataDescription = computeMetadataDescription(ipIpSpace);
    if (metadataDescription != null) {
      return metadataDescription;
    }
    return ipIpSpace.getIp().toString();
  }

  @Override
  public String visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    String metadataDescription = computeMetadataDescription(ipSpaceReference);
    if (metadataDescription != null) {
      return metadataDescription;
    }
    String name = ipSpaceReference.getName();
    IpSpace referencedSpace = _aclTracer.getNamedIpSpaces().get(name);
    String defaultValue = String.format("An IpSpace named '%s'", name);
    if (referencedSpace == null) {
      return defaultValue;
    }
    String referencedMetadataDescription = computeMetadataDescription(referencedSpace);
    if (referencedMetadataDescription != null) {
      return referencedMetadataDescription;
    }
    return defaultValue;
  }

  @Override
  public String visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    String metadataDescription = computeMetadataDescription(ipWildcardIpSpace);
    if (metadataDescription != null) {
      return metadataDescription;
    }
    return ipWildcardIpSpace.getIpWildcard().toString();
  }

  @Override
  public String visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    String metadataDescription = computeMetadataDescription(ipWildcardSetIpSpace);
    if (metadataDescription != null) {
      return metadataDescription;
    }
    return ipWildcardSetIpSpace.toString();
  }

  @Override
  public String visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    String metadataDescription = computeMetadataDescription(prefixIpSpace);
    if (metadataDescription != null) {
      return metadataDescription;
    }
    return prefixIpSpace.getPrefix().toString();
  }

  @Override
  public String visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    String metadataDescription = computeMetadataDescription(universeIpSpace);
    if (metadataDescription != null) {
      return metadataDescription;
    }
    return universeIpSpace.toString();
  }
}
