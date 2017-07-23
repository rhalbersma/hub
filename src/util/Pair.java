
package util;

public class Pair {

   private final String p_name;
   private final String p_value;

   public static Pair make(String name, String value) {
      return new Pair(name, value);
   }

   public Pair(String name, String value) {
      p_name = name;
      p_value = value;
   }

   public String name() {
      return p_name;
   }

   public String value() {
      return p_value;
   }
}

