public class Main {

    public static void main(String[] args) {
        // write your code here

        //args[0] uctValue

        Tree gameTree = new Tree();
        MonteCarloTreeSearch mcts = new MonteCarloTreeSearch();

        int v1 = Integer.parseInt(args[0]);
        int v2 = Integer.parseInt(args[1]);
        int v3 = Integer.parseInt(args[2]);


        double UctResponse = UCT.uctValue(v1, v2, v3);

        Board board = new Board();
        MonteCarloTreeSearch mcts1 = new MonteCarloTreeSearch();
        mcts1.setLevel(1);
        MonteCarloTreeSearch mcts3 = new MonteCarloTreeSearch();
        mcts3.setLevel(3);

        int player = Board.P1;
        int totalMoves = Integer.parseInt(args[0]);
        for (int i = 0; i < totalMoves; i++) {
            if (player == Board.P1)
                board = mcts3.findNextMove(board, player);
            else
                board = mcts1.findNextMove(board, player);

            if (board.checkStatus() != -1) {
                break;
            }
            player = 3 - player;
        }
        int winStatus = board.checkStatus();
        System.out.println("Win status " + winStatus);
    }
}
