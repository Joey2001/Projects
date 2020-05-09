import java.util.ArrayList;
import java.util.Arrays;

public class Compare {

    private static final Card[][] sorted = Sort();

    private static double[] highestCard(){
        double[] highestCard = new double[Constants.numOfPlayers];
        for(int i = 0; i < Constants.numOfPlayers; i++){
            Card[] player = Deck.playerCard(i + 1);
            Arrays.sort(player);
            highestCard[i] = player[1].Value() + (((double)player[0].Value()) / 10);
        }
        return highestCard;
    }

    private static int[][] findSets(){
        int[][] sets = new int[Constants.numOfPlayers][5];
        for(int i = 0; i < Constants.numOfPlayers; i++){
            ArrayList<Integer> temp = new ArrayList<>();
            for(int j = 0; j < sorted[i].length - 1; j++){
                if(sorted[i][j].Value() == sorted[i][j + 1].Value()) {
                    temp.add(sorted[i][j].Value());
                }
            }
            int len = temp.size();

            if(len == 3){
                if(temp.get(0).equals(temp.get(1))){
                    sets[i][3] = temp.get(0);
                    sets[i][0] = temp.get(2);
                }else if(temp.get(1).equals(temp.get(2))){
                    sets[i][3] = temp.get(1);
                    sets[i][0] = temp.get(0);
                }else{
                    sets[i][2] = temp.get(0) + temp.get(1) + temp.get(2);
                }
            }else if(len == 2){
                if(temp.get(0).equals(temp.get(1))){
                    sets[i][3] = temp.get(0);
                }else{
                    sets[i][1] = temp.get(0) + temp.get(1);
                }
            }else if(len == 1){
                sets[i][0] = temp.get(0);
            }
        }
        return sets;
    }

    private static int[] straight(){
        int[] special = new int[Constants.numOfPlayers];
        int[] straight = new int[Constants.numOfPlayers];
        for(int i = 0; i < Constants.numOfPlayers; i++){
            for(int j = 1; j < sorted[i].length - 1; j++){
                if(sorted[i][j - 1].Value() + 1 != sorted[i][j].Value()) {
                    special[i] = 0;
                }else{
                    special[i]++;
                    if(special[i] >= 4)
                        straight[i] = sorted[i][j].Value();
                }
            }
        }
        for(int k = 0; k < Constants.numOfPlayers; k++){
            boolean aTwo = sorted[k][0].Value() == 2;
            boolean anAce = sorted[k][sorted[k].length - 1].Value() == 14;
            if(special[k] == 3 && aTwo && anAce){
                straight[k] = sorted[k][3].Value();
            }
        }
        return straight;
    }

    private static boolean[] flush(){
        boolean[] flush = new boolean[Constants.numOfPlayers];
        int[] special = straight();
        int index = 0;
        for(int val : special){
            flush[index] = val != 0;
            index++;
        }
        Arrays.fill(special, 0);
        for(int i = 0; i < Constants.numOfPlayers; i++){
            if(flush[i]){
                for(int j = 1; j < sorted[i].length - 1; j++){
                    if(sorted[i][j - 1].Suit().equals(sorted[i][j].Suit())) {
                        special[i] = 0;
                    }else{
                        special[i]++;
                    }
                }
            }
            flush[i] = special[i] == 4;
        }
        return flush;
    }

    private static boolean[] royalFlush(){
        boolean[] royal = flush();
        for(int i = 0; i < Constants.numOfPlayers; i++){
            if(!(royal[i] && sorted[i][2].Value() == 10)){
                royal[i] = false;
            }
        }
        return royal;
    }

    static int winner(){
        double[] points = new double[Constants.numOfPlayers];
        double[] highCard = highestCard();
        int[][] sets = findSets();
        int[] straight = straight();
        boolean[] flush = flush();
        boolean[] royalFlush = royalFlush();

        for(int i = 0; i < Constants.numOfPlayers; i++){
            points[i] = highCard[i] / 1000;
            if(royalFlush[i]){
                points[i] += 80000000;
                return i + 1;
            }else if(straight[i] != -1 && flush[i]){
                points[i] += ((double) straight[i]) * 6000000;
            }else if(sets[i][4] != 0){
                points[i] += ((double)(sets[i][1])) * 500000;
            }else if(sets[i][1] != 0 && sets[i][3] != 0){
                points[i] += ((double)(sets[i][1] + sets[i][3])) * 40000;
            }else if(flush[i]){
                points[i] += ((double) straight[i]) * 3000;
            }else if(straight[i] != 0){
                points[i] += ((double) straight[i]) * 200;
            }else if(sets[i][3] != 0){
                points[i] += ((double) sets[i][3]) * 10;
            }else if(sets[i][2] != 0){
                points[i] += sets[i][2];
            }else if(sets[i][1] != 0){
                points[i] += ((double) sets[i][1]) / 10;
            }else if(sets[i][0] != 0){
                points[i] += ((double)sets[i][0]) / 100;
            }
        }
        double mostPoints = 0;

        for(double pts : points)
            mostPoints = Math.max(pts, mostPoints);

        for(int i = 0; i < points.length; i++) {
            if (points[i] == mostPoints)
                return i + 1;
        }
        return -1;
    }

    private static Card[][] Sort(){
        Card[][] allPlayTable = new Card[Constants.numOfPlayers][7];
        for(int i = 0; i < Constants.numOfPlayers; i++){
            allPlayTable[i] = Deck.playerAndTable(i + 1);
            Arrays.sort(allPlayTable[i]);
        }
        return allPlayTable;
    }
}