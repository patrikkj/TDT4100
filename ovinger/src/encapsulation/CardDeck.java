package encapsulation;

import java.util.*;

public class CardDeck {
	//Declarations
	private ArrayList<Card> cardArrList = new ArrayList<Card>();
	private static final char[] VALID_SUITS = { 'S', 'H', 'D', 'C' };
	
	
	//Constructor
	public CardDeck(int n) throws IllegalArgumentException {
		if (n > 13  ||  n < 1) 
			throw new IllegalArgumentException("Invalid card count.");
		
		for (int i = 0; i != 4*n; i++) 
			cardArrList.add(new Card(validSuits[Math.floorDiv(i, n)], i % n + 1));
	}
	
	
	//Getters
	public int getCardCount() { return cardArrList.size(); }
	
	public Card getCard(int n) { 
		if (n >= cardArrList.size()) 
			throw new IllegalArgumentException("Card does not exist.");
		
		return cardArrList.get(n);
	}
	
	
	//Actions
	public void shufflePerfectly() {
		int splitIndex = cardArrList.size() / 2;
		
		ArrayList<Card> tempArrList = new ArrayList<Card>();
		List<Card> cardList1 = cardArrList.subList(0, splitIndex);
		List<Card> cardList2 = cardArrList.subList(splitIndex, cardArrList.size());

		for (int i = 0; i < splitIndex; i++) {
			tempArrList.add(cardList1.get(i));
			tempArrList.add(cardList2.get(i));
		}

		cardArrList = tempArrList;
	}
	
	
	//Other
	@Override
	public String toString() {
		String outputStr = "";
		
		for (int i = 0; i < cardArrList.size(); i++) {
			outputStr += cardArrList.get(i).toString() + "\t";

			if ((i + 1) % (cardArrList.size() / 4) == 0)
				outputStr += "\n";
		}
		
		return outputStr;
	}
	
	public void printDeck() {
		for (int i = 0; i < cardArrList.size(); i++) 
			System.out.println(cardArrList.get(i));
	}
	
	public static void main(String[] args) {
		CardDeck deck1 = new CardDeck(10);
		deck1.printDeck();
		
	}
}



