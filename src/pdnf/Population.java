/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdnf;

import java.util.ArrayList;
import java.util.List;

public class Population {

    List<Individual> individuals;
    int size;
    int solution;
    String str;
    int breed;

    public Population(int size, String str, int breed, int solution, boolean createNew) {
        this.breed = breed;
        this.str = str;
        this.solution = solution;
        individuals = new ArrayList();
        this.size = size;
        if (createNew) {
            for (int i = 0; i < size; i++) {
                individuals.add(new Individual(str, solution));
            }
        }
    }

    @Override
    public String toString() {
        return individuals.toString();
    }

    Individual getBest() {
        GeneticAlgorithm.sort(this);
        return this.individuals.get(0);
    }

    void add(Population newPop) {
        this.individuals.addAll(newPop.individuals);
    }
}
