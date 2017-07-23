
package engine;

import draughts.*;
import hub.*;
import io.*;
import util.*;

public class Engine_Hub implements Runnable {

   private static final boolean Disp_IO = true;
   private static final boolean Disp_Ponder = true;

   private final Input_Output p_io;
   private final int p_id;
   private Engine_User p_user;

   private Var_List p_set_params;
   private volatile Var_List p_params;
   private volatile String p_name;

   private volatile boolean p_ready;
   private volatile int p_pending;
   private volatile boolean p_cancel;

   private int p_ping;

   private Pos p_pos;
   private long p_expected;
   private boolean p_wait;

   private String p_info;

   public Engine_Hub(int id, String dir, String command, Var_List set_params) {

      p_io = new My_Process(dir, command, null, "Engine");
      p_id = id;
      p_user = null;

      p_set_params = set_params;
      p_params = new Var_List();
      p_name = null;

      p_ready = false;
      p_pending = 0;
      p_cancel = false;

      p_ping = 0;

      p_pos = null;
      p_expected = Move.None;
      p_wait = false;
   }

   public void set_user(Engine_User user) {
      p_user = user;
   }

   public void run() {

      assert p_user != null;

      put("hub");

      while (true) {

         String line = p_io.get_line();

         if (line == null) { // disconnected
            p_user.engine_quit();
            return;
         }

         if (Disp_IO /* && !line.startsWith("info") */) Hub.log("<" + p_id + " " + line);

         try {
            parse_line(line);
         } catch (Bad_Input e) {
            e.printStackTrace();
         }
      }
   }

   private void parse_line(String line) throws Bad_Input {

      Hub_Scanner scan = new Hub_Scanner(line);

      if (scan.is_end()) return; // skip empty line

      String command = scan.get_command();

      if (false) {

      } else if (command.equals("done")) {

         parse_done(scan);

      } else if (command.equals("error")) {

         while (!scan.is_end()) {

            Pair p = scan.get_pair();

            if (p.name().equals("message")) {
               p_user.engine_search_info("error: " + p.value()); // HACK
            }
         }

      } else if (command.equals("id")) {

         parse_id(scan);

      } else if (command.equals("info")) {

         p_info = line;

         if (Disp_Ponder || p_expected == Move.None) {
            parse_info(line);
         }

      } else if (command.equals("param")) {

         parse_param(scan);

      } else if (command.equals("pong")) {

         synchronized(this) {

            assert p_ping > 0;
            p_ping--;

            if (p_ping == 0) p_user.engine_pong();
         }

      } else if (command.equals("ready")) {

         p_ready = true;
         p_user.engine_ready(p_name);

      } else if (command.equals("wait")) {

         for (int i = 0; i < p_set_params.size(); i++) {
            set_param(p_set_params.name(i), p_set_params.value(i));
         }

         put("init");
      }
   }

   private synchronized void parse_done(Hub_Scanner scan) throws Bad_Input {

      assert p_pending > 0;
      p_pending--;

      if (p_pending == 0 && !p_cancel) {

         long move = Move.None;
         long expecting = Move.None;

         while (!scan.is_end()) {

            Pair p = scan.get_pair();

            if (p.name().equals("move")) {
               move = Move.from_hub(p.value());
            } else if (p.name().equals("ponder")) {
               expecting = Move.from_hub(p.value());
            }
         }

         if (p_expected != Move.None) { // premature end of ponder search

            p_wait = true;

            while (p_wait) {

               try {
                  wait();
               } catch (InterruptedException e) {
                  // ignore
               }
            }
         }

         if (p_pending == 0 && !p_cancel) { // can change during wait()
            p_user.engine_move(move, expecting);
         }
      }
   }

   private void parse_id(Hub_Scanner scan) throws Bad_Input {

      String name = null;
      String version = null;
      String author = null;
      String country = null;

      while (!scan.is_end()) {

         Pair p = scan.get_pair();

         if (false) {
         } else if (p.name().equals("name")) {
            name = p.value();
         } else if (p.name().equals("version")) {
            version = p.value();
         } else if (p.name().equals("author")) {
            author = p.value();
         } else if (p.name().equals("country")) {
            country = p.value();
         }
      }

      if (name != null) {
         p_name = name;
         if (version != null) p_name += " " + version;
      }
   }

   private void parse_info(String line) throws Bad_Input {

      if (p_pending > 1 || p_cancel) return; // information is not current

      Hub_Scanner scan = new Hub_Scanner(line);

      String command = scan.get_command(); // skip command
      assert command.equals("info");

      // parse arguments

      int depth = 0;
      double mean_depth = 0.0;
      int max_depth = 0;
      double score = 0.0;
      long node = 0;
      double time = 0.0;
      double speed = 0.0;
      String pv_string = "";

      while (!scan.is_end()) {

         Pair p = scan.get_pair();

         if (false) {
         } else if (p.name().equals("depth")) {
            depth = Integer.parseInt(p.value());
         } else if (p.name().equals("mean-depth")) {
            mean_depth = Double.parseDouble(p.value());
         } else if (p.name().equals("max-depth")) {
            max_depth = Integer.parseInt(p.value());
         } else if (p.name().equals("score")) {
            score = Double.parseDouble(p.value());
         } else if (p.name().equals("nodes")) {
            node = Long.parseLong(p.value());
         } else if (p.name().equals("time")) {
            time = Double.parseDouble(p.value());
         } else if (p.name().equals("nps")) {
            speed = Double.parseDouble(p.value());
         } else if (p.name().equals("pv")) {
            pv_string = p.value();
         }
      }

      // build output

      String info = "";

      info += Integer.toString(depth);
      if (mean_depth != 0.0) info += "/" + String.format("%.1f", mean_depth);
      // if (max_depth  != 0)   info += "/" + Integer.toString(max_depth);
      info += "  " + String.format("%+.2f", score);
      if (speed != 0.0) info += "  " + String.format("%.1fM", speed);

      // format PV

      info += " ";

      synchronized(this) {

         Pos pos = p_pos;

         if (p_expected != Move.None) {
            info += " (" + Move.to_string(p_expected, pos) + ")";
            pos = pos.succ(p_expected);
         }

         for (String move_string : pv_string.split("\\s+")) { // split on white space

            if (move_string.isEmpty()) continue;

            long mv = Move.from_hub(move_string);
            if (!Move.is_legal(mv, pos)) break;

            info += " " + Move.to_string(mv, pos);
            pos = pos.succ(mv);
         }
      }

      p_user.engine_search_info(info);
   }

   private void parse_param(Hub_Scanner scan) throws Bad_Input {

      String name = null;
      String value = "";

      while (!scan.is_end()) {

         Pair p = scan.get_pair();

         if (false) {
         } else if (p.name().equals("name")) {
            name = p.value();
         } else if (p.name().equals("value")) {
            value = p.value();
         }
      }

      if (name != null) p_params.set(name, value);
   }

   public boolean has_param(String name) {
      return p_params.has(name);
   }

   public synchronized String get_param(String name) {

      if (p_params.has(name)) {
         return p_params.get(name);
      } else {
         return null;
      }
   }

   public synchronized void set_param(String name, String value) {

      if (p_params.has(name) && !p_params.get(name).equals(value)) {
         put("set-param" + pair("name", name) + pair("value", value));
         p_params.set(name, value);
      }
   }

   public boolean is_ready() {
      return p_ready;
   }

   public String name() {
      return p_name;
   }

   public void new_game() {
      assert is_ready();
      put("new-game");
   }

   public void think(Game game) {

      assert is_ready();

      synchronized(this) {

         Pos pos = game.pos();

         put_pos(game);
         put_time(game);

         put("go think");
         search(pos);
      }

      p_user.engine_search_info("...");
   }

   public boolean can_ponder(Game game, long expected) {

      if (!has_param("ponder")) return false;

      Node new_node = game.node().succ(expected);
      return !new_node.is_end();
   }

   public void ponder(Game game, long expected) {

      assert is_ready();

      synchronized(this) {

         Game new_game = game.clone(); // local copy for adding expected move

         Pos pos = new_game.pos();

         new_game.add_move(expected);
         put_pos(new_game);
         put_time(new_game);

         put("go ponder");
         search(pos);
         p_expected = expected;
      }

      if (Disp_Ponder) p_user.engine_search_info("...");
   }

   public void analyse(Game game) {

      assert is_ready();

      synchronized(this) {

         Pos pos = game.pos();

         put_pos(game);
         put("level infinite");

         put("go analyze");
         search(pos);
      }

      p_user.engine_search_info("...");
   }

   private void search(Pos pos) {

      assert p_ping == 0;

      p_pending++;
      p_cancel = false;

      p_pos = pos;
      p_expected = Move.None;
      p_wait = false;

      p_info = null;
   }

   public synchronized void cancel() {
      p_cancel = true;
      stop();
   }

   public synchronized void stop() {

      if (p_wait) { // ponder search already finished
         p_wait = false;
         notify();
      } else if (p_pending != 0) {
         put("stop");
      }
   }

   public synchronized void ponder_hit(long mv) {

      assert is_ready();
      assert p_expected == mv;

      p_pos = p_pos.succ(p_expected);
      p_expected = Move.None;

      if (p_info != null) { // reformat last search information

         try {
            parse_info(p_info);
         } catch (Bad_Input e) {
            e.printStackTrace();
         }
      }

      if (p_wait) { // ponder search already finished
         p_wait = false;
         notify();
      } else {
         put("ponder-hit");
      }
   }

   public synchronized void ping() {

      assert is_ready();

      put("ping");
      p_ping++;
   }

   public void quit() {
      cancel();
      put("quit");
   }

   private void put_pos(Game game) {

      String line = "pos";

      int start = game.i() - game.ply();
      Pos pos = game.pos(start);
      line += pair("pos", pos.toString());

      String moves = "";

      for (int i = start; i < game.i(); i++) {

         long mv = game.move(i);

         if (i != start) moves += " ";
         moves += Move.to_hub(mv, pos);

         pos = pos.succ(mv);
      }

      if (!moves.isEmpty()) line += pair("moves", moves);

      put(line);
   }

   private void put_time(Game game) {

      String line = "level";

      int moves = game.moves(game.turn());
      double time = game.time(game.turn());
      double inc = game.inc();

      if (moves != 0) line += pair("moves", Integer.toString(moves));
      line += pair("time", String.format("%.3f", time));
      if (inc != 0.0) line += pair("inc", String.format("%.3f", inc));

      put(line);
   }

   private synchronized void put(String line) {
      p_io.put_line(line);
      if (Disp_IO) Hub.log(">" + p_id + " " + line);
   }

   private static String pair(String name, String value) {

      if (value.isEmpty() || value.indexOf(' ') >= 0) { // TODO: use is_id()
         return " " + name + "=\"" + value + "\"";
      } else {
         return " " + name + "=" + value;
      }
   }
}

