package collection.tree.minmax;

import collection.searchstruct.SearchStructure;
import collection.searchstruct.impl.StackLL;
import board.Grid;
import collection.tree.Node;
import collection.tree.minmax.impl.MinNode;
import utils.Move;
import utils.enums.Color;
import utils.enums.TypeMove;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class MinMaxNode extends Node<Grid> {

    protected static final Predicate<Node<Grid>> moveFilterEqMove = x -> {
        if(x.getAction().getTypeMove().equals(TypeMove.EQ_MOVE) && !x.getAction().isCollision()){
            Grid parent = x.getParent().getConfiguration();
            Move move = x.getAction();

            if(correttezzaIntorno(parent, move.getX1(), move.getY1(), move.getX2(), move.getY2(), parent.getTurn())) return true;

            else return false;
        }
        return true;
    };

    /**
     * dovrebbe stabilire se espandibile o meno
     */
    
    protected boolean filterChanged = true;

    protected boolean expandableChanged = true;

    protected double value;

    public MinMaxNode(Grid conf, Move action, MinMaxNode parent) {
        this.configuration = conf;
        this.action = action;
        this.parent = parent;
    }

    public MinMaxNode(Grid conf, Move action) {
        this.configuration = conf;
        this.action = action;
    }

    /**
     * Anche questo metodo dovrebbe rendere flessibile la generazione di figli
     * ad esempio ordinandoli e prendendone al più un tot. oppure eliminando alcune mosse a priori.
     * Va specificato nelle classi concrete
     *
     * @return
     */

    protected abstract List<Node<Grid>> generateChildren();

    /**
     * Se si usa un nodo nell'algoritmo MinMax del libro per incapsulare l'informazione
     * (stato, azione), il metodo getChildrenAsSearchStructure permette di ottenere subito i figli (e quindi le azioni)
     *
     * @return
     */

    public List<Node<Grid>> getChildren(){
        if (filterChanged || expandableChanged || children == null) {
            children = generateChildren();

            filterChanged = false;
            expandableChanged = false;
        }

        return children;
    }

    public SearchStructure<Node<Grid>> getChildrenAsSearchStructure() {
       // childrenSS = new OrderedStack<>(getChildren(), (this instanceof MinNode));
        childrenSS = new StackLL<>(getChildren());

        return childrenSS;
    }

    /*+
     * Si usa per verificare se un nodo MinMax è un nodo foglia
     *
     */
    public boolean isTerminal() {
        return this.configuration.isTerminal();
    }

    @Override
    public String toString() {
        return "MinMaxNode{" +
                "configuration=" + configuration +
                ", action=" + action +
                '}';
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public int compareTo(Node<Grid> o) {
        TypeMove thisTM = this.getAction().getTypeMove();
        TypeMove oTM = o.getAction().getTypeMove();

        if(thisTM.equals(oTM)) {
            switch (this.getAction().getTypeMove()){
                case EQ_MOVE: {

                    //Consistente rispetto al giocatore
                    Comparator<Move> conf = Move::compareTo;

                    if(o instanceof MinNode)
                        conf = (x,y) -> y.compareTo(x);

                    Optional<Move> moves1 = this.getConfiguration().getAvailableMoves()
                            .stream()
                            .filter(m -> m.getTypeMove().equals(TypeMove.POS_MOVE))
                            .sorted(conf)
                            .findFirst();

                    Optional<Move> moves2 = o.getConfiguration().getAvailableMoves()
                            .stream()
                            .filter(m -> m.getTypeMove().equals(TypeMove.POS_MOVE))
                            .sorted(conf)
                            .findFirst();

                    if (!moves1.isPresent() && !moves2.isPresent()) return 0;
                    if (!moves1.isPresent() && moves2.isPresent()) return 1;
                    if (moves1.isPresent() && !moves2.isPresent()) return -1;

                    int best1 = moves1.get().getEliminatedPawnsB() - moves1.get().getEliminatedPawnsW();
                    int best2 = moves2.get().getEliminatedPawnsB() - moves2.get().getEliminatedPawnsW();

                    if (best1 < best2) return 1;
                    else if (best1 == best2) {
                        /*
                        if(this.getAction().isCollision() && this.getAction().isCollision()) return 0;
                        else if(this.getAction().isCollision()) return 1;
                        else if(o.getAction().isCollision()) return -1;

                        else

                         */
                        return 0;
                    }
                    return -1;
                }
                case POS_MOVE:{
                    return this.getAction().compareTo(o.getAction());
                }
                case NEG_MOVE:{
                    return  Math.negateExact(this.getAction().compareTo(o.getAction()));
                }
            }
        }
        if (thisTM.equals(TypeMove.POS_MOVE) && (oTM.equals(TypeMove.NEG_MOVE) || oTM.equals(TypeMove.EQ_MOVE)))
            return 1;
        else if (thisTM.equals(TypeMove.NEG_MOVE) && (oTM.equals(TypeMove.EQ_MOVE) || oTM.equals(TypeMove.POS_MOVE)))
            return  -1;
        else if (thisTM.equals(TypeMove.EQ_MOVE) && (oTM.equals(TypeMove.POS_MOVE) || oTM.equals(TypeMove.NEG_MOVE)))
            if (oTM.equals(TypeMove.NEG_MOVE)) return   1;
            else return  -1; //preferisco la vittoria al pareggio

        return 0;

    }

    protected static boolean correttezzaIntorno(Grid parent, int x1, int y1, int x2, int y2, Color turn) {

        // <-
        if(x2-1>=0){
            if(x2 -1 != x1 && parent.get(x2-1, y2) == (byte) turn.ordinal()) return false;
        }
        // ->
        if((x2+1)<=7){
            if(x2 +1 != x1 && parent.get(x2+1, y2) == (byte) turn.ordinal())  return false;
        }
        // Sud
        if ((y2+1)<=7){
            if(y2 +1 != y1 && parent.get(x2, y2+1) == (byte) turn.ordinal())  return false;
        }
        // Nord
        if ((y2-1)>=0){
            if(y2 -1 != y1 && parent.get(x2, y2-1) == (byte) turn.ordinal())  return false;
        }

        return true;
    }


}

