package org.batfish.graphviz;

import java.util.Map;

import org.batfish.common.BatfishLogger;
import org.batfish.job.BatfishJobResult;
import org.batfish.representation.Prefix;

public final class GraphvizResult extends BatfishJobResult<Map<String, byte[]>> {

   private final byte[] _graphBytes;

   private final String _graphFile;

   private final Prefix _prefix;

   private final byte[] _svgBytes;

   private final String _svgFile;

   public GraphvizResult(long elapsedTime, String graphFile, byte[] graphBytes,
         String svgFile, byte[] svgBytes, Prefix prefix) {
      super(elapsedTime);
      _graphBytes = graphBytes;
      _graphFile = graphFile;
      _prefix = prefix;
      _svgBytes = svgBytes;
      _svgFile = svgFile;
   }

   public GraphvizResult(long elapsedTime, String graphFile, String svgFile,
         Prefix prefix, Throwable failureCause) {
      super(elapsedTime, failureCause);
      _graphBytes = null;
      _graphFile = graphFile;
      _prefix = prefix;
      _svgBytes = null;
      _svgFile = svgFile;
   }

   @Override
   public void applyTo(Map<String, byte[]> output, BatfishLogger logger) {
      output.put(_graphFile, _graphBytes);
      output.put(_svgFile, _svgBytes);
   }

   @Override
   public void explainFailure(BatfishLogger logger) {
   }

   @Override
   public String toString() {
      return "<Computed graph for prefix: " + _prefix.toString() + ">";
   }

}
