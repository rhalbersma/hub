
package hub;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

import draughts.*;
import engine.*;
import gui.*;
import util.*;

public class Model_User implements Engine_User {

   private static final boolean Auto_Save = true;

   private final Engine_Hub p_engine;
   private String p_name;
   private boolean p_ready;

   private Game p_game;
   private Timer p_timer;

   private boolean[] p_computer;
   private boolean p_ponder;
   private boolean p_analyse;
   private long p_expected;

   private long p_clicked;

   private My_Sound p_sound;

   public Model_User(Engine_Hub engine) {

      engine.set_user(this);

      p_engine = engine;
      p_name = "Computer";
      p_ready = false;

      p_game = new Game();
      p_timer = new Timer();

      p_computer = new boolean[] { false, true };
      p_ponder = Hub.vars.get_bool("game-ponder");
      p_analyse = false;
      p_expected = Move.None;

      p_clicked = 0;

      if (!Hub.vars.get("gui-sound").isEmpty()) {
         p_sound = new My_Sound(Hub.vars.get("gui-sound"));
      }

      // start

      Hub.log("start");
      Hub.gui.set_state("Init");

      // the game will actually start when the engine is ready
   }

   public synchronized void new_game() {

      if (!p_engine.is_ready()) return;

      Hub.log("new-game #");
      set_game(new Game(), false);
   }

   public synchronized void load_game() {

      if (!p_engine.is_ready()) return;

      Hub.log("load-game #");

      try {
         set_game(PDN_Input.load_game("game.pdn"), true);
      } catch (Bad_Input e) {
         e.printStackTrace();
         return;
      }
   }

   public synchronized void set_pos(String s) {

      if (!p_engine.is_ready()) return;

      Hub.log("set-pos " + s + " #");

      try {
         set_game(new Game(FEN.from_fen(s)), true);
      } catch (Bad_Input e) {
         e.printStackTrace();
         return;
      }
   }

   private synchronized void set_game(Game game, boolean load_clock) {

      p_engine.cancel();
      p_engine.new_game();

      p_game = game;

      int    moves = Hub.vars.get_int("game-moves");
      double time  = Hub.vars.get_real("game-time") * 60.0;
      double inc   = Hub.vars.get_real("game-inc");
      p_game.set_time_control(moves, time, inc);

      if (load_clock) {
         double clock_time = Hub.vars.get_real("load-clock") * 60.0;
         p_game.set_time(Side.White, clock_time);
         p_game.set_time(Side.Black, clock_time);
      }

      p_analyse = false;
      new_position();
   }

   public synchronized void save_game() {
      PDN_Output.save_game("game.pdn", p_game);
   }

   public synchronized void set_players(int n) {

      if (!p_engine.is_ready()) return;

      Hub.log("set-players " + n);

      int turn = p_game.turn();
      p_computer[turn] = (n == 2);
      p_computer[Side.opp(turn)] = (n != 0);
      p_analyse = false;

      update_engine();
   }

   public synchronized void go() {

      if (!p_engine.is_ready()) return;

      Hub.log("go");

      int turn = p_game.turn();
      p_computer[turn] = true;
      p_computer[Side.opp(turn)] = false;
      p_analyse = false;

      update_engine();
   }

   public synchronized void analyse() {

      if (!p_engine.is_ready()) return;

      Hub.log("analyse-mode");

      p_computer[Side.White] = false;
      p_computer[Side.Black] = false;
      p_analyse = true;

      update_engine();
   }

   public synchronized void undo() {
      Hub.log("undo");
      go_to(p_game.i() - 1);
   }

   public synchronized void redo() {
      Hub.log("redo");
      go_to(p_game.i() + 1);
   }

   public synchronized void undo_all() {
      Hub.log("undo-all");
      go_to(0);
   }

   public synchronized void redo_all() {
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

   public synchronized void move_now() {

      if (!computer_turn()) return;

      Hub.log("move-now");
      p_engine.stop();
   }

   private void new_position() { // MOVE ME?

      if (computer_turn()) { // make sure it's the user's turn
         int turn = p_game.turn();
         p_computer[Side.opp(turn)] = p_computer[turn];
         p_computer[turn] = false;
      }

      p_expected = Move.None;
      p_clicked = 0;

      update_gui();
      Hub.gui.set_info(" "); // to set window size properly

      update_engine();
   }

   private synchronized void play_move(long mv, long expecting) {

      p_game.add_move(mv, p_timer.elapsed());
      p_timer.restart();
      if (Auto_Save) save_game();

      update_gui();

      if (computer_turn() && p_ponder && p_expected != Move.None) {

         if (mv == p_expected) {
            Hub.log("ponder-hit");
            Hub.gui.set_state("Hit");
            p_expected = Move.None;
            p_engine.ponder_hit(mv);
         } else {
            Hub.log("ponder-miss");
            update_engine(expecting);
         }

      } else {

         update_engine(expecting);
      }
   }

   private void update_gui() {

      p_clicked = 0;
      draw(Move.bit(p_game.last_move()));

      Hub.gui.set_move(p_game.i() / 2 + 1);

      Hub.gui.set_clocks((int) p_game.time(Side.White),
                         (int) p_game.time(Side.Black));
   }

   private void update_engine() {
      update_engine(Move.None);
   }

   private void update_engine(long expecting) {

      p_timer.restart();
      p_engine.cancel();

      p_expected = Move.None;

      if (p_game.is_end()) {

         Hub.log("game-end #");
         Hub.gui.set_state("End");

      } else if (computer_turn()) {

         Hub.log(String.format("think number=%d time=%.1f", p_game.i() / 2 + 1, p_game.time(p_game.turn())));
         Hub.gui.set_state("Think");

         p_engine.think(p_game);

      } else if (playing() && p_analyse) {

         Hub.log("analyse");
         Hub.gui.set_state("Analyse");

         p_engine.analyse(p_game);

      } else if (user_turn() && p_ponder && expecting != Move.None && p_engine.can_ponder(p_game, expecting)) {

         Hub.log(String.format("ponder move=%s number=%d time=%.1f", Move.to_string(expecting, p_game.pos()), (p_game.i() + 1) / 2 + 1, p_game.time(Side.opp(p_game.turn()))));
         Hub.gui.set_state("Ponder");

         p_expected = expecting;
         p_engine.ponder(p_game, expecting);

      } else {

         Hub.log("wait");
         Hub.gui.set_state("Wait");
      }
   }

   public synchronized void click(int sq) {

      if (!user_turn()) return;

      Pos pos = p_game.pos();

      if (pos.is_empty(sq)) {
         p_clicked &= ~pos.empty();
      } else if (pos.is_side(sq, pos.turn())) {
         p_clicked &= ~pos.side(pos.turn());
      }

      p_clicked = Bit.flip(p_clicked, sq);
      user_clicked();
   }

   public synchronized void unclick() {

      if (!user_turn()) return;

      p_clicked = 0;
      draw(p_clicked);
   }

   public synchronized void drag(int from, int to) {

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

   public synchronized void single_move() {

      if (!user_turn()) return;

      List list = Gen.gen_moves(p_game.pos());
      if (list.size() == 1) user_move(list.move(0));
   }

   private void user_move(long mv) {

      if (!user_turn()) return;

      Hub.log(String.format("user-move move=%s number=%d time=%.1f", Move.to_string(mv, p_game.pos()), p_game.i() / 2 + 1, p_timer.elapsed()));

      play_move(mv, Move.None);
   }

   public synchronized void engine_move(long mv, long expecting) {

      if (!computer_turn()) return;

      Hub.log(String.format("engine-move move=%s number=%d time=%.1f", Move.to_string(mv, p_game.pos()), p_game.i() / 2 + 1, p_timer.elapsed()));

      play_move(mv, expecting);

      if (!Hub.vars.get("gui-sound").isEmpty()) {
         p_sound.play();
      }
   }

   public synchronized void engine_ready(String name) {

      if (name != null) p_name = name;
      p_ready = true;

      Hub.gui.set_title(name);

      new_game();
   }

   public synchronized void engine_quit() {
      assert false;
   }

   public void engine_search_info(String info) {
      Hub.gui.set_info(info);
   }

   public synchronized void engine_pong() {
      // ignore
   }

   private boolean user_turn() {
      return playing() && !p_computer[p_game.turn()];
   }

   private boolean computer_turn() {
      return playing() && p_computer[p_game.turn()];
   }

   private boolean playing() {
      return p_ready && !p_game.is_end();
   }

   private void draw(long bit) {
      Hub.gui.set_board(p_game.pos(), bit);
   }
}

class My_Sound {

   private Clip p_clip;

   My_Sound(String file_name) {

      try {

         File file = new File(file_name);
         // InputStream file = getClass().getResourceAsStream("/" + file_name);
         // URL file = getClass().getResource("/" + file_name);
         // URL file = getClass().getResource(file_name);

         AudioInputStream ais = AudioSystem.getAudioInputStream(file);

         p_clip = AudioSystem.getClip();
         p_clip.open(ais);

      } catch (Exception e) {

         e.printStackTrace();
      }
   }

   public void play() {

      if (!p_clip.isRunning()) {
         p_clip.setFramePosition(0);
         p_clip.start();
      }
   }
}

