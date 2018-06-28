package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface TraceEvent extends Serializable {}
