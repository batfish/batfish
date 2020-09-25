package org.batfish.representation.terraform;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;

interface TerraformFileContent extends Serializable {
  List<TerraformResource> toConvertedResources(Warnings warnings);
}
