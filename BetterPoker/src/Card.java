public class Card implements Comparable<Card>{

    private final int value;
    private final String suit;
    private final String rank;
    private final String[] prints;

    Card(String rank, String suit, int value, String[] prints){
        this.suit = suit;
        this.rank = rank;
        this.value = value;
        this.prints = prints;
    }

    public int Value(){
        return value;
    }

    public String Suit(){
        return suit;
    }

    public String Rank(){
        return rank;
    }

    public String[] Prints(){
        return prints;
    }

    @Override
    public int compareTo(Card that) {
        return this.value - that.value;
    }

    public String toString(){
        return "Card is a " + Rank() + " of " + Suit();
    }
}
