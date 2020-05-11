import java.util.Scanner;
import java.util.regex.Pattern;

class BetLogic {
    static double[] Betting(double[] playersBet, double[] prevBets, double[] balance){

        int playersIn = Constants.numOfPlayers;
        boolean[] playerFold = new boolean[Constants.numOfPlayers];

        for (int i = 0; i < prevBets.length; i++) {
            if (prevBets[i] < 0) {
                playerFold[i] = true;
                playersIn--;
            }
        }
        raiseBet(true, -1, playersBet, playerFold, playersIn, balance);

        return playersBet;
    }

    private static void raiseBet(boolean first, double highestBid, double[] playersBet, boolean[] playerFold, int playersIn, double[] balance){
        boolean[] isPlayerIn = new boolean[Constants.numOfPlayers];
        boolean allIn = false;
        for(int i = 0; i < Constants.numOfPlayers; i++)
            isPlayerIn[i] = playersBet[i] == highestBid || playersBet[i] == balance[i];
        for(boolean bool : isPlayerIn)
            allIn = bool || allIn;
        if(first || !allIn){
            for (int j = 0; j < playersBet.length; j++) {
                if(playersBet[j] < 0 && !playerFold[j]) {
                    playerFold[j] = true;
                    playersIn--;
                }
                boolean betRange = playersBet[j] < highestBid || playersBet[j] < balance[j];
                if (betRange && !playerFold[j]) {
                    playersBet[j] = betting(j);
                    if(balance[j] < playersBet[j]){
                        System.out.println("Please try again");
                        j--;
                    }else if(playersBet[j] > highestBid) {
                        highestBid = playersBet[j];
                    }
                }
            }
            if(playersIn >= 0)
                raiseBet(false, highestBid, playersBet, playerFold, playersIn, balance);
        }
    }

    private static double betting(int player){
        Scanner betAmount = new Scanner(System.in);
        System.out.println("How much do you want to bet player " + (player + 1) + "?");
        String bets = betAmount.next();
        if (!Pattern.matches("[a-zA-Z]+", bets) && Pattern.matches("[0-9]+", bets) && bets.length() < 6)
            return Math.abs(Double.parseDouble(bets));
        return -1;
    }
}