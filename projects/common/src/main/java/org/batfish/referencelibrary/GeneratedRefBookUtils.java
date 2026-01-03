package org.batfish.referencelibrary;

import org.batfish.datamodel.Names;

public class GeneratedRefBookUtils {

  public enum BookType {
    AwsSeviceIps,
    PoolAddresses,
    PublicIps,
    VirtualAddresses
  }

  public static String getName(String hostname, BookType type) {
    return Names.generatedReferenceBook(hostname, type.toString());
  }
}
