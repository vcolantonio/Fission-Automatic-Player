package collection.tree.filter;

import utils.Move;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * La classe definisce una gerarchia di filtri.
 * La gerarchia viene passata al costruttore come array ordinato di filtri.
 * Viene applicato prima il filtro in indice 0, poi indice 1, etc.
 * Il metodo testAll prova ad applicare tutti i filtri: se uno di essi, nell'ordine specificato
 * non dà risultati, viene restituito l'ultimo risultato valido.
 * Sarà possibile ottenere i filtri applicati realmente con il metodo getSuccessfulFilters.
 */

public final class FilterHierarchy {

    private Predicate<Move>[] filters;

    private int lastSuccessfulFilter = -1;

    public FilterHierarchy(Predicate<Move>... filters) {
        this.filters = filters;
    }

    /**
     * filtra tutte le mosse in input
     *
     * @param moves collezione di mosse da filtrare
     * @return mosse filtrate. Restituirà sempre qualcosa, a patto che moves non sia empty
     */

    public Collection<Move> filterAll(Collection<Move> moves) {
        Collection<Move> temp = moves;

        for (int i = 0; i < filters.length; i++) {
            moves = moves.stream().filter(filters[i]).collect(Collectors.toList());
            if (moves.isEmpty())
                break;
            else {
                lastSuccessfulFilter = i;
                temp = moves;
            }
        }

        return temp;
    }

    /**
     * permette di ottenere i filtri applicati con successo nell'ultima esecuzione di filterAll,
     * ordinati nello stesso ordine del costruttore.
     *
     * @return
     */

    public Predicate<Move>[] getSuccessfulFilters() {
        Predicate<Move>[] appliedFilters = new Predicate[lastSuccessfulFilter + 1];
        for (int i = 0; i <= lastSuccessfulFilter; i++) {
            appliedFilters[i] = filters[i];
        }

        return appliedFilters;
    }

}
