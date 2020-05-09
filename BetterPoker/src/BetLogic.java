import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

class BetLogic {
    static double[] Betting(double[] prevBets, double[] balance){
        if(prevBets == null){
            prevBets = new double[Constants.numOfPlayers];
            Arrays.fill(prevBets, .0001);
        }
        double highestBid = -1;

        int playersIn = Constants.numOfPlayers;

        double[] playersBet = new double[Constants.numOfPlayers];
        boolean[] playerFold = new boolean[Constants.numOfPlayers];

        for (int i = 0; i < prevBets.length; i++) {
            if (prevBets[i] <= 0) {
                playerFold[i] = true;
                playersIn--;
            }
        }

        raiseBet(true, highestBid, playersBet, playerFold, playersIn, balance);

        return playersBet;
    }

    private static void raiseBet(boolean first, double highestBid, double[] playersBet, boolean[] playerFold, int playersIn, double[] balance){
        boolean allIn = true;
        for(int i = 0; i < Constants.numOfPlayers; i++){
            if(!playerFold[i] && allIn){
                allIn = playersBet[i] == highestBid || playersBet[i] == balance[i];
            }
        }
        if(!allIn || first){
            for (int j = 0; j < playersBet.length; j++) {
                if(playersBet[j] < 0 && !playerFold[j]) {
                    playerFold[j] = true;
                    playersIn--;
                }
                if ((playersBet[j] < highestBid || playersBet[j] < balance[j]) && !playerFold[j]) {
                    playersBet[j] = betting(j);
                    if(balance[j] < playersBet[j]){
                        System.out.println("Please try again");
                        j--;
                    }else if(playersBet[j] > highestBid) {
                        highestBid = playersBet[j];
                    }
                }
            }
            if(playersIn != 0)
                raiseBet(false, highestBid, playersBet, playerFold, playersIn, balance);
        }
    }

    private static double betting(int player){
        Scanner betAmount = new Scanner(System.in);
        System.out.println("How much do you want to bet player " + (player + 1) + "?");
        String bets = betAmount.next();
        if (!Pattern.matches("[0-9]+", bets))
            return -1;
        return Math.abs(Double.parseDouble(bets));
    }
}