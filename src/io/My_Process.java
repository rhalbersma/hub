
package io;

import java.io.*;

public class My_Process implements Input_Output {

   private Process p_process;
   private Input_Output p_io;

   public My_Process(String dir_name, String command, Output log, String name) {

      try {

         java.io.File dir = new java.io.File(dir_name);
         p_process = Runtime.getRuntime().exec(command, null, dir);

         Input  in  = new Stream_Input(p_process.getInputStream());
         Output out = new Stream_Output(p_process.getOutputStream());

         p_io = new Logged_Input_Output(in, out, log, name);

      } catch (IOException e) {

         e.printStackTrace();
      }
   }

   public String get_line() {
      return p_io.get_line();
   }

   public void put_line(String string) {
      p_io.put_line(string);
   }

   public boolean ready() {
      return p_io.ready();
   }

   public void close() {
      p_io.close();
   }
}

