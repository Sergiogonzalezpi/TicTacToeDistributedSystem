package DistributeSystem.ClientTTT;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Client extends JPanel {

    private static final long serialVersionUID = 1L;
    private BufferedImage myTile = null;
    private BufferedImage opponentTile = null;

    JFrame frame;
    JTextArea textArea;
    static final String NEWLINE = System.getProperty("line.separator");

    /**
     * Who goes first in the next game?
     */
    boolean opponentFirst = true;

    // This variable stores all moves of the other player
    int opponentMoves;

    // This variable stores all my moves.
    int myMoves;

    // The squares in order of importance...
    final int bestMoves[] = {4, 0, 2, 6, 8, 1, 3, 5, 7};

    // won contains all possible winning positions
    // won is an boolean array of 2^9 elements = 512 booleans
    boolean won[] = new boolean[1 << 9];
    final int DONE = (1 << 9) - 1; // 511 // every spaces in the grid are used
    final int ONGOING = 0;
    final int WIN = 1;
    final int LOSE = 2;
    final int STALEMATE = 3;

    /**
     * Remote private
     */

    public Client() {
        // Initialize all winning positions in the won array
        this.setWinningPositions();
        // Load the player image tiles
        try {
            myTile = ImageIO.read(new File("resources/circle2.gif"));
            opponentTile = ImageIO.read(new File("resources/cross2.gif"));
            this.addMouseListener(new BoardMouseListener(this));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setWinningPositions() {
        /**
         * Inicializa las posiciones ganadoras en el vector secuenciasGanadoras las
         * posiciones del tablero se numeran del 0 al 8, comenzando por la esquina
         * superior izquierda
         **/
        // fila superior
        this.isWon((1 << 0) | (1 << 1) | (1 << 2));
        // fila central
        this.isWon((1 << 3) | (1 << 4) | (1 << 5));
        // fila inferior
        this.isWon((1 << 6) | (1 << 7) | (1 << 8));
        // columna izda
        this.isWon((1 << 0) | (1 << 3) | (1 << 6));
        // columna central
        this.isWon((1 << 1) | (1 << 4) | (1 << 7));
        // columna derecha
        this.isWon((1 << 2) | (1 << 5) | (1 << 8));
        // diagonal izquierda
        this.isWon((1 << 0) | (1 << 4) | (1 << 8));
        // diagonal derecha
        this.isWon((1 << 2) | (1 << 4) | (1 << 6));
    }

    /**
     * Mark all positions with these bits set as winning. ej. The
     * won array will have true in positions 7, ...
     */

    void isWon(int pos) {
        for (int i = 0; i < DONE; i++) {
            if ((i & pos) == pos) {
                won[i] = true;
            }
        }
    }

    /**
     * Figure what the status of the game is.
     */
    int status() {
        if (won[opponentMoves]) { // has the opponent a winning position?
            return WIN;
        }
        if (won[myMoves]) { // am I the winner?
            return WIN;
        }
        if ((myMoves | opponentMoves) == DONE) { // every spaces in the grid are
            // used
            return STALEMATE;
        }
        return ONGOING;
    }

    /**
     * User move.  # TODO: User Move Server
     *
     * @return true if legal
     */
    boolean setMove(int m) {
        if ((m < 0) || (m > 8)) {
            return false;
        }
        if (((myMoves | opponentMoves) & (1 << m)) != 0) {
            return false;
        }
        // record my new move
        // myMoves |=1 << m is equivalent to myMoves = MyMoves | (1 << m);
        myMoves |= 1 << m;
        return true;
    }

    /**
     * Opponent Move. # TODO: Opponent Move Server
     *
     * @return true if legal
     */
    boolean getMove() {
        // The bitwise | operator performs a bitwise inclusive OR operation.
        if ((myMoves | opponentMoves) == DONE) {
            return false;
        }
        int best = -1; // # TODO: Signal bestMove(opponentMoves, myMoves);
        // record the move of the opponent
        opponentMoves = opponentMoves | (1 << best);
        return true;
    }

    /**
     * Compute the best move for the opponent. (The computer). # TODO: Server Generate Move
     *
     * @return the square to take
     */
    int bestMove(int opponentMoves, int myMoves) {
        int bestmove = -1;

        loop:
        for (int i = 0; i < 9; i++) {
            int mw = bestMoves[i];
            if (((opponentMoves & (1 << mw)) == 0) && ((myMoves & (1 << mw)) == 0)) {
                int pw = opponentMoves | (1 << mw);
                if (won[pw]) {
                    // opponentMoves wins, take it!
                    return mw;
                }
                for (int mb = 0; mb < 9; mb++) {
                    if (((pw & (1 << mb)) == 0) && ((myMoves & (1 << mb)) == 0)) {
                        int pb = myMoves | (1 << mb);
                        if (won[pb]) {
                            // myMoves wins, take another
                            continue loop;
                        }
                    }
                }
                // Neither opponentMoves nor myMoves can win in one move, this
                // will do.
                if (bestmove == -1) {
                    bestmove = mw;
                }
            }
        }
        if (bestmove != -1) {
            return bestmove;
        }

        // No move is totally satisfactory, try the first one that is open
        for (int i = 0; i < 9; i++) {
            int mw = bestMoves[i];
            if (((opponentMoves & (1 << mw)) == 0) && ((myMoves & (1 << mw)) == 0)) {
                return mw;
            }
        }
        // No more moves
        return -1;
    }

    public void moveMade(int move) {
        System.out.println("move made " + move);

        if (status() != ONGOING) { // end of game. Reset to a new game
            opponentMoves = myMoves = 0;
            if (opponentFirst) {
                opponentMoves |= 1 << (int) (Math.random() * 9);
            }
            opponentFirst = !opponentFirst;
            System.out.println("End of game. Reset");
            this.repaint(); // call to the paintComponent to paint a new board

            return;
        }


        if (setMove(move)) { // Paint my move

            this.repaint();

            switch (status()) {
                case WIN:
                    System.out.println(" I win!!!");
                    break;
                case LOSE:
                    System.out.println(" I lose!!!");
                    break;
                case STALEMATE:
                    System.out.println(" stalemate !!!");
                    break;

                default:
                    if (getMove()) { // computer move
                        this.repaint();
                        switch (status()) {
                            case WIN:
                                System.out.println(" Opponent wins!!!");
                                eventOutput("Opponent wins!!");
                                break;
                            case LOSE:
                                System.out.println(" Opponent loses!!!");
                                eventOutput("Opponent loses!!");
                                break;
                            case STALEMATE:
                                System.out.println(" Stalemate !!!");
                                eventOutput(" Stalemate !!");

                                break;
                            default:
                        }
                    } else {
                        System.out.println(" beep !!!");
                    }
            }
        } else {
            System.out.println(" beep !!!");
        }
    }

    public void go() {
        frame = new JFrame("Tic Tac Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //newGameButton = new JButton("New Game");

        textArea = new JTextArea("Status");
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(200, 75));
        frame.add(scrollPane);


        frame.getContentPane().add(BorderLayout.CENTER, this);
        //frame.getContentPane().add(BorderLayout.EAST, newGameButton);
        frame.getContentPane().add(BorderLayout.SOUTH, scrollPane);

        frame.setPreferredSize(new Dimension(450, 450));
        frame.pack(); // to show the preferred size initially
        frame.setVisible(true);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI() {
        Client w = new Client();
        w.go();
    }

    void eventOutput(String eventDescription) {
        textArea.append(eventDescription + NEWLINE);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public static void main(String args[]) throws InterruptedException {
        Client game = new Client();
        // Schedule a job for the event dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                game.createAndShowGUI();
            }
        });
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Dimension d = getSize();
        g.setColor(Color.black);
        int xoff = d.width / 3;
        int yoff = d.height / 3;
        g.drawLine(xoff, 0, xoff, d.height);
        g.drawLine(2 * xoff, 0, 2 * xoff, d.height);
        g.drawLine(0, yoff, d.width, yoff);
        g.drawLine(0, 2 * yoff, d.width, 2 * yoff);

        int i = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++, i++) {
                // left shift operator "<<" shifts a bit pattern
                if ((this.opponentMoves & (1 << i)) != 0) {
                    g.drawImage(myTile, c * xoff + 1, r * yoff + 1, xoff, yoff, null);

                } else if ((this.myMoves & (1 << i)) != 0) {
                    g.drawImage(opponentTile, c * xoff + 1, r * yoff + 1, xoff, yoff, null);
                }
            }
        }
    }

    public void boardMouseActionReleased(MouseEvent e) {

        Dimension d = this.getSize();
        int x = e.getX();
        int y = e.getY();
        // column number = the number of times x contains d.height (0, 1, 2)
        // row number = the number of times y contains d.width (0, 1, 2)
        int c = (x * 3) / d.width;
        int r = (y * 3) / d.height;

        //System.out.println("x " + x + " y " + y);
        //System.out.println("c " + c + " r " + r);

        int move = c + r * 3; // myMove
        this.moveMade(move);
    }
}


class BoardMouseListener implements MouseListener {

    private Client board = null;

    public BoardMouseListener(Client ticTacToeBoard) {
        board = ticTacToeBoard;

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        board.boardMouseActionReleased(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}




