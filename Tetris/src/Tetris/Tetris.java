package Tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

class Tetris extends JComponentWithEvents
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 662996480156109617L;

	// Creating pointer for global variables
	private static int cols, rows; // Creating columns and rows variable to keep track of the numbers of cells the "Width and Length" of the board
	private static Color[][] board; // Creating a 2 dimensional color object array pointer for the board
	// Creating graphics related global pointers
	private int boardMargin;
	private int gameInfoMargin; // The width of the menu that display the next piece, the hold piece, the score and the level
	private int cellSize; // The size of a block (pixel) that makes up a Tetris piece
	private int miniCellSize; // The size of a block (pixel) that makes up a Tetris in the information menu
	private int cellMargin; // The size of the line (pixel) that separates each Tetris piece
	private int boardWidth; // The total width of the game in pixel
	private int boardHeight; // The total height of the game in pixel
	private static Color emptyColor; // The color of the board when it's empty
	private static Color backGroundColor; // The color under the board
	private static Color cellBoarderColor; // The color of the line that separates the block that makes up a Tetris piece
	private static Color shadowColor; // The color of the shadow that shows where the piece is going to land if it keeps falling down
	private static Color textColor; // The color of the text
	// Creating game-play related global variables
	private final int DEFAULT_HEART_BEAT_MS = 5; // The interval (in milliseconds) that timerFired() method is being called
	private final short[] SCORE_REQUIRED = { 5, 10, 15, 20, 25, 30, 45, 60, 75, 85, 90, 100 }; // The score in each level require to level up
	private final short[] FALLING_RATE_PER_HEART_BEAT = { 75, 70, 65, 60, 50, 35, 25, 20, 10, 5, 2, 1 }; // The number of interval (in HEART_BEAT) that the Tetris piece is going to move down
	private final short TIME_TOLERANCE_HEART_BEAT = 45; // The time that the user can still manipulate the Tetris piece after the piece had touched the ground/piece under it
	private short heartBeatCount; // The heart beat counter that keeps tracts of the number heart beat for the Tetris piece to move down
	private short placePieceHeartBeatCount; // A second heart beat counter that keep tract of the time that the user is still able to manipulate the Tetris piece after it touched the ground/piece under it
	private short lvlGoal; // The number of rows that the user needs to eliminate in order to proceed to the next level
	private short gameLvl; // The current level
	private boolean gamePaused; // Boolean to track if the game is paused or not
	private boolean gameOver; // Boolean to track if the game is over
	private boolean swapped; // Boolean to track if the user had swapped his falling piece
	private int gameScore; // Tracking the overall score
	// Creating Tetris piece related global pointers
	private boolean[][] fallingPiece;
	private boolean[][] nextFallingPiece;
	private boolean[][] swapPiece;
	private Color fallingPieceColor;
	private Color nextPieceColor;
	private int fallingPieceRowPosition;
	private int fallingPieceColPosition;
	private int shadowRowPosition;
	private int fallingPieceRows;
	private int fallingPieceCols;
	private int pieceIndex;
	private int nextPieceIndex;
	private int swapPieceIndex;
	private boolean fallingPieceReachedBottom;
	private Random random = new Random();

	// 2-Dimensional boolean arrays that stores the shape of the Tetris pieces
	private static final boolean[][] I_PIECE = { { true, true, true, true } };
	private static final boolean[][] J_PIECE = { { true, false, false }, { true, true, true } };
	private static final boolean[][] L_PIECE = { { false, false, true }, { true, true, true } };
	private static final boolean[][] O_PIECE = { { true, true }, { true, true } };
	private static final boolean[][] S_PIECE = { { false, true, true }, { true, true, false } };
	private static final boolean[][] T_PIECE = { { false, true, false }, { true, true, true } };
	private static final boolean[][] Z_PIECE = { { true, true, false }, { false, true, true } };
	// Compiling all the 2-Dimensional array into 1 array in order to randomly generate
	private static boolean[][][] TETRIS_PIECES = { I_PIECE, J_PIECE, L_PIECE, O_PIECE, S_PIECE, T_PIECE, Z_PIECE };
	// Colors for the Tetris pieces, the index number are correspondent to the TETRIS_PIECES array. Thus giving each piece a distinct color
	private static Color TETRIS_PIECE_COLORS[];

	static
	{
		TETRIS_PIECE_COLORS = (new Color[] { Color.red, Color.yellow, Color.magenta, Color.blue, Color.cyan,
				Color.green, Color.orange });
	}

	public static void main(String[] args)
	{
		launch();
	}

	// Constructor
	public Tetris(int setRows, int setCols)
	{
		rows = setRows;
		cols = setCols;
		board = new Color[setRows][setCols]; // Initializing the board
		cellSize = 20;
		miniCellSize = cellSize - 5;
		cellMargin = 2;
		boardMargin = 10;
		gameInfoMargin = miniCellSize * 5;
		boardWidth = 2 * boardMargin + cols * cellSize + gameInfoMargin;
		boardHeight = 2 * boardMargin + rows * cellSize;
		setPreferredSize(new Dimension(boardWidth, boardHeight));

		backGroundColor = Color.gray;
		emptyColor = Color.darkGray;
		cellBoarderColor = Color.black;
		shadowColor = Color.lightGray;
		textColor = Color.lightGray;

		resetGame();
	}

	// Overloading constructor for default size (24 * 12) in Tetris blocks
	public Tetris()
	{
		this(24, 12); // Calling the original constructor with the default size
	}

	private void resetGame()
	{
		stopSounds(); // Stop any sound that might be playing
		for (int rowCounter = 0; rowCounter < board.length; rowCounter++)
		{
			for (int colCounter = 0; colCounter < board[rowCounter].length; colCounter++)
			{
				board[rowCounter][colCounter] = emptyColor; // Iterates each blocks in the board, setting them to the empty color
			}
		}
		gameOver = false; // The game resets therefore it's not over
		swapped = false; // The user had not swap his/her's falling piece yet because the game just started/restarted
		swapPiece = null; // Setting the swapPiece pointer to null
		nextPieceIndex = random.nextInt(TETRIS_PIECES.length); // Generate the next falling Piece that will appear right after the user had placed the first one
		gameLvl = 1; // Setting the level to LVL_1
		lvlGoal = SCORE_REQUIRED[gameLvl - 1]; // Setting the number of rows that you need to eliminate in order to progress into the next level
		heartBeatCount = 0;
		newFallingPiece(); // Generates a new Tetris piece
		fallingPieceReachedBottom = false;
		loop("ThemeDubstep.wav"); // Start the music
		gameScore = 0; // Clear the score
	}

	private void newFallingPiece() // Generates a new falling piece
	{
		pieceIndex = nextPieceIndex;
		fallingPiece = TETRIS_PIECES[pieceIndex];
		fallingPieceColor = TETRIS_PIECE_COLORS[pieceIndex];
		fallingPieceRows = fallingPiece.length;
		fallingPieceCols = fallingPiece[0].length;
		fallingPieceRowPosition = 0;
		fallingPieceColPosition = cols / 2 - fallingPieceCols / 2;
		nextPieceIndex = random.nextInt(TETRIS_PIECES.length);
		nextFallingPiece = TETRIS_PIECES[nextPieceIndex];
		nextPieceColor = TETRIS_PIECE_COLORS[nextPieceIndex];
		placeFallingPieceShadow();
	}

	public void paint(Graphics2D game) // The main display method
	{
		game.setColor(backGroundColor);
		game.fillRect(0, 0, getWidth(), getHeight());
		paintBoard(game);
		paintFallingPieceShadow(game);
		paintFallingPiece(game);
		paintNextFallingPiece(game);
		paintSwappedPiece(game);
		displayString(game);
	}

	private void paintBoard(Graphics2D graphics) // Iterates through each cell in board array, passing each element into paintCell
	{
		for (int rowCounter = 0; rowCounter < rows; rowCounter++)
		{
			for (int colCounter = 0; colCounter < cols; colCounter++)
			{
				paintCell(graphics, rowCounter, colCounter, board[rowCounter][colCounter]);
			}
		}
	}

	private void paintFallingPiece(Graphics2D graphics) // Iterates through each element in the fallingPiece pointer, if the element returns true, also pass it to paintCell
	{
		if (fallingPieceIsLegal())
		{
			for (int rowCounter = 0; rowCounter < fallingPieceRows; rowCounter++)
			{
				for (int colCounter = 0; colCounter < fallingPieceCols; colCounter++)
				{
					if (fallingPiece[rowCounter][colCounter] == true)
					{
						paintCell(graphics, rowCounter + fallingPieceRowPosition, colCounter + fallingPieceColPosition,
								fallingPieceColor);
					}
				}
			}
		}
	}

	private void paintFallingPieceShadow(Graphics2D graphics)
	{
		if (fallingPieceIsLegal())
		{
			for (int rowCounter = 0; rowCounter < fallingPieceRows; rowCounter++)
			{
				for (int colCounter = 0; colCounter < fallingPieceCols; colCounter++)
				{
					if (fallingPiece[rowCounter][colCounter] == true)
					{
						paintCell(graphics, rowCounter + shadowRowPosition, colCounter + fallingPieceColPosition,
								shadowColor);
					}
				}
			}
		}
	}

	private void paintNextFallingPiece(Graphics2D graphics)
	{
		for (int rowCounter = 0; rowCounter < nextFallingPiece.length; rowCounter++)
		{
			for (int colCounter = 0; colCounter < nextFallingPiece[rowCounter].length; colCounter++)
			{
				if (nextFallingPiece[rowCounter][colCounter])
				{
					paintNextFallingPieceCell(graphics, rowCounter, colCounter, nextPieceColor);
				}
			}
		}
	}

	private void paintSwappedPiece(Graphics2D graphics)
	{
		if (swapPiece == null)
		{
			return;
		}
		for (int rowCounter = 0; rowCounter < swapPiece.length; rowCounter++)
		{
			for (int colCounter = 0; colCounter < swapPiece[rowCounter].length; colCounter++)
			{
				if (swapPiece[rowCounter][colCounter])
				{
					paintSwapPieceCell(graphics, rowCounter, colCounter, TETRIS_PIECE_COLORS[swapPieceIndex]);
				}
			}
		}
	}

	private void paintCell(Graphics2D graphics, int row, int col, Color cellColor)
	{
		if (cellColor == emptyColor)
		{
			int cellLeft = boardMargin + col * cellSize;
			int cellTop = boardMargin + row * cellSize;
			int innerSize = cellSize;
			graphics.setColor(cellColor);
			graphics.fillRect(cellLeft, cellTop, innerSize, innerSize);
		} else if (cellColor == shadowColor)
		{
			int cellLeft = boardMargin + col * cellSize;
			int cellTop = boardMargin + row * cellSize;
			int innerSize = cellSize;
			graphics.setColor(cellColor);
			graphics.fillRect(cellLeft, cellTop, innerSize, innerSize);
		} else
		{
			int cellLeft = boardMargin + col * cellSize;
			int cellTop = boardMargin + row * cellSize;
			int innerSize = cellSize;
			graphics.setColor(cellBoarderColor);
			graphics.fillRect(cellLeft, cellTop, innerSize, innerSize);
			graphics.setColor(cellColor);
			innerSize -= cellMargin * 2;
			cellLeft += cellMargin;
			cellTop += cellMargin;
			graphics.fillRect(cellLeft, cellTop, innerSize, innerSize);
		}
	}

	private void paintNextFallingPieceCell(Graphics2D graphics, int row, int col, Color cellColor)
	{
		int cellLeft = boardMargin * 2 + cols * cellSize + col * miniCellSize;
		int cellTop = cellSize * 3 + row * miniCellSize;
		int innerSize = miniCellSize;
		graphics.setColor(cellBoarderColor);
		graphics.fillRect(cellLeft, cellTop, innerSize, innerSize);
		graphics.setColor(cellColor);
		innerSize -= cellMargin * 2;
		cellLeft += cellMargin;
		cellTop += cellMargin;
		graphics.fillRect(cellLeft, cellTop, innerSize, innerSize);
	}

	private void paintSwapPieceCell(Graphics2D graphics, int row, int col, Color cellColor)
	{
		int cellLeft = boardMargin * 2 + cols * cellSize + col * miniCellSize;
		int cellTop = cellSize * 7 + row * miniCellSize;
		int innerSize = miniCellSize;
		graphics.setColor(cellBoarderColor);
		graphics.fillRect(cellLeft, cellTop, innerSize, innerSize);
		graphics.setColor(cellColor);
		innerSize -= cellMargin * 2;
		cellLeft += cellMargin;
		cellTop += cellMargin;
		graphics.fillRect(cellLeft, cellTop, innerSize, innerSize);
	}

	private void displayString(Graphics2D graphics)
	{
		if (gamePaused)
		{
			displayPausedString(graphics);
		}
		if (gameOver)
		{
			displayGameOverString(graphics);
		}
		displayMisString(graphics);
	}

	private void displayPausedString(Graphics2D graphics)
	{
		graphics.setColor(textColor);
		graphics.setFont(new Font("Arial", Font.BOLD, 16));
		graphics.drawString("Game paused", (int) (boardWidth / 3.5), (int) (boardHeight / 15));
	}

	private void displayGameOverString(Graphics2D graphics)
	{
		String display = "Game Over! Your Score: " + gameScore;
		graphics.setColor(textColor);
		graphics.setFont(new Font("Arial", Font.BOLD, 16));
		graphics.drawString(display, (int) (boardWidth / 2 - 100), (int) (boardHeight / 5));
	}

	private void displayMisString(Graphics2D graphics)
	{
		String scoreString = "Score: " + gameScore;
		String lvlString = "Level: " + gameLvl;
		String lvlGoalString = "Goal: " + lvlGoal;
		graphics.setColor(textColor);
		graphics.setFont(new Font("Arial", Font.BOLD, 16));
		graphics.drawString("Next:", (cellSize * cols + boardMargin * 2), (cellSize * 2));
		graphics.drawString("Hold:", (cellSize * cols + boardMargin * 2), (cellSize * 6));
		graphics.drawString(lvlString, (cellSize * cols + boardMargin * 2), (cellSize * 13));
		graphics.drawString(lvlGoalString, (cellSize * cols + boardMargin * 2), (cellSize * 14));
		int scoreStringFont = 16;
		if (gameScore >= 100)
		{
			scoreStringFont = 12;
		}
		graphics.setFont(new Font("Arial", Font.BOLD, scoreStringFont));
		graphics.drawString(scoreString, (cellSize * cols + boardMargin * 2), (cellSize * 12));
	}

	private boolean moveFallingPiece(int moveRow, int moveCol)
	{
		fallingPieceRowPosition += moveRow;
		fallingPieceColPosition += moveCol;
		if (!fallingPieceIsLegal())
		{
			fallingPieceRowPosition -= moveRow;
			fallingPieceColPosition -= moveCol;
			placeFallingPieceShadow();
			return false;
		}
		placeFallingPieceShadow();
		return true;
	}

	private boolean moveFallingPieceShadow()
	{
		boolean moved = true;
		shadowRowPosition++;
		while (!fallingPieceShadowIsLegal())
		{
			shadowRowPosition--;
			if (shadowRowPosition == 0)
			{
				break;
			}
			moved = false;
		}
		return moved;
	}

	private void moveFallingPieceToShadow()
	{
		fallingPieceRowPosition = shadowRowPosition;
		placeFallingPiece();
		newFallingPiece();
	}

	private boolean fallingPieceIsLegal()
	{
		for (int rowCounter = 0; rowCounter < fallingPieceRows; rowCounter++)
		{
			for (int colCounter = 0; colCounter < fallingPieceCols; colCounter++)
			{
				if (rowCounter + fallingPieceRowPosition < 0 || rowCounter + fallingPieceRowPosition >= rows
						|| colCounter + fallingPieceColPosition < 0 || colCounter + fallingPieceColPosition >= cols
						|| fallingPiece[rowCounter][colCounter]
								&& board[rowCounter + fallingPieceRowPosition][colCounter
										+ fallingPieceColPosition] != emptyColor)
				{
					return false;
				}
			}

		}
		return true;
	}

	private boolean fallingPieceShadowIsLegal()
	{
		for (int rowCounter = 0; rowCounter < fallingPieceRows; rowCounter++)
		{
			for (int colCounter = 0; colCounter < fallingPieceCols; colCounter++)
			{
				if (rowCounter + shadowRowPosition < 0 || rowCounter + shadowRowPosition >= rows
						|| shadowRowPosition < fallingPieceRowPosition
						|| fallingPiece[rowCounter][colCounter] && board[rowCounter + shadowRowPosition][colCounter
								+ fallingPieceColPosition] != emptyColor)
				{
					return false;
				}
			}

		}
		return true;
	}

	private void rotateFallingPiece()
	{
		int oldFallingPieceRows = fallingPieceRows;
		int oldFallingPieceCols = fallingPieceCols;
		int oldRowPosition = fallingPieceRowPosition;
		int oldColPosition = fallingPieceColPosition;
		boolean[][] rotatedFallingPiece = rotateCW(fallingPiece);
		boolean[][] notValidPlaceHolder = fallingPiece;
		fallingPiece = rotatedFallingPiece;
		fallingPieceRows = fallingPiece.length;
		fallingPieceCols = fallingPiece[0].length;
		fallingPieceRowPosition -= (fallingPieceRows - oldFallingPieceRows) / 2;
		fallingPieceColPosition -= (fallingPieceCols - oldFallingPieceCols) / 2;
		if (fallingPieceIsLegal())
		{
			placeFallingPieceShadow();
			return;
		}
		fallingPieceColPosition -= fallingPieceCols / 2;
		if (fallingPieceIsLegal())
		{
			placeFallingPieceShadow();
			return;
		}
		fallingPieceRowPosition -= fallingPieceRows / 2;
		fallingPieceColPosition += fallingPieceCols / 2;
		if (!fallingPieceIsLegal())
		{
			fallingPiece = notValidPlaceHolder;
			fallingPieceRows = oldFallingPieceRows;
			fallingPieceCols = oldFallingPieceCols;
			fallingPieceRowPosition = oldRowPosition;
			fallingPieceColPosition = oldColPosition;
		}
		placeFallingPieceShadow();
	}

	private boolean[][] rotateCW(boolean[][] arr)
	{
		boolean[][] newArr = new boolean[arr[0].length][arr.length];
		for (int targetRowcounter = 0; targetRowcounter < arr[0].length; targetRowcounter++)
		{
			for (int rowCounter = 0; rowCounter < arr.length; rowCounter++)
			{
				newArr[newArr.length - 1 - targetRowcounter][rowCounter] = arr[rowCounter][targetRowcounter];
			}
		}

		return newArr;
	}

	private void placeFallingPiece()
	{
		for (int row = 0; row < fallingPieceRows; row++)
		{
			for (int col = 0; col < fallingPieceCols; col++)
			{
				if (fallingPiece[row][col])
				{
					board[row + fallingPieceRowPosition][col + fallingPieceColPosition] = fallingPieceColor;
				}
			}
		}
		removeRows();
		swapped = false;
	}

	private void placeFallingPieceShadow()
	{
		shadowRowPosition = fallingPieceRowPosition;
		for (int rowCounter = fallingPieceRowPosition; rowCounter < rows; rowCounter++)
		{
			if (!moveFallingPieceShadow())
			{
				break;
			}
		}
	}

	private void swapFallingPiece()
	{
		if (swapped)
		{
			return;
		}
		if (swapPiece == null)
		{
			swapPiece = fallingPiece;
			swapPieceIndex = pieceIndex;
			newFallingPiece();
			swapped = true;
			return;
		} else
		{
			boolean[][] swapPiecePlaceHolder = fallingPiece;
			fallingPiece = swapPiece;
			swapPiece = swapPiecePlaceHolder;
			int swapIndexPlaceHolder = pieceIndex;
			pieceIndex = swapPieceIndex;
			swapPieceIndex = swapIndexPlaceHolder;
			fallingPieceColor = TETRIS_PIECE_COLORS[pieceIndex];
			fallingPieceRows = fallingPiece.length;
			fallingPieceCols = fallingPiece[0].length;
			fallingPieceRowPosition = 0;
			fallingPieceColPosition = cols / 2 - fallingPieceCols / 2;
			swapped = true;
			placeFallingPieceShadow();
		}
	}

	private void removeRows()
	{
		for (int rowCounter = 0; rowCounter < rows; rowCounter++)
		{
			boolean check = true;
			for (int colCounter = 0; colCounter < cols; colCounter++)
			{
				if (board[rowCounter][colCounter] == emptyColor)
				{
					check = false;
				}
			}
			if (check == true)
			{
				shiftRow(rowCounter);
				gameScore++;
				lvlGoal--;
				lvlManage();
			}
		}
	}

	private void shiftRow(int row)
	{
		for (int rowCounter = row; rowCounter > 0; rowCounter--)
		{
			Color[] shift = new Color[cols];
			if (rowCounter == 1)
			{
				for (int counter = 0; counter < shift.length; counter++)
				{
					shift[counter] = emptyColor;
				}
			} else
			{
				shift = board[rowCounter - 1];
			}
			board[rowCounter] = shift;
		}
	}

	public void keyPressed(char key)
	{
		if (key == 'r')
		{
			resetGame();
			return;
		}
		if (gameOver)
		{
			return;
		}
		if (key == 'p')
		{
			gamePaused = !gamePaused;
			return;
		}
		if (gamePaused == false)
		{
			switch (key)
			{
			case 37:
				moveFallingPiece(0, -1);
				placePieceHeartBeatCount = 0;
				break;
			case 38:
				rotateFallingPiece();
				placePieceHeartBeatCount = 0;
				break;
			case 39:
				moveFallingPiece(0, 1);
				placePieceHeartBeatCount = 0;
				break;
			case 40:
				moveFallingPiece(1, 0);
				placePieceHeartBeatCount = 0;
				break;
			case 'q':
				gameOver = true;
				break;
			case ' ':
				moveFallingPieceToShadow();
				break;
			case 'z':
				swapFallingPiece();
				break;
			}
		}
	}

	private void lvlManage()
	{
		if (lvlGoal <= 0)
		{
			gameLvl++;
			if (gameLvl < SCORE_REQUIRED.length)
			{
				lvlGoal = SCORE_REQUIRED[gameLvl - 1];
			} else
			{
				lvlGoal = 100;
			}
		}
	}

	public void timerFired()
	{
		heartBeatCount++;
		placePieceHeartBeatCount++;
		if (gamePaused)
		{
			return;
		}
		if (!fallingPieceIsLegal())
		{
			gameOver = true;
		}
		if (!gameOver)
		{
			if (placePieceHeartBeatCount % TIME_TOLERANCE_HEART_BEAT == 0 && fallingPieceReachedBottom)
			{
				fallingPieceReachedBottom = !moveFallingPiece(1, 0);
				if (fallingPieceReachedBottom)
				{
					placeFallingPiece();
					newFallingPiece();
					fallingPieceReachedBottom = false;
				}
				placePieceHeartBeatCount = 0;
			}
			if (heartBeatCount % getFallingRateHB() == 0)
			{
				fallingPieceReachedBottom = !moveFallingPiece(1, 0);
			}
		}
	}

	private int getFallingRateHB()
	{
		if (gameLvl < FALLING_RATE_PER_HEART_BEAT.length)
		{
			return FALLING_RATE_PER_HEART_BEAT[gameLvl - 1];
		}
		return FALLING_RATE_PER_HEART_BEAT[FALLING_RATE_PER_HEART_BEAT.length - 1];
	}

	public void start()
	{
		setTimerDelay(DEFAULT_HEART_BEAT_MS);
	}
}