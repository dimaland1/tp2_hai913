package org.example.graph;

/**
 * Classe générique représentant une paire de valeurs de types potentiellement différents.
 * @param <T> Le type du premier élément de la paire
 * @param <U> Le type du second élément de la paire
 */
public class Pair<T, U> {
    // Les deux valeurs de la paire sont marquées final pour garantir l'immutabilité
    private final T first;   // Premier élément de la paire
    private final U second;  // Second élément de la paire

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        // Vérification si c'est le même objet en mémoire
        if (this == o) return true;
        // Vérification du type de l'objet comparé
        if (o == null || getClass() != o.getClass()) return false;

        // Cast de l'objet en Pair avec des wildcards pour la généricité
        Pair<?, ?> pair = (Pair<?, ?>) o;
        // Comparaison des deux éléments
        return first.equals(pair.first) && second.equals(pair.second);
    }

    @Override
    public int hashCode() {
        return 31 * first.hashCode() + second.hashCode();
    }
}