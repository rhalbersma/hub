
package hub;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

import draughts.*;
import gui.*;
import util.*;

public class Model {

   private Game p_game;
   private Timer p_timer;

   private boolean[] p_computer;
   private boolean p_ponder;
   private boolean p_analyse;
   private long p_expected;
   private long p_clicked;

   private My_Sound p_sound;

   public Model() {

      p_game = new Game();
      p_timer = new Timer();
      p_computer = new boolean[Side.Size];

      if (!Hub.vars.get("gui-sound").equals("")) {
         p_sound = new My_Sound(Hub.vars.get("gui-sound"));
      }
   }

   public void start() {

      Hub.log("start");

      set_tc();
      p_timer.restart();

      p_computer[Side.White] = false;
      p_computer[Side.Black] = true;
      p_ponder = Hub.vars.get_bool("game-ponder");
      p_analyse = false;

      p_expected = Move.None;
      p_clicked = 0;

      Hub.gui.set_title(Hub.vars.get("engine-name"));

      new_game();
   }

   public void quit() {
      Hub.log("quit ###");
      Hub.engine.quit();
   }

   public void new_game() {

      Hub.log("new-game #");

      Hub.engine.cancel();
      Hub.engine.new_game();

      synchronized(this) {
         p_game = new Game();
         set_tc();
      }

      p_analyse = false;
      new_position();
   }

   public void load_game() {

      Hub.log("load-game #");

      Hub.engine.cancel();
      Hub.engine.new_game();

      synchronized(this) {

         try {
            p_game = PDN_Input.load_game("game.pdn");
         } catch (Bad_Input e) {
            e.printStackTrace();
            return;
         }

         set_tc();
      }

      p_analyse = false;
      new_position();
   }

   public synchronized void save_game() {
      PDN_Output.save_game("game.pdn", p_game);
   }

   private void set_tc() {
      int moves = Hub.vars.get_int("game-moves");
      int time  = Hub.vars.get_int("game-time") * 60;
      int inc   = Hub.vars.get_int("game-inc");
      p_game.set_time_control(moves, time * 1000, inc * 1000);
   }

   public void set_players(int n) {

      if (!Hub.engine.ready()) return;

      Hub.log("set-players " + n);

      int turn = p_game.pos().turn();
      p_computer[turn] = (n == 2);
      p_computer[Side.opp(turn)] = (n != 0);
      p_analyse = false;

      update_engine();
   }

   public void go() {

      if (!Hub.engine.ready()) return;

      Hub.log("go");

      int turn = p_game.pos().turn();
      p_computer[turn] = true;
      p_computer[Side.opp(turn)] = false;
      p_analyse = false;

      update_engine();
   }

   public void analyse() {

      if (!Hub.engine.ready()) return;

      Hub.log("analyse-mode");

      p_computer[Side.White] = false;
      p_computer[Side.Black] = false;
      p_analyse = true;

      update_engine();
   }

   public void undo() {
      Hub.log("undo");
      go_to(p_game.i() - 1);
   }

   public void redo() {
      Hub.log("redo");
      go_to(p_game.i() + 1);
   }

   public void undo_all() {
      Hub.log("undo-all");
      go_to(0);
   }

   public void redo_all() {
      Hub.log("redo-all");
      go_to(p_game.size());
   }

   private synchronized void go_to(int n) {
      if (n >= 0 && n <= p_game.size() && p_game.i() != n) {
         p_game.go_to(n);
         new_position();
      }
   }

   public void toggle_oval() {
      Hub.gui.toggle_oval();
   }

   public void reverse_board() {
      Hub.gui.reverse_board();
   }

   public void ping() {
      Hub.log("ping");
      Hub.engine.ping();
   }

   public void move_now() {

      if (!computer_turn()) return;

      Hub.log("move-now");
      Hub.engine.stop();
   }

   private void new_position() {

      if (computer_turn()) { // make sure it's the user's turn
         int turn = p_game.pos().turn();
         p_computer[Side.opp(turn)] = p_computer[turn];
         p_computer[turn] = false;
      }

      p_expected = Move.None;
      p_clicked = 0;

      update_gui();
      Hub.gui.set_info(" "); // to set window size properly
      update_engine();
   }

   private void play_move(long mv, long ponder) {

      synchronized(this) {
         p_game.add_move(mv, p_timer.elapsed());
         p_timer.restart();
      }

      update_gui();

      if (p_ponder && computer_turn() && p_expected != Move.None) {
         if (mv == p_expected) {
            Hub.log("ponder-hit");
            engine_state("Hit");
            p_expected = Move.None;
            Hub.engine.ponder_hit();
         } else {
            Hub.log("ponder-miss");
            update_engine(ponder);
         }
      } else {
         update_engine(ponder);
      }
   }

   private void update_gui() {

      long mv = (p_game.i() == 0) ? Move.None : p_game.move(p_game.i() - 1);

      p_clicked = 0;
      draw(Move.bit(mv));

      Hub.gui.set_move(p_game.i() / 2 + 1);

      int time_white = Math.max(p_game.time(Side.White), 0);
      int time_black = Math.max(p_game.time(Side.Black), 0);
      Hub.gui.set_clocks(time_white / 1000, time_black / 1000);
   }

   private void update_engine() {
      update_engine(Move.None);
   }

   private void update_engine(long ponder) {

      p_timer.restart();
      Hub.engine.cancel();

      p_expected = Move.None;

      if (p_game.is_end()) {

         Hub.log("game-end #");
         engine_state("End");

      } else if (computer_turn()) {

         Hub.log(String.format("search %d %.1f", p_game.i() / 2 + 1, (double) p_game.time(p_game.pos().turn()) / 1000.0));
         engine_state("Think");

         Hub.engine.search(p_game);

      } else if (playing() && p_analyse) {

         Hub.log("analyse");
         engine_state("Analyse");

         Hub.engine.analyse(p_game);

      } else if (user_turn() && p_ponder && ponder != Move.None) {

         Hub.log(String.format("ponder %s %d %.1f", Move.to_string(ponder, p_game.pos()), (p_game.i() + 1) / 2 + 1, p_game.time(Side.opp(p_game.pos().turn())) / 1000.0));
         engine_state("Ponder");

         p_expected = ponder;
         Hub.engine.ponder(p_game, ponder);

      } else {

         Hub.log("wait");
         engine_state("Wait");
      }
   }

   public void click(int sq) {

      if (!user_turn()) return;

      Pos pos = p_game.pos();

      if (pos.is_empty(sq)) {
         p_clicked &= ~pos.empty();
      } else if (pos.is_side(sq, pos.turn())) {
         p_clicked &= ~pos.piece(pos.turn());
      }

      p_clicked = Bit.flip(p_clicked, sq);
      user_clicked();
   }

   public void unclick() {

      if (!user_turn()) return;

      p_clicked = 0;
      draw(p_clicked);
   }

   public void drag(int from, int to) {

      if (!user_turn()) return;

      p_clicked = Bit.bit(from) | Bit.bit(to);
      user_clicked();
   }

   private void user_clicked() {

      List list = Gen.gen_moves(p_game.pos());

      long mv = list.find(p_clicked);

      if (mv == Move.None) {
         p_clicked = 0;
         draw(p_clicked);
      } else if (mv == Move.Amb) {
         draw(p_clicked);
      } else {
         user_move(mv);
      }
   }

   public void single_move() {

      if (!user_turn()) return;

      List list = Gen.gen_moves(p_game.pos());

      if (list.size() == 1) {
         user_move(list.move(0));
      }
   }

   private void user_move(long mv) {

      Hub.log(String.format("user-move %s %d %.1f", Move.to_string(mv, p_game.pos()), p_game.i() / 2 + 1, (double) p_timer.elapsed() / 1000.0));

      play_move(mv, Move.None);
   }

   public void engine_move(long mv, long ponder) {

      if (!computer_turn()) return;

      Hub.log(String.format("engine-move %s %d %.1f", Move.to_string(mv, p_game.pos()), p_game.i() / 2 + 1, (double) p_timer.elapsed() / 1000.0));

      play_move(mv, ponder);

      if (!Hub.vars.get("gui-sound").equals("")) {
         p_sound.play();
      }
   }

   public void engine_quit() {
      assert(false);
   }

   public void engine_search_info(String info) {
      Hub.gui.set_info(info);
   }

   public void engine_state(String state) {
      if (Hub.gui != null) Hub.gui.set_state(Hub.engine.ready() ? state : "Init");
   }

   private boolean user_turn() {
      return playing() && !p_computer[p_game.pos().turn()];
   }

   private boolean computer_turn() {
      return playing() && p_computer[p_game.pos().turn()];
   }

   private boolean playing() {
      return !p_game.is_end() && Hub.engine.ready();
   }

   private void draw(long bit) {
      Hub.gui.set_board(p_game.pos(), bit);
   }
}

class My_Sound implements Runnable {

   private static final boolean Thread = false;

   private Clip p_clip;
   private volatile boolean p_play;

   My_Sound(String file_name) {

      try {

         File file = new File(file_name);
         // InputStream file = getClass().getResourceAsStream("/" + file_name);
         // URL file = getClass().getResource("/" + file_name);

         AudioInputStream ais = AudioSystem.getAudioInputStream(file);

         p_clip = AudioSystem.getClip();
         p_clip.open(ais);

         p_play = false;
         if (Thread) util.Thread.launch(this);

      } catch (Exception e) {

         e.printStackTrace();
      }
   }

   public void run() {

      while (true) {

         while (!p_play) {
            util.Thread.sleep(10);
         }

         play_now();
         p_play = false;
      }
   }

   public void play() {
      if (!p_clip.isRunning()) {
         p_play = true;
         if (!Thread) play_now();
      }
   }

   private void play_now() {
      p_clip.setFramePosition(0);
      p_clip.start();
   }
}

