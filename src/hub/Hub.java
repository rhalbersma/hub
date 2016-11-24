
package hub;

import java.io.*;

import draughts.*;
import engine.*;
import gui.*;
import util.*;

public class Hub {

   public static final boolean Disp_Log = true;

   public static Var_List vars;
   public static Var_List vars_engine;

   public static Model model;
   public static GUI gui;
   public static Engine engine;

   private static PrintWriter p_log;

   public static void main(String args[]) {

      vars = new Var_List();
      vars.set("pdn-trace", "false");
      vars.set("gui-sound", "");
      vars.load("hub.ini");

      vars_engine = new Var_List();
      vars_engine.load("engine.ini");

      if (vars.get_bool("log")) {
         try {
            OutputStream os = new FileOutputStream("log.txt", true);
            p_log = new PrintWriter(os, true);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      log("---");
      log("init ###");

      Draughts.init();

      engine = new Engine();
      gui = new GUI();
      model = new Model();

      util.Thread.launch(engine);
      model.start();

      log("init done");
   }

   public static void log(String s) {
      if (Disp_Log) System.out.println(s);
      if (p_log != null) p_log.println(s);
   }
}

