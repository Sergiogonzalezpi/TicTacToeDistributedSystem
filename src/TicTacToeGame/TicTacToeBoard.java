package TicTacToeGame;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;


/******************************************
 * TicTacToeBoard JPanel class
 ******************************************/

 class TicTacToeBoard extends JPanel {

	private static final long serialVersionUID = 1L;
	private TicTacToeGame game =null;
	private BufferedImage myTile = null;
	private BufferedImage opponentTile = null;
	static final String NEWLINE = System.getProperty("line.separator");

	
	TicTacToeBoard(TicTacToeGame game) {

		this.game = game;
		try {
			myTile = ImageIO.read(new File("resources/tictactoe/circle2.gif"));
			opponentTile = ImageIO.read(new File("resources/tictactoe/cross2.gif"));
			this.addMouseListener(new BoardMouseListener(this));

		} catch (IOException e) {
			e.printStackTrace();
		}
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
				if ((game.opponentMoves & (1 << i)) != 0) {
					g.drawImage(myTile, c * xoff + 1, r * yoff + 1, xoff, yoff, null);

				} else if ((game.myMoves & (1 << i)) != 0) {
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
		 game.moveMade(move); 
	}
 }

class BoardMouseListener implements MouseListener {

	private TicTacToeBoard board = null;

	public BoardMouseListener(TicTacToeBoard ticTacToeBoard) {
		board  = ticTacToeBoard;
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




