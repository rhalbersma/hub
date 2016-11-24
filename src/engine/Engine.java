
package engine;

import draughts.*;
import hub.*;
import io.*;
import util.*;

public class Engine implements Runnable {

   private static final boolean Disp_IO = true;
   private static final boolean Disp_Ponder = true;

   private final Input_Output p_io;

   private volatile boolean p_ready;
   private volatile int p_pending;
   private volatile boolean p_cancel;

   private volatile int p_ping;
   private volatile int p_pong;

   private long p_expected;
   private long p_move;
   private long p_ponder;

   private String p_info;

   public Engine() {

      String dir = Hub.vars.get("engine-dir");
      String command = Hub.vars.get("engine-command") + " hub";
      p_io = new My_Process(dir, command, null, "Engine");

      p_ready = false;
      p_pending = 0;
      p_cancel = false;

      p_ping = 0;
      p_pong = 0;

      p_expected = Move.None;
      p_move = Move.None;
      p_ponder = Move.None;
   }

   public void run() {

      synchronized(this) {

         put("hub");

         Var_List vars = Hub.vars_engine;

         for (int i = 0; i < vars.size(); i++) {
            put("set " + vars.name(i) + " " + vars.value(i));
         }

         put("init");
      }

      while (true) {

         String line = p_io.get_line();

         if (line == null) { // disconnected
            if (Hub.model != null) Hub.model.engine_quit();
            return;
         }

         if (Disp_IO) Hub.log("< " + line);

         try {
            parse_line(line);
         } catch (Bad_Input e) {
            e.printStackTrace();
         }
      }
   }

   private void parse_line(String line) throws Bad_Input {

      String[] tokens = line.split(" ");
      String command = (tokens.length < 1) ? "" : tokens[0];

      if (command.equals("author")) {

         String author = tokens[1];

         // TODO

      } else if (command.equals("country")) {

         String country = tokens[1];

         // TODO

      } else if (command.equals("error")) {

         Hub.model.engine_search_info(line); // HACK

      } else if (command.equals("info")) {

         p_info = line;

         if (Disp_Ponder || p_expected == Move.None) {
            parse_info(line);
         }

      } else if (command.equals("move")) {

         boolean current;

         synchronized(this) {

            assert p_pending > 0;
            p_pending--;

            current = p_pending == 0 && !p_cancel;
         }

         if (current) {

            long mv = Move.from_hub(tokens[1]);
            long ponder = (tokens.length < 3) ? Move.None : Move.from_hub(tokens[2]);

            if (p_expected != Move.None) { // premature end of ponder search
               p_move = mv;
               p_ponder = ponder;
            } else {
               Hub.model.engine_move(mv, ponder);
            }
         }

      } else if (command.equals("name")) {

         String name = tokens[1];

         // TODO

      } else if (command.equals("pong")) {

         p_pong++;
         if (p_pong == p_ping) Hub.model.engine_state("Pong " + p_pong);

      } else if (command.equals("ready")) {

         p_ready = true;
         if (Hub.model != null) Hub.model.engine_state("Ready");

      } else if (command.equals("var")) {

         String name = tokens[1];
         String value = tokens[2];

         // TODO

      } else if (command.equals("wait")) {

         // no-op
      }
   }

   private void parse_info(String line) {

      String[] tokens = line.split(" ");
      assert tokens[0].equals("info");

      String info = "";

      int depth = Integer.parseInt(tokens[1]);
      info += depth;

      double ply = Double.parseDouble(tokens[2]);
      if (ply != 0.0) info += "/" + String.format("%.1f", ply);

      double score = (double) Integer.parseInt(tokens[3]) / 100.0;
      info += "  " + String.format("%+.2f", score);

      long node = Long.parseLong(tokens[4]);
      // ignore

      double time = Double.parseDouble(tokens[5]);
      // ignore

      double speed = Double.parseDouble(tokens[6]);
      if (speed != 0.0) info += "  " + String.format("%.1fM", speed);

      info += " ";

      if (p_expected != Move.None) info += " (" + Move.to_string(p_expected, null) + ")"; // HACK, "pos" is not used

      for (int i = 7; i < tokens.length; i++) {
         info += " " + tokens[i]; // PV move
      }

      Hub.model.engine_search_info(info);
   }

   public boolean ready() {
      return p_ready;
   }

   public void new_game() {
      put("new");
   }

   public void search(Game game) {

      synchronized(this) {

         put_pos(game);

         int sd = game.pos().turn();
         put("level " + game.moves(sd) + " " + game.time(sd) + " " + game.inc());

         put("go");
         search();
      }

      Hub.model.engine_search_info("...");
   }

   public void ponder(Game game, long expected) {

      synchronized(this) {

         game.add_move(expected);
         put_pos(game);
         int sd = game.pos().turn();
         put("level " + game.moves(sd) + " " + game.time(sd) + " " + game.inc());
         game.go_to(game.i() - 1);

         put("ponder");
         search();
         p_expected = expected;
      }

      if (Disp_Ponder) Hub.model.engine_search_info("...");
   }

   public void analyse(Game game) {

      synchronized(this) {

         put_pos(game);

         put("analyse");
         search();
      }

      Hub.model.engine_search_info("...");
   }

   private synchronized void search() {

      p_pending++;
      p_cancel = false;

      p_ping = 0;
      p_pong = 0;

      p_expected = Move.None;
      p_move = Move.None;
      p_ponder = Move.None;

      p_info = null;
   }

   public synchronized void cancel() {
      stop();
      p_cancel = true;
   }

   public synchronized void stop() {
      if (p_pending != 0) put("stop");
   }

   public synchronized void ponder_hit() {

      assert p_expected != Move.None;

      p_expected = Move.None;

      if (p_info != null) parse_info(p_info);

      if (p_move != Move.None) { // ponder search already finished
         Hub.model.engine_move(p_move, p_ponder);
      } else {
         put("ponder-hit");
      }
   }

   public synchronized void ping() {
      put("ping");
      p_ping++;
   }

   public synchronized void quit() {
      cancel();
      put("quit");
   }

   private synchronized void put_pos(Game game) {

      int start = game.i() - game.ply();

      Pos pos = game.pos(start);
      put("pos " + pos.toString());

      for (int i = start; i < game.i(); i++) {
         long mv = game.move(i);
         put("move " + Move.to_hub(mv));
         pos = new Pos(pos, mv);
      }
   }

   private synchronized void put(String line) {
      p_io.put_line(line);
      if (Disp_IO) Hub.log("> " + line);
   }
}

