
package util;

import java.io.*;
import java.util.*;

public class Var_List {

   static private final int Size = 256;

   private int p_size;
   private String[] p_name;
   private String[] p_value;

   public Var_List() {
      p_size = 0;
      p_name = new String[Size];
      p_value = new String[Size];
   }

   public String get(String name) {

      for (int i = 0; i < p_size; i++) {
         if (p_name[i].equals(name)) {
            return p_value[i];
         }
      }

      assert false;
      return null;
   }

   public boolean get_bool(String name) {
      String value = get(name);
      assert value.equals("true") || value.equals("false");
      return Boolean.parseBoolean(value);
   }

   public int get_int(String name) {
      return Integer.parseInt(get(name));
   }

   public void set(String name, String value) {

      for (int i = 0; i < p_size; i++) {
         if (p_name[i].equals(name)) {
            p_value[i] = value;
            return;
         }
      }

      p_name[p_size] = name;
      p_value[p_size] = value;
      p_size++;
   }

   public boolean has(String name) {

      for (int i = 0; i < p_size; i++) {
         if (p_name[i].equals(name)) {
            return true;
         }
      }

      return false;
   }

   public void load(String file_name) {

      try {

         InputStream is = new FileInputStream(file_name);
         Scanner scan = new Scanner(is);

         while (scan.hasNext()) {

            assert scan.hasNext();
            String name = scan.next();

            assert scan.hasNext();
            String sep = scan.next();
            assert sep.equals("=");

            assert scan.hasNext();
            String value = scan.next();

            set(name, value);
         }

      } catch (Exception e) {

         e.printStackTrace();
      }
   }

   public int size() {
      return p_size;
   }

   public String name(int n) {
      assert(n < p_size);
      return p_name[n];
   }

   public String value(int n) {
      assert(n < p_size);
      return p_value[n];
   }
}

