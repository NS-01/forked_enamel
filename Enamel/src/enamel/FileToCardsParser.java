package enamel;

import java.util.*;

import java.io.*;

/**
 *
 * @author Jeremy, Nisha, Tyler
 *
 *         Class to parse card data from a file. New Updates.
 *
 */
public class FileToCardsParser {

	private Scanner fileScanner;
	private ArrayList<Card> cards;
	private String scenarioFilePath;
	private String initialPrompt;
	private String endingPrompt;
	private int numButtons;
	private int numCells;
	private String title;
	private int numLines;
	private int start;
	private boolean inButton;
	private String fileLine;
	private int cardNum;
	private int buttonNum;
	private int currLineNum;
	private Card currCard;
	private ArrayList<DataButton> buttons;
	private ArrayList<BrailleCell> cells;
	private DataButton currButton;
	private BrailleCell currCell;

	public FileToCardsParser() {
		cards = new ArrayList<Card>();
		this.initialPrompt = "";
		this.endingPrompt = "";
	}

	public void setFile(String scenarioFile) {
		try {
			File f = new File(scenarioFile);
			fileScanner = new Scanner(f);
			String absolutePath = f.getAbsolutePath();
			scenarioFilePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
			checkNumLines(scenarioFile);
			checkButtonsAndCells();
			parse();
			print();
		} catch (Exception e) {
			System.out.println("Incorrect File Name");
		}

	}

	public int getNumLines() {
		return this.numLines;
	}

	/**
	 * checks the number of lines in the file. This was needed for adding the last
	 * card
	 * 
	 * @param scenarioFile
	 */
	public void checkNumLines(String scenarioFile) {
		try {
			File f = new File(scenarioFile);
			Scanner numLineChecker = new Scanner(f);
			String fileLine;
			Boolean initialFound = false;
			while (numLineChecker.hasNextLine()) {
				fileLine = numLineChecker.nextLine();
				numLines++;
			}
			numLineChecker.close();
			System.out.println(numLines + "Num Lines");
		} catch (Exception e) {
			System.out.println("Something went wrong");
		}

	}

	/**
	 * This reads the number of buttons and cells and the title
	 */
	public void checkButtonsAndCells() {
		if (fileScanner == null) {
			try {
				File f = new File("./FactoryScenarios/test.txt");
				fileScanner = new Scanner(f);
			} catch (Exception e) {
				System.out.println("Incorrect File Name");
			}

		}
		// Checks num cells
		String fileLine = fileScanner.nextLine();
		if (fileLine.length() >= 6 && fileLine.substring(0, 4).equals("Cell")) {
			if (Character.isDigit(fileLine.charAt(5))) {
				numCells = Character.getNumericValue(fileLine.charAt(5));
			} else {
				throw new IllegalArgumentException();
			}

		} else {
			throw new IllegalArgumentException();
		}
		// Checks num buttons
		fileLine = fileScanner.nextLine();
		if (fileLine.length() >= 8 && fileLine.substring(0, 6).equals("Button")) {
			if (Character.isDigit(fileLine.charAt(7))) {
				numButtons = Character.getNumericValue(fileLine.charAt(7));
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalArgumentException();
		}
		// Checks title
//		fileLine = fileScanner.nextLine();
		// if (fileLine.length() >= 1) {
		title = initialPrompt;
		// } else {
		// throw new IllegalArgumentException();
		// }
	}

	/**
	 * This goes through the whole file and parses it
	 */
	public void parse() {
		setUp();
		while (fileScanner.hasNextLine()) {

			currLineNum++;
			fileLine = fileScanner.nextLine();
			if (fileLine.replace(" ", "").equals(""))
				continue;
			if (fileLine.length() >= 2 && fileLine.substring(0, 2).equals("/~")) {
				checkCommands();
			} else {

				if (inButton) {
					currButton.addText(fileLine);
				} else {
					currCard.addText(fileLine);
				}
			}
			if (currLineNum == numLines-1){
				if (currCard.getText() != null || currButton.getText() != "") {
					// buttons.clear();
					if (cells.isEmpty()) {
						cells.add(new BrailleCell());
					}
					buttons.add(new DataButton(currButton));
					nextCard();
				}
			}
		}
		print();

	}

	/**
	 * Checks which command the current line is and does the corresponding action
	 */
	private void checkCommands() {
		if (fileLine.length() >= 17 && fileLine.substring(0, 17).equals("/~disp-cell-pins:")) {
			dispCellPins();
		} else if (fileLine.length() >= 8 && fileLine.substring(0, 8).equals("/~sound:")) {
			// Still unclear how we are doing sound
			if (inButton) {
				currButton.setAudio(
						scenarioFilePath + File.separator + "AudioFiles" + File.separator + fileLine.substring(8));
				currButton.addText("/Play sound file " + fileLine.substring(8));
			} else {
				currCard.setSound(
						scenarioFilePath + File.separator + "AudioFiles" + File.separator + fileLine.substring(8));
				currCard.addText("/Play sound file " + fileLine.substring(8));
			}
		} else if (fileLine.equals("/~user-input")) {
			inButton = true;
		} else if (fileLine.equals("/~NEXTT") || fileLine.equals("/~reset-buttons")) {
			buttons.add(new DataButton(currButton));
			inButton = false;
			nextCard();
			// <<<<<<< HEAD
			// } else if(fileLine.equals("/~pause:")){
			// insertPause();
			// =======
		} else if (fileLine.length() >= 8 && fileLine.substring(0, 8).equals("/~pause:")) {
			if (inButton) {
				currButton.addText("/Wait for " + Character.getNumericValue(fileLine.charAt(8)) + " second(s)");
			} else {
				currCard.addText("/Wait for " + Character.getNumericValue(fileLine.charAt(8)) + " second(s)");
			}
		} else if (fileLine.length() >= 14 && fileLine.substring(0, 14).equals("/~disp-string:")) {if (inButton) {
				currButton.addText("/Display string " + fileLine.substring(14));
			} else {
				currCard.addText("/Display string " + fileLine.substring(14));
			}
		} else if (fileLine.length() >= 17 && fileLine.substring(0, 17).equals("/~disp-cell-char:")) {
			if (!inButton) {
				currCell = new BrailleCell();
				try {
					String[] param = fileLine.substring(17).split("\\s");
					int paramIndex = Integer.parseInt(param[0]);
					char dispChar = param[1].charAt(0);
					System.out.println(paramIndex + ", " + dispChar);
					if (paramIndex > numCells - 1 || paramIndex < 0 || param[1].length() > 1) {
						System.out.println("Incorrect format of /~disp-cell-char");
					} else {
						currCell.displayCharacter(dispChar);
						try {
							cells.set(paramIndex, currCell);
						} catch (Exception e) {
							while (cells.size() < Character.getNumericValue(fileLine.charAt(17))) {
								cells.add(new BrailleCell());
							}
							cells.add(currCell);
						}
						currCard.addText("/Display character " + dispChar + " on cell " + (paramIndex + 1));
					}

				} catch (InterruptedException e1) {

				}

			} else {
				String[] param = fileLine.substring(17).split("\\s");
				int paramIndex = Integer.parseInt(param[0]);
				char dispChar = param[1].charAt(0);
				System.out.println(paramIndex + ", " + dispChar);
				if (paramIndex > numCells - 1 || paramIndex < 0 || param[1].length() > 1) {
					System.out.println("Incorrect format of /~disp-cell-char");
				} else {
					currButton.addText("/Display character " + dispChar + " on cell " + (paramIndex + 1));
				}
			}
			// if (inButton) {
			// currButton.addText("/Display character " + fileLine.charAt(17) );
			// } else {
			// currCard.addText("/Display character " + fileLine.charAt(17) );
			// }
		} else if (fileLine.equals("/~disp-clearAll")) {
			if (inButton) {
				currButton.addText("/Clear all pins");
			} else {
				currCard.addText("/Clear all pins");
			}
			// >>>>>>> branch 'TestingUpdates' of https://github.com/NS-01/forked_enamel
		}

		checkButtons();
		

	}

	/**
	 * sets the cell pins depending on if they are in the button or not
	 */
	private void dispCellPins() {
		if (!inButton) {
			currCell = new BrailleCell();
			currCell.setPins(fileLine.substring(19));
			try {
				cells.set(Character.getNumericValue(fileLine.charAt(17)), currCell);
			} catch (Exception e) {
				while (cells.size() < Character.getNumericValue(fileLine.charAt(17))) {
					cells.add(new BrailleCell());
				}
				cells.add(currCell);
			}
			currCard.addText(
					"/Pins on " + (Character.getNumericValue(fileLine.charAt(17)) + 1) + ": " + fileLine.substring(19));
		} else {
			currButton.addText("\n/Pins on " + (Character.getNumericValue(fileLine.charAt(17)) + 1) + ": "
					+ fileLine.substring(19));
		}
	}

	/**
	 * checks if the current line is one to start the buttons
	 */
	private void checkButtons() {
		if (fileLine.equals("/~ONEE")) {
			buttonNum = 1;
			currButton = new DataButton(buttonNum);
		} else if (fileLine.equals("/~TWOO")) {
			buttonNum = 2;
			buttons.add(new DataButton(currButton));
			currButton = new DataButton(buttonNum);
		} else if (fileLine.equals("/~THREEE")) {
			buttonNum = 3;
			buttons.add(new DataButton(currButton));
			currButton = new DataButton(buttonNum);
		} else if (fileLine.equals("/~FOURR")) {
			buttonNum = 4;
			buttons.add(new DataButton(currButton));
			currButton = new DataButton(buttonNum);
		} else if (fileLine.equals("/~FIVEE")) {
			buttonNum = 5;
			buttons.add(new DataButton(currButton));
			currButton = new DataButton(buttonNum);
		} else if (fileLine.equals("/~SIXX")) {
			buttonNum = 6;
			buttons.add(new DataButton(currButton));
			currButton = new DataButton(buttonNum);
		}
	}

	/**
	 * This saves the data for the card and sets the current card to a new card
	 */
	private void nextCard() {
		currCard.setBList(new ArrayList<DataButton>(buttons));
		buttons.clear();
		currCard.setCells(new ArrayList<BrailleCell>(cells));
		cells.clear();
		cards.add(currCard);
		cardNum++;
		currCard = new Card(cardNum - 1, "Card " + cardNum, "notSure", true);
		buttonNum = 1;
		currButton = new DataButton(buttonNum);
	}

	/**
	 * This is a setup which needs to be done first
	 */
	private void setUp() {
		inButton = false;
		cardNum = 1;
		buttonNum = 1;
		currLineNum = 2;
		// while (currLineNum < start - 1 && fileScanner.hasNextLine()) {
		//
		// this.initialPrompt += fileScanner.nextLine();
		// currLineNum++;
		//
		// }
		currCard = new Card(cardNum - 1, "Card " + cardNum, "notSure", true);
		buttons = new ArrayList<DataButton>(numButtons);
		cells = new ArrayList<BrailleCell>(numCells);
		title = this.initialPrompt;
		currButton = new DataButton(buttonNum);
		currCell = new BrailleCell();
	}

	public int getCells() {
		return this.numCells;
	}

	public int getButtons() {
		return this.numButtons;
	}

	public ArrayList<Card> getCards() {
		return this.cards;
	}


	/**
	 * This method just prints out the information stored in the cards It is just
	 * for debugging purposes
	 */
	public void print() {
		System.out.println(cards.size());
		for (int i = 0; i < cards.size(); i++) {
			System.out.println("In card " + i + ":\n" + cards.get(i).getText() + "\n\n");
			ArrayList<DataButton> buttonList = cards.get(i).getButtonList();
			if (buttonList.size() > 0) {
				for (int j = 0; j < buttonList.size(); j++) {
					System.out.println("In Button" + j + ":" + buttonList.get(j).getID() + "\n"
							+ buttonList.get(j).getText() + "\n");
				}
			}

			System.out.println("\n\n\n");
		}
		for (int i = 0; i < cards.size(); i++) {
			System.out.println(i);
			for (int j = 0; j < 8; j++) {

				if (!cards.get(i).getCells().isEmpty()) {

					System.out.print(cards.get(i).getCells().get(0).getPinState(j) ? "1" : "0");
				}

			}
			System.out.println();
		}
	}

}