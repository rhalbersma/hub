
package util;

import java.io.*;
import java.util.*;

import io.Stream_Input;

public class Var_List {

   static private final int Size = 256;

   private int p_size;
   private Pair[] p_pair;

   public Var_List() {
      p_size = 0;
      p_pair = new Pair[Size];
   }

   public Var_List(String file_name) {
      this();
      load(file_name);
   }

   public String get(String name) {

      for (int i = 0; i < p_size; i++) {
         if (p_pair[i].name().equals(name)) {
            return p_pair[i].value();
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

   public double get_real(String name) {
      return Double.parseDouble(get(name));
   }

   public void set(String name, String value) {

      for (int i = 0; i < p_size; i++) {
         if (p_pair[i].name().equals(name)) {
            p_pair[i] = Pair.make(name, value);
            return;
         }
      }

      p_pair[p_size] = Pair.make(name, value);
      p_size++;
   }

   public boolean has(String name) {

      for (int i = 0; i < p_size; i++) {
         if (p_pair[i].name().equals(name)) {
            return true;
         }
      }

      return false;
   }

   public void load(String file_name) {

      try {

         InputStream is = new FileInputStream(file_name);
         Stream_Input si = new Stream_Input(is);

         while (si.has_line()) {

            String line = si.get_line();

            if (line.isEmpty()) continue; // skip empty line
            if (line.charAt(0) == '#') continue; // skip comment

            String[] fields = line.split("=");
            String name = fields[0].trim();
            String value = (fields.length == 2) ? fields[1].trim() : "";
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
      return pair(n).name();
   }

   public String value(int n) {
      return pair(n).value();
   }

   public Pair pair(int n) {
      assert n < p_size;
      return p_pair[n];
   }
}

