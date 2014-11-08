package batfish.grammar;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeListener;

import batfish.representation.VendorConfiguration;

public interface ControlPlaneExtractor extends ParseTreeListener {

   VendorConfiguration getVendorConfiguration();

   List<String> getWarnings();

}
