package batfish.representation;

public class Prefix implements Comparable<Prefix> {
   private Ip _network;
   private int _prefixLength;

   public Prefix(Ip network, int prefixLength) {
      _network = network;
      _prefixLength = prefixLength;
   }

   public Prefix(String text) {
      String[] parts = text.split("/");
      _network = new Ip(parts[0]);
      _prefixLength = Integer.parseInt(parts[1]);
   }

   @Override
   public int compareTo(Prefix rhs) {
      int ret = _network.compareTo(rhs._network);
      if (ret != 0) {
         return ret;
      }
      return Integer.compare(_prefixLength, rhs._prefixLength);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      Prefix rhs = (Prefix) obj;
      return _network.equals(rhs._network)
            && _prefixLength == rhs._prefixLength;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _network.hashCode();
      result = prime * result + _prefixLength;
      return result;
   }
}
