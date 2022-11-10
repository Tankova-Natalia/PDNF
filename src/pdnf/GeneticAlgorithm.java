/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdnf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GeneticAlgorithm {

    static float crosProb;
    static float mutationProb;

    static void evolve(Population myPop) {
        Population newPop = GeneticAlgorithm.createNewPop(myPop);
        myPop.add(newPop);
        GeneticAlgorithm.mutation(myPop);
        GeneticAlgorithm.sort(myPop);
        GeneticAlgorithm.cut(myPop);
    }

    public static void setCrosProb(float crosProb) {
        GeneticAlgorithm.crosProb = crosProb;
    }

    public static void setMutationProb(float mutationProb) {
        GeneticAlgorithm.mutationProb = mutationProb;
    }

    static Population createNewPop(Population myPop) {
        Individual ind1;
        Individual ind2;
        Individual newInd;
        Population newPop = new Population(myPop.breed, myPop.str, myPop.breed, myPop.solution, false);
        for (int i = 0; i < myPop.breed; i++) {
            ind1 = GeneticAlgorithm.select(myPop);
            do {
                ind2 = GeneticAlgorithm.select(myPop);
            } while (ind2 == ind1);
            newInd = GeneticAlgorithm.crossover(ind1, ind2);
            if (newInd != null && !myPop.individuals.contains(newInd)) {
                newPop.individuals.add(newInd);
            }
        }
        return newPop;
    }

    static Individual select(Population myPop) {
        byte n = (byte) myPop.individuals.size();
        GeneticAlgorithm.sort(myPop);
        int sum = 0, i;
        byte[] ar = new byte[n];
        ar[0] = (byte) (n - 1);
        for (i = 1; i < n; i++) {
            ar[i] = (byte) (ar[i - 1] + n - i);
            sum += i;
        }
        sum += n;
        int index = (int) (Math.random() * sum);
        i = 0;
        while (index > ar[i]) {
            i++;
        }
        return myPop.individuals.get(i);
    }

    static Individual crossover(Individual ind1, Individual ind2) {
        if (Math.random() >= crosProb) {
            int len = ind1.len;
            int line = (int) (Math.random() * (len - 2)) + 1;
            Individual newInd = new Individual(ind1.str, ind1.solution);
            for (int i = 0; i < len; i++) {
                if (i <= line) {
                    newInd.setGene(i, ind1.getGene(i));
                } else {
                    newInd.setGene(i, ind2.getGene(i));
                }
            }
            newInd.checkFitness();
            if (newInd.equals(ind1) || newInd.equals(ind2)) {
                GeneticAlgorithm.mutation(newInd);
            }
            return newInd;
        }
        return null;
    }

    static void mutation(Individual ind) {
        int index;
        do {
            index = (int) (Math.random() * ind.genes.length);
        } while (ind.str.charAt(index) != '-');
        ind.inversion(index);
        ind.checkFitness();

    }

    static void mutation(Population myPop) {
        int index;
        for (Individual i : myPop.individuals) {
            if (Math.random() < mutationProb) {
                do {
                    index = (int) (Math.random() * i.len);
                } while (i.str.charAt(index) != '-');
                i.inversion(index);
                i.checkFitness();
            }
        }
    }

    static void sort(Population myPop) {
        Collections.sort(myPop.individuals);
    }

    static void cut(Population myPop) {
        int size = myPop.size;
        List<Individual> delPop = new ArrayList();
        for (int i = size; i < myPop.individuals.size(); i++) {
            delPop.add(myPop.individuals.get(i));
        }
        myPop.individuals.removeAll(delPop);
    }
}
