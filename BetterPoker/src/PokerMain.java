import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

public class PokerMain {
    public static void main(String[] args) throws IOException {
        double[] playerCred;
        Scanner scan = new Scanner(System.in);
        boolean first = true;
        String contin = "yes";

        System.out.println("Every player starts out with $" + Constants.bank);
        askNumber();
        playerCred = new double[Constants.numOfPlayers];
        while (contin.equalsIgnoreCase("yes")){
            if(!first){
                System.out.println("Do you want to change the number of players?");
                String changePlayer = scan.next();
                if(changePlayer.equalsIgnoreCase("yes")){
                    askNumber();
                }
            }
            Arrays.fill(playerCred, Constants.bank);
            startGame(true, null, playerCred);
            System.out.println("Do you want to play again?");
            contin = scan.next();
            first = false;
        }
    }

    private static void startGame(boolean keepPlaying, double[] prevBet, double[] playerCred) throws IOException {
        prevBet = play(keepPlaying, prevBet, playerCred);

        for(double aCred : playerCred)
            keepPlaying = aCred > 0;

        if(keepPlaying){
            for(int k = 0; k < playerCred.length; k++)
                System.out.println("Player " + (k + 1) + " has $" + playerCred[k]);

            System.out.println("Press enter to continue");
            System.in.read();
            for(int i = 0; i < Constants.spaces; i++)
                System.out.println();
            System.out.println("Please pass to player one.");
            System.in.read();
            startGame(true, prevBet, playerCred);
        }else{
            System.out.println("Thanks for playing, unfortunately one or more players has run out of money");
        }
    }

    private static double[] play(boolean keepPlaying, double[] prevBet, double[] playerCred) throws IOException {
        new Deck();
        Card[] tableFull = Deck.tableCard();
        Card[] table1 = Arrays.copyOfRange(tableFull, 0, 3);
        Card[] table2 = Arrays.copyOfRange(tableFull, 0, 4);
        Card[][] tableRounds = {table1, table2, tableFull};

        for(int i = 0; i < Constants.numOfPlayers; i++){
            System.out.println("Player " + (i + 1) + " cards.");
            Deck.printCard(Deck.playerCard(i + 1));
            System.in.read();

            for(int j = 0; j < Constants.spaces; j++)
                System.out.println();

            if(i+1 < Constants.numOfPlayers)System.out.println("Please pass to player " + (i + 2));
            System.in.read();
            for(int j = 0; j < Constants.spaces; j++)
                System.out.println();
        }
        int sum = 0;
        prevBet = new double[Constants.numOfPlayers];
        double[] playerBet = new double[Constants.numOfPlayers];
        for(int j = 0; j < Constants.subRounds; j++){
            if(keepPlaying){
                Deck.printCard(tableRounds[j]);
                double[] bets = BetLogic.Betting(playerBet, prevBet, playerCred);
                for(int k = 0; k < bets.length; k++){
                    prevBet[k] = Math.min(bets[k], prevBet[k]);
                    if(bets[k] > 0){
                        sum += bets[k];
                        playerCred[k] -= bets[k];
                    }
                }
                for(double cred : playerCred)
                    keepPlaying = cred > 0;
            }
        }

        int winner = Compare.winner(prevBet);
        for(int i = 1; i <= Constants.numOfPlayers; i++)
            if(i == winner) playerCred[i - 1] += sum;
        for(int j = 0; j < Constants.spaces; j++)
            System.out.println();

        return prevBet;
    }

    private static void askNumber(){
        Scanner scan  = new Scanner(System.in);
        System.out.println("How many players do you want?");
        String numberOfPlayers = scan.next();
        if(!Pattern.matches("[0-9]+", numberOfPlayers)){
            System.out.println("Please enter a number.");
            askNumber();
        }
        Constants.numOfPlayers = Integer.parseInt(numberOfPlayers);
        if(Constants.numOfPlayers <= 1 || Constants.numOfPlayers > 23){
            System.out.println("Number of players out of range. Please try again.");
            askNumber();
        }
    }
}