/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.s2id90.group19;
import nl.tue.s2id90.game.*;
import org10x10.dam.game.Move;
import java.util.*;
import nl.tue.s2id90.draughts.DraughtsState;
/**
 *
 * @author Hidden
 */
public class AlphaBetaCopy {
    private class NodeDepthPair {
        public int depth;
        public int eval;
        public Move bestMove;
        public NodeDepthPair(int depth, int eval, Move bestMove){
            this.depth = depth;
            this.eval = eval;
            this.bestMove = bestMove;
        }
    }
    private int maxDepth; // maximum depth for AlphaBeta Algorithm
    private final GameNode root;
    private boolean stopped = false;
    private HashMap<String,NodeDepthPair> transpositionTable = new HashMap();
    /**
     * @param root GameNode where the AlphaBeta search should start
     * @param maxDepth Maximum depth for AlphaBeta Algorithm
     */
    public AlphaBetaCopy(GameNode root, int maxDepth) {
        this.maxDepth = maxDepth;
        this.root = root;
    }
    
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
    
    public void stop() { 
        stopped = true;
    }
    
    private boolean isForcedCapture(DraughtsState s) {
        if (s.getMoves().size() > 0){
            return s.getMoves().get(0).isCapture();
        }
        else {
            return false;
        }
    }
    
    

     
   public int eval_GameState(DraughtsState s) {
        /*int[] pieces = s.getPieces();
        System.out.println("--------------------");
        for (int piece : pieces) {
            System.out.println(piece);
        }
        System.out.println("--------------------");*/
        int[] pieces = s.getPieces();

        boolean turnWhite = s.isWhiteToMove();
        
        if(s.isEndState()) {
            return turnWhite ? -500 : 500;
        }
        final float piece_value = 10; // basic value of a piece
        final float king_value = 3.4f * piece_value; //value of a king
        final float side_weight  = 0.8f; // weight for pieces on the side
        //additional value a piece gets by advancing to the 
        //other side of the board
        final float row_weight = 0.25f * piece_value;
        final float turn_value = 3; //value for the players turn 
        final float move_weight = 0.3f;
        final float back_row_weight = 1.4f;
        
        int blackCount = 0;
        int whiteCount = 0;
        int fieldNumber = 0;
        int rowNumber;
        float value;
        int black_back_count = 4;
        int white_back_count = 4;

        if(turnWhite) {
            //whiteCount += turn_value;
            whiteCount += move_weight * s.getMoves().size();
        }
        else{
            //blackCount += turn_value;
            blackCount += move_weight * s.getMoves().size();
        }
        
        for (int piece : pieces) {
            rowNumber = (int) Math.ceil(fieldNumber / 5.0); //get rowNumber
            boolean sideField = (fieldNumber % 5 <= 1); //field is a side 
            //field if the number of the field is equal to 0 (right side) 
            //or 1 (left side) in modulus 5
            switch(piece) {
                case 1: 
                    //value = Math.max(row_weight*(11-rowNumber), piece_value);
                    value = piece_value;
                    if(rowNumber == 10 && white_back_count > 0) {
                        value = back_row_weight*value;
                        --white_back_count;
                    }
                    else if(rowNumber <= 5) value = (row_weight*(11-rowNumber));
                    value = sideField ? (float) side_weight*value : value;
                    whiteCount += value;
                    break;
                case 2:
                    //value = Math.max(row_weight*rowNumber, piece_value);
                    value = piece_value;
                    if(rowNumber == 1 && black_back_count > 0){ 
                        value = back_row_weight*value;
                        --black_back_count;
                    }
                    else if(rowNumber > 5) value = (row_weight*rowNumber);
                    value = sideField ? (float) side_weight*value : value;
                    blackCount += value;
                    break;
                case 3:
                    value = king_value;
                    value = sideField ? (float) side_weight*value : value;
                    whiteCount += value;
                    break;
                case 4: 
                    value = king_value;
                    value = sideField ? (float) side_weight*value : value;
                    blackCount += value;
                    break;
                default:
                    break;
                          
            }
            fieldNumber++;
        }
        
        
        
        return whiteCount - blackCount;
    }
    public Move AlphaBeta(GameState state) throws AIStoppedException{
       DraughtsState s = (DraughtsState) state;
       GameNode root = new GameNode(s);
       Move bestMove = null;
       if(s.isWhiteToMove()) {
           int best = Integer.MIN_VALUE;
           int temp;
           for (Move move : s.getMoves()) {
               s.doMove(move);
               temp = alphaBetaMin(root, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
               if(temp > best) {
                   bestMove = move;
                   best = temp;
               }
               s.undoMove(move);
           }
       }
       else {
           int best = Integer.MAX_VALUE;
           int temp;
           for (Move move : s.getMoves()) {
               s.doMove(move);
               temp = alphaBetaMax(root, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
               if(temp < best) {
                   bestMove = move;
                   best = temp;
               }
               s.undoMove(move);
           }
       }
       return bestMove;
    }

    public int alphaBetaMax(GameNode node, int alpha, int beta,
            int depth) throws AIStoppedException {
        if(stopped) { throw new AIStoppedException();}
        DraughtsState s = (DraughtsState) node.getGameState();
        //search until Endstate or maxDepth reached with no forced 
        //captures left
        if ( (depth >= maxDepth && !this.isForcedCapture(s)) || 
                s.isEndState()) {
            int eval = eval_GameState(s);
            //System.out.println("alpha:" + eval);
            return eval;
        }
        
        //generating a hash-key to map the nodes in the transpositionTable
        //the "0" indicates it's blacks turn
        String boardHash = s.toString();
        Move priorityMove = null;
        if(transpositionTable.containsKey(boardHash)) {//Node is in Table
            //System.out.println("hit");
            NodeDepthPair ndp = transpositionTable.get(boardHash);
            if(ndp.depth >= this.maxDepth - depth) { //if the depth of the 
                //searchtree for the node in the databse is sufficient the
                //already computed value can be returned
                //System.out.println("hit1");
                return ndp.eval;
            }
            else if (ndp.bestMove != null) {
                priorityMove = ndp.bestMove;
            }
        }
        
        
        List<Move> moves = s.getMoves();
        if (priorityMove != null && moves.size() > 1) {
            // if there exists a bestMove for the current state 
            //but with a lower search depth we prioritize this move to
            //be able to prune as much as possible
            
            moves.remove(priorityMove);
            Move temp = moves.get(0);
            moves.set(0, priorityMove);
            moves.add(temp);
        }
        
        //bestMove is initialized with first List element to 
        //avoid having bestMove == null
        Move bestMove = moves.get(0); //Variable holding the best Move
        int bestAlpha = Integer.MIN_VALUE; //Variable holding the maximum Alpha
        
        for (Move move : moves) {
            s.doMove(move);
            
            int tmp = alphaBetaMin(node, alpha, beta, depth + 1);
            
            if (tmp  > bestAlpha) {
                bestAlpha = tmp;
                bestMove = move;
                
                if (tmp > alpha) {
                alpha = tmp;
            }
            }
            

            s.undoMove(move);

            if (alpha >= beta) {
                transpositionTable.put(boardHash, new NodeDepthPair(
                        this.maxDepth - depth, beta, move));
                return beta;
            }
        }
        if (alpha >= beta) {
                transpositionTable.put(boardHash, new NodeDepthPair(
                        this.maxDepth - depth, alpha, bestMove));
        }
        return alpha;
    }
    
    public int alphaBetaMin(GameNode node, int alpha, int beta,
            int depth) throws AIStoppedException {
        if(stopped) { throw new AIStoppedException();}
        DraughtsState s = (DraughtsState) node.getGameState();
        
        //search until Endstate or maxDepth reached with no forced 
        //captures left
        if ( (depth >= maxDepth && !this.isForcedCapture(s)) || 
                s.isEndState()) {
            int eval = eval_GameState(s);
            //System.out.println("alpha:" + eval);
            return eval;
        }
        
        //generating a hash-key to map the nodes in the transpositionTable
        //the "0" indicates it's blacks turn
        String boardHash = s.toString();
        Move priorityMove = null;
        if(transpositionTable.containsKey(boardHash)) {//Node is in Table
            NodeDepthPair ndp = transpositionTable.get(boardHash);
            //System.out.println("hit");
            if(ndp.depth >= this.maxDepth - depth) { //if the depth of the 
                //searchtree for the node in the databse is sufficient the
                //already computed value can be returned
                //System.out.println("hit1");
                return ndp.eval;
                
            }
            else if (ndp.bestMove != null) {
                priorityMove = ndp.bestMove;
            }
        }
        
        
        List<Move> moves = s.getMoves();
        if (priorityMove != null && moves.size() > 1) {
            // if there exists a bestMove for the current state 
            //but with a lower search depth we prioritize this move to
            //be able to prune as much as possible
            moves.remove(priorityMove);
            Move temp = moves.get(0);
            moves.set(0, priorityMove);
            moves.add(temp);
        }
        //bestMove is initialized with first List element to 
        //avoid having bestMove == null
        Move bestMove = moves.get(0); //Variable holding the best Move
        int bestBeta = Integer.MAX_VALUE; //Variable holding the minimum beta
        
        for (Move move : moves) {
            s.doMove(move);

            int tmp = alphaBetaMax(node, alpha, beta, depth + 1);
            
            if (tmp  < bestBeta) {
                bestBeta = tmp;
                bestMove = move;
                
                if (tmp < beta) {
                beta = tmp;
            }
                
            }
            

            s.undoMove(move);

            if (beta <= alpha) {
                transpositionTable.put(boardHash, new NodeDepthPair(
                        this.maxDepth - depth, alpha, move));
                return alpha;
            }
        }
        transpositionTable.put(boardHash, new NodeDepthPair(
                        this.maxDepth - depth, beta, bestMove));
        return beta;
    }
}
