import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TicTacToeTest {

	FindNextMove nextMove;

	@BeforeEach
	void init() {
		nextMove = new FindNextMove();
	}


	@Test
	void testInitFindNextMove() {
		assertEquals("FindNextMove", nextMove.getClass().getName(), "Incorrectly initialized FindNextMove object");
	}


	@Test
	void testIsThereADrawFalse() {
		assertFalse(nextMove.IsThereDraw("O X b b O X b b O"));
	}

	@Test
	void testIsThereADrawTrue() {
		assertTrue(nextMove.IsThereDraw("X O O O O X X X O"));
	}

	@Test
	void testCheckIfThereIsWinnerForDraw() {
		assertEquals("draw", nextMove.checkIfThereIsWinner("X O O O O X X X O"), "Incorrectly checked for winner");
	}

	@Test
	void testcheckIfThereIsWinnerOnFirstMove() {
		assertEquals("", nextMove.checkIfThereIsWinner("O b b b b b b b b"), "Incorrectly checked for winner");
	}

	@Test
	void testCheckIfThereIsWinnerForPlayer() {
		assertEquals("player", nextMove.checkIfThereIsWinner("O X b b O X b b O"), "Incorrectly checked for winner");
	}

	@Test
	void testCheckIfThereIsWinnerForServer() {
		assertEquals("server", nextMove.checkIfThereIsWinner("X O O O X O b b X"), "Incorrectly checked for winner");
	}

	@Test
	void testCheckIfThereIsWinnerForServerDiagonol() {
		assertEquals("server", nextMove.checkIfThereIsWinner("X O b O X b x O X"), "Incorrectly checked for winner");
	}
}
	