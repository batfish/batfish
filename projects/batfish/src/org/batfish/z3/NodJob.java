package org.batfish.z3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.batfish.main.BatfishException;

import com.microsoft.z3.Context;
import com.microsoft.z3.Fixedpoint;
import com.microsoft.z3.Params;
import com.microsoft.z3.Z3Exception;

public class NodJob implements Callable<NodJobResult> {

   private Path _dataPlanePath;
   private Path _outputPath;
   private Path _queryPath;

   public NodJob(Path dataPlanePath, Path queryPath) {
      _dataPlanePath = dataPlanePath;
      _queryPath = queryPath;
   }

   @Override
   public NodJobResult call() throws Exception {
      _outputPath = Paths.get(_queryPath.toString() + ".out");
      String concatenatedQuery = null;
      try {
         String dataPlane = readFile(_dataPlanePath.toFile());
         String queryText = readFile(_queryPath.toFile());
         concatenatedQuery = dataPlane + queryText;
      }
      catch (BatfishException e) {
         return new NodJobResult(false, new BatfishException(
               "Could not concatenate query to data plane", e));
      }

      try {
         Context ctx = new Context();
         Params p = ctx.mkParams();
         p.add("fixedpoint.engine", "datalog");
         p.add("fixedpoint.datalog.default_relation", "doc");
         p.add("fixedpoint.print.answer", true);
         Fixedpoint fix = ctx.mkFixedpoint();
         fix.setParameters(p);
      }
      catch (Z3Exception e) {
         return new NodJobResult(false, new BatfishException(
               "Error running NoD on concatenated data plane", e));
      }
      return new NodJobResult(true, null);
   }

   public Path getOutputPath() {
      return _outputPath;
   }

   private String readFile(File file) {
      String text = null;
      try {
         text = FileUtils.readFileToString(file);
      }
      catch (IOException e) {
         throw new BatfishException("Failed to read file: " + file.toString(),
               e);
      }
      return text;
   }

}
