/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdnf;

import java.awt.*;
import java.io.*;
import java.lang.ClassNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class PDNF {

    private static String str;
    private static int solution;
    private static int gen;
    private static int popSize;
    private static int breed;
    public static float crosProb;
    public static float mutationProb;

    public static void main(String[] args) throws IOException {
        List<Object> ratings = new ArrayList<Object>();
        FileReader fr = new FileReader("textIn.txt");
        BufferedReader reader = new BufferedReader(fr);
        String line = null;
        while ((line = reader.readLine()) != null) {
            for (String s : line.split(" ")) {
                ratings.add(parse(s));
            }
        }

        str = (String) ratings.get(0);
        String s1 = (String) ratings.get(1);
        solution = Integer.parseInt(s1.trim());
        ratings.remove(0);
        ratings.remove(0);
        FileReader fr2 = new FileReader("textIn2.txt");
        BufferedReader reader2 = new BufferedReader(fr2);
        line = null;
        while ((line = reader2.readLine()) != null) {
            for (String s : line.split(" ")) {
                ratings.add(parse(s));
            }
        }
        s1 = (String) ratings.get(0);
        gen = Integer.parseInt(s1.trim());
        s1 = (String) ratings.get(1);
        popSize = Integer.parseInt(s1.trim());
        s1 = (String) ratings.get(2);
        breed = Integer.parseInt(s1.trim());
        s1 = (String) ratings.get(3);
        crosProb = Float.parseFloat(s1.trim());
        s1 = (String) ratings.get(4);
        mutationProb = Float.parseFloat(s1.trim());
        reader.close();
        GeneticAlgorithm.setCrosProb(crosProb);
        GeneticAlgorithm.setMutationProb(mutationProb);
        Population myPop = new Population(popSize, str, breed, solution, true);
        int j = 0, x, y;
        StdDraw.setCanvasSize(700, 700);
        StdDraw.setYscale(0, myPop.individuals.get(0).getMax().fitness);
        StdDraw.setXscale(0, gen);
        StdDraw.line(0, solution, gen, solution);
        StdDraw.line(0, 0, 0, myPop.individuals.get(0).getMax().fitness);
        StdDraw.line(0, 0, gen, 0);
        if (myPop.individuals.get(0).getMax().fitness < 1000) {
            for (int i = 0; i <= gen; i += 10) {
                StdDraw.text(i, -10, Integer.toString(i));
            }
            for (int i = 0; i <= myPop.individuals.get(0).getMax().fitness; i += 10) {
                StdDraw.text(-2.5, i, Integer.toString(i));
            }
        } else {
            for (int i = 0; i <= gen; i += 100) {
                StdDraw.text(i, -10, Integer.toString(i));
            }
            for (int i = 0; i <= myPop.individuals.get(0).getMax().fitness; i += 100) {
                StdDraw.text(-2.5, i, Integer.toString(i));
            }
        }
        StdDraw.setPenColor(Color.RED);
        StdDraw.setPenRadius(0.007);
        while (myPop.getBest().fitness != solution && j != gen) {
            GeneticAlgorithm.evolve(myPop);
            StdDraw.point(j, myPop.getBest().fitness);
            j++;
        }
        System.out.println("Количество циклов: " + j);
        Individual best = myPop.getBest();
        System.out.println("Доопределение: " + best);
        System.out.println("Количество отрицаний: " + best.fitness);
        System.out.print("СДНФ: ");
        best.print();
    }

    static Object parse(String s) {
        try {
            return String.valueOf(s);
        } catch (Exception ignored) {
        }

        try {
            return Integer.valueOf(s);
        } catch (Exception ignored) {
        }

        try {
            return Double.valueOf(s);
        } catch (Exception ignored) {
        }

        return Boolean.valueOf(s);
    }

}
