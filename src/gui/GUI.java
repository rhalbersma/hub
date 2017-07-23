
package gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import draughts.*;
import hub.*;
import util.*;

public class GUI {

   private JFrame p_frame;
   private Panel_Board p_panel_board;
   private JPanel p_panel_east;
   private JLabel p_label_clock_north;
   private JLabel p_label_move;
   private JLabel p_label_state;
   private JLabel p_label_clock_south;
   private JLabel p_label_info;

   private String p_clock[];

   public GUI() {

      p_frame = new JFrame("Hub");
      p_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      p_frame.setLayout(new BorderLayout());

      p_panel_board = new Panel_Board();
      p_frame.add(p_panel_board, BorderLayout.CENTER);

      p_panel_east = new JPanel();
      p_panel_east.setLayout(new BoxLayout(p_panel_east, BoxLayout.Y_AXIS));
      p_frame.add(p_panel_east, BorderLayout.EAST);

      JLabel label = new JLabel();
      Font font_small = label.getFont();
      Font font_large = new Font(font_small.getName(), Font.PLAIN, Hub.vars.get_int("gui-font"));

      p_label_clock_north = new JLabel("00:00"); // to set window size properly
      p_label_clock_north.setFont(font_large);
      p_label_clock_north.setAlignmentX(Component.CENTER_ALIGNMENT);
      p_panel_east.add(p_label_clock_north);

      p_label_move = new JLabel("0"); // to set window size properly
      p_label_move.setFont(font_large);
      p_label_move.setAlignmentX(Component.CENTER_ALIGNMENT);
      p_panel_east.add(p_label_move);

      p_label_state = new JLabel("Ponder"); // to set window size properly
      p_label_state.setFont(font_large);
      p_label_state.setAlignmentX(Component.CENTER_ALIGNMENT);
      p_panel_east.add(p_label_state);

      p_label_clock_south = new JLabel("00:00"); // to set window size properly
      p_label_clock_south.setFont(font_large);
      p_label_clock_south.setAlignmentX(Component.CENTER_ALIGNMENT);
      p_panel_east.add(p_label_clock_south);

      p_label_info = new JLabel(" "); // to set window size properly
      p_label_info.setFont(font_large);
      p_frame.add(p_label_info, BorderLayout.SOUTH);

      // p_frame.setLocationRelativeTo(null); // centered
      p_frame.pack();
      p_frame.setVisible(true);

      p_clock = new String[] { "00:00", "00:00" };
   }

   public void set_title(String title) {
      p_frame.setTitle(title);
   }

   public void set_board(Pos pos, long squares) {
      p_panel_board.set_board(pos, squares);
   }

   public void set_move(int move) {
      p_label_move.setText(Integer.toString(move));
   }

   public void set_clocks(int time_white, int time_black) {

      p_clock[Side.White] = format_time(time_white);
      p_clock[Side.Black] = format_time(time_black);

      update_clocks();
   }

   private String format_time(int time) {

      String s = "";

      if (time < 0) {
         s += "-";
         time = -time;
      }

      if (time >= 3600) { // 1 hour
         s += time / 3600 + ":";
         time %= 3600;
      }

      s += String.format("%02d:%02d", time / 60, time % 60);

      return s;
   }

   public void set_state(String state) {
      p_label_state.setText(state);
   }

   public void set_info(String text) {
      p_label_info.setText(text);
   }

   public void toggle_oval() {
      p_panel_board.toggle_oval();
   }

   public void reverse_board() {
      p_panel_board.reverse_board();
      update_clocks();
   }

   private void update_clocks() {

      JLabel clock_white = p_panel_board.reverse() ? p_label_clock_north : p_label_clock_south;
      JLabel clock_black = p_panel_board.reverse() ? p_label_clock_south : p_label_clock_north;

      clock_white.setText(p_clock[Side.White]);
      clock_black.setText(p_clock[Side.Black]);

      if (p_panel_board.turn() == Side.White) {
         label_reverse(clock_white);
         label_normal(clock_black);
      } else {
         label_normal(clock_white);
         label_reverse(clock_black);
      }
   }

   private void label_normal(JLabel label) {
      label.setOpaque(false);
      label.setForeground(Color.BLACK);
   }

   private void label_reverse(JLabel label) {
      label.setOpaque(true);
      label.setForeground(Color.WHITE);
      label.setBackground(Color.BLACK);
   }
}

class Panel_Board extends JPanel implements Runnable, MouseListener, KeyListener {

   private static final boolean HD = true;
   private static final boolean Thread = false;

   private static final long serialVersionUID = 0; // who cares?

   private static final int Square_None = -1; // TODO: move to draughts.Square?

   private Pos p_pos;
   private long p_squares;
   private boolean p_redraw;

   private int p_size;
   private boolean p_oval;
   private boolean p_reverse;
   private int p_from;

   public Panel_Board() {

      p_pos = Pos.Start;
      p_squares = 0;
      p_redraw = false;

      p_size = Hub.vars.get_int("gui-square");
      p_oval = Hub.vars.get_bool("gui-oval");
      p_reverse = false;
      p_from = Square_None;

      addMouseListener(this);
      addKeyListener(this);

      setPreferredSize(new Dimension(Square.File_Size * p_size, Square.Rank_Size * p_size));
      setFocusable(true); // for keyboard events #

      if (Thread) util.Thread.launch(this);
   }

   public void run() {

      while (true) {

         while (!p_redraw) {
            util.Thread.sleep(10);
         }

         p_redraw = false;
         repaint();
      }
   }

   public synchronized void set_board(Pos pos, long squares) {
      p_pos = pos;
      p_squares = squares;
      p_redraw = true;
      if (!Thread) repaint();
   }

   public void toggle_oval() {
      p_oval = !p_oval;
      repaint();
   }

   public void reverse_board() {
      p_reverse = !p_reverse;
      repaint();
   }

   public boolean reverse() {
      return p_reverse;
   }

   public int turn() {
      return p_pos.turn();
   }

   public void mouseEntered(MouseEvent e) {

      // TODO: requestFocusInWindow()?
      // or requestFocus()
   }

   public void mouseExited(MouseEvent e) {
   }

   public void mousePressed(MouseEvent e) {

      if (SwingUtilities.isLeftMouseButton(e)) {
         p_from = square(e);
      } else {
         Hub.model_user.unclick();
         p_from = Square_None;
      }
   }

   public void mouseReleased(MouseEvent e) {

      if (SwingUtilities.isLeftMouseButton(e)) {

         int to = square(e);

         if (p_from != Square_None && to != Square_None && to != p_from) {
            Hub.model_user.drag(p_from, to);
         }
      }

      p_from = Square_None;
   }

   public void mouseClicked(MouseEvent e) {

      if (SwingUtilities.isLeftMouseButton(e)) {
         int sq = square(e);
         if (sq != Square_None) Hub.model_user.click(sq);
      }

      p_from = Square_None;
   }

   public void keyPressed(KeyEvent e) {

      int key = e.getKeyCode();
      char c  = e.getKeyChar();

      if (c == '0') {
         Hub.model_user.set_players(0);
      } else if (c == '1') {
         Hub.model_user.set_players(1);
      } else if (c == '2') {
         Hub.model_user.set_players(2);
      } else if (key == KeyEvent.VK_A) {
         Hub.model_user.analyse();
      } else if (key == KeyEvent.VK_G) {
         Hub.model_user.go();
      } else if (key == KeyEvent.VK_L) {
         Hub.model_user.load_game();
      } else if (key == KeyEvent.VK_N) {
         Hub.model_user.new_game();
      } else if (key == KeyEvent.VK_O) {
         Hub.model_user.toggle_oval();
      } else if (key == KeyEvent.VK_P) {
         String s = get_clipboard();
         if (s != null) Hub.model_user.set_pos(s);
      } else if (key == KeyEvent.VK_R) {
         Hub.model_user.reverse_board();
      } else if (key == KeyEvent.VK_S) {
         Hub.model_user.save_game();
      } else if (key == KeyEvent.VK_ESCAPE) {
         Hub.model_user.move_now();
      } else if (key == KeyEvent.VK_SPACE) {
         Hub.model_user.single_move();
      } else if (key == KeyEvent.VK_LEFT) {
         Hub.model_user.undo();
      } else if (key == KeyEvent.VK_RIGHT) {
         Hub.model_user.redo();
      } else if (key == KeyEvent.VK_UP) {
         Hub.model_user.undo_all();
      } else if (key == KeyEvent.VK_DOWN) {
         Hub.model_user.redo_all();
      }
   }

   private String get_clipboard() {

      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

      Transferable t = clipboard.getContents(null);

      if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {

         try {
            return (String) t.getTransferData(DataFlavor.stringFlavor);
         } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
            return null;
         } catch (IOException e) {
            e.printStackTrace();
            return null;
         }
      }

      return null;
   }

   private void set_clipboard(String s) {

      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

      StringSelection selection = new StringSelection(s);
      clipboard.setContents(selection, null);
   }

   public void keyReleased(KeyEvent e) {
   }

   public void keyTyped(KeyEvent e) {
   }

   public void paintComponent(Graphics g) {

      p_size = Math.min(getWidth() / Square.File_Size, getHeight() / Square.Rank_Size);

      super.paintComponent(g);

      if (HD) {
         Graphics2D g2d = (Graphics2D) g;
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      }

      for (int y = 0; y < Square.Rank_Size; y++) {
         for (int x = 0; x < Square.File_Size; x++) {
            draw_square(g, x, y);
         }
      }
   }

   private void draw_square(Graphics g, int x, int y) {

      if (is_light(x, y)) {

         draw_square(g, x, y, light_colour());

      } else {

         int sq = square(x, y);

         draw_square(g, x, y, Bit.has(p_squares, sq) ? Color.RED : dark_colour());

         switch (p_pos.piece_side(sq)) {
            case White_Man :  draw_man (g, x, y, Color.WHITE, Color.BLACK); break;
            case Black_Man :  draw_man (g, x, y, Color.BLACK, Color.WHITE); break;
            case White_King : draw_king(g, x, y, Color.WHITE, Color.BLACK); break;
            case Black_King : draw_king(g, x, y, Color.BLACK, Color.WHITE); break;
         }
      }
   }

   private void draw_square(Graphics g, int x, int y, Color c) {
      g.setColor(c);
      g.fillRect(x * p_size, y * p_size, p_size, p_size);
   }

   private void draw_man(Graphics g, int x, int y, Color c_in, Color c_out) {

      if (p_oval) {

         draw_oval(g, x, y, 9, 0.92, c_out);
         draw_oval(g, x, y, 9, 0.84, c_in);

         draw_oval(g, x, y, 7, 0.92, c_out);
         draw_oval(g, x, y, 7, 0.84, c_in);

      } else {

         draw_disc(g, x, y, 0.92, c_out);
         draw_disc(g, x, y, 0.84, c_in);
      }
   }

   private void draw_king(Graphics g, int x, int y, Color c_in, Color c_out) {

      if (p_oval) {

         draw_oval(g, x, y, 10, 0.92, c_out);
         draw_oval(g, x, y, 10, 0.84, c_in);

         draw_oval(g, x, y,  8, 0.92, c_out);
         draw_oval(g, x, y,  8, 0.84, c_in);

         draw_oval(g, x, y,  6, 0.92, c_out);
         draw_oval(g, x, y,  6, 0.84, c_in);

      } else {

         draw_disc(g, x, y, 0.92, c_out);
         draw_disc(g, x, y, 0.84, c_in);
         draw_disc(g, x, y, 0.16, c_out);
         draw_disc(g, x, y, 0.08, c_in);
      }
   }

   private void draw_disc(Graphics g, int x, int y, double d, Color c) {

      int cx = x * p_size + p_size / 2;
      int cy = y * p_size + p_size / 2;

      int r = (int) (p_size * d / 2.0);

      g.setColor(c);
      g.fillOval(cx - r, cy - r, r * 2, r * 2);
   }

   private void draw_oval(Graphics g, int x, int y, int dy, double d, Color c) {

      int cx = x * p_size + p_size / 2;
      int cy = y * p_size + dy * p_size / 16;

      int r = (int) ((double) p_size * d / 2.0);

      g.setColor(c);
      g.fillOval(cx - r, cy - r * 3 / 4, r * 2, r * 3 / 2);
   }

   private Color light_colour() {
      // return new Color(200, 200, 100);
      return new Color(220, 190, 110);
   }

   private Color dark_colour() {
      // return new Color(100, 140, 100);
      return new Color(175, 130,  75);
   }

   private int square(MouseEvent e) {

      int x = e.getX() / p_size;
      int y = e.getY() / p_size;

      return Square.is_valid(x, y) ? square(x, y) : Square_None;
   }

   private int square(int x, int y) {
      int sq = Square.make(x, y);
      return p_reverse ? Square.opp(sq) : sq;
   }

   static private boolean is_light(int x, int y) {
      return Square.is_light(x, y);
   }

   static private boolean is_dark(int x, int y) {
      return Square.is_dark(x, y);
   }
}

