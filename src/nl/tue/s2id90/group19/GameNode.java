/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.s2id90.group19;
import nl.tue.s2id90.game.*;
import org10x10.dam.game.Move;
import java.util.ArrayList;
/**
 *
 * @author Hidden
 */
public class GameNode {
    private GameState state;
    private int eval_Value;
    private ArrayList<GameNode> successors = new ArrayList<GameNode>();
    private Move bestMove;
    
    public GameNode(GameState state) {
        this.state = state;
    }
    
    public void clearSuccessors() {
        this.successors.clear();
    }
    
    public void addSuccessor(GameNode successor){
        this.successors.add(successor);
    }

    
    public ArrayList<GameNode> getSuccessors(){
        return successors;
    }

    public void setGameState(GameState state) {
        this.state = state;
    }
    
    /**
     * The returned GameState is not always the real GameState for this node
     * because in the AlphaBeta-Search the State is NOT cloned for every 
     * new node.
     * @return The GameState associated with this node 
     */
    public GameState getGameState() {
        return state;
    }
    
    public Move getBestMove() {
        return bestMove;
    }
    
    public void setBestMove(Move bestMove) {
        this.bestMove = bestMove;
    }
    
    public void setEval(int eval) {
        this.eval_Value = eval;
    }
    
    public int getEval(){
        return eval_Value;
    }    
    
}
