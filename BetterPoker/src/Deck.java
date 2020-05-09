import java.util.Arrays;

public class Deck{
    private static final Card[] cards = new Card[52];
    public Deck(){
        for(int i = 0; i < cards.length; i++){
            String currRank = Constants.ranks[i % 13];
            String currSuit = Constants.suits[i / 13];
            String mid1 = "|" + currRank + "    |";
            String mid3 = "|    " + currRank + "|";
            if((i % 13) == 9){
                mid1 = mid1.substring(0, mid1.length() - 2) + "|";
                mid3 = "|" + mid3.substring(2);
            }
            String[] prints = {" ----- ", mid1, "|  " + currSuit + "  |", mid3, " ----- "};
            cards[i] = new Card(currRank, currSuit, Constants.values[i % 13], prints);
        }
        shuffle();
    }

    private void shuffle() {
        for(int i = 0; i < Constants.timesToShuffle; i++){
            for(int j = cards.length - 1; j >= 0; j--){
                int ranCard = (int) (Math.random() * j);
                Card temp = cards[ranCard];
                cards[ranCard] = cards[j];
                cards[j] = temp;
            }
        }
    }

    public static Card[] playerCard(int playerNum){
        return Arrays.copyOfRange(cards, (2 * playerNum - 2), (2 * playerNum));
    }

    public static Card[] tableCard(){
        int offset = (Constants.numOfPlayers * 2);
        return Arrays.copyOfRange(cards, offset, offset + 5);
    }

    public static Card[] playerAndTable(int playerNum){
        Card[] player = playerCard(playerNum);
        Card[] table = tableCard();
        return new Card[]{player[0], player[1], table[0], table[1], table[2], table[3], table[4]};
    }

    static void printCard(Card[] cards){
        String[][] card = new String[cards.length][5];
        for(int i = 0; i < cards.length; i++){
            card[i] = cards[i].Prints();
        }
        for(int i = 0; i < card[0].length; i++){
            for (String[] aCard : card)
                System.out.print(aCard[i] + "\t    ");
            System.out.println();
        }
    }
}