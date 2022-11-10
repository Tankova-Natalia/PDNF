/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdnf;

import static java.lang.Integer.parseInt;
import java.util.Arrays;

public class Individual implements Comparable<Individual> {

    int len;
    byte[] genes;
    int fitness;
    int solution;
    int pow;
    String str;

    public Individual(String str, int solution) {
        this.len = str.length();
        this.str = str;
        this.pow = Individual.pow(str);
        this.solution = solution;
        genes = new byte[len];
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == '-') {
                genes[i] = (byte) Math.round(Math.random());
            } else {
                genes[i] = (byte) (str.charAt(i) - 48);
            }
        }
        fitness = 0;
        String bin;
        for (int i = 0; i < len; i++) {
            if (genes[i] == 1) {
                bin = String.format("%0" + pow + "d", Integer.parseInt(Integer.toBinaryString(i)));
                for (int j = 0; j < bin.length(); j++) {
                    if (bin.charAt(j) == '0') {
                        fitness++;
                    }
                }
            }
        }
    }

    Individual getMax() {
        Individual maxInd = new Individual(this.str, this.solution);
        for (int i = 0; i < this.len; i++) {
            if (this.str.charAt(i) == '-') {
                maxInd.genes[i] = (byte) 1;
            } else {
                genes[i] = (byte) (str.charAt(i) - 48);
            }
        }
        maxInd.checkFitness();
        return maxInd;
    }

    static int pow(String str) {
        int len = str.length();
        int pow = 0;
        while (len != 1) {
            len /= 2;
            pow++;
        }
        if (Math.pow(2, pow) == str.length()) {
            return pow;
        } else {
            return -1;
        }
    }

    void checkFitness() {
        fitness = 0;
        String bin;
        for (int i = 0; i < len; i++) {
            if (genes[i] == 1) {
                bin = String.format("%0" + this.pow + "d", Integer.parseInt(Integer.toBinaryString(i)));
                for (int j = 0; j < bin.length(); j++) {
                    if (bin.charAt(j) == '0') {
                        fitness++;
                    }
                }
            }
        }
    }

    byte getGene(int index) {
        return this.genes[index];
    }

    void setGene(int index, byte value) {
        this.genes[index] = value;
    }

    void inversion(int index) {
        if (this.getGene(index) == 0) {
            this.setGene(index, (byte) 1);
        } else {
            this.setGene(index, (byte) 0);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Individual other = (Individual) obj;
        if (!Arrays.equals(this.genes, other.genes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(genes);
    }

    @Override
    public int compareTo(Individual o1) {
        int a1 = Math.abs(this.fitness - solution);
        int a2 = Math.abs(o1.fitness - solution);
        return a1 - a2;
    }

    void print() {
        int len = this.len;
        pow = this.pow;
        String bin;
        int ed = 0;
        for (int i = 0; i < len; i++) {
            if (genes[i] == 1) {
                ed++;
            }
        }
        System.out.print("(");
        for (int i = 0; i < len; i++) {
            if (genes[i] == 1) {
                bin = String.format("%0" + this.pow + "d", Integer.parseInt(Integer.toBinaryString(i)));
                for (int j = 0; j < pow; j++) {
                    if (bin.charAt(j) == '0') {
                        if (j != pow - 1) {
                            System.out.print("!a" + j + "∧");
                        } else {
                            System.out.print("!a" + j);
                        }
                    } else {
                        if (j != pow - 1) {
                            System.out.print("a" + j + "∧");
                        } else {
                            System.out.print("a" + j);
                        }
                    }
                }
                ed--;
                if (ed != 0) {
                    System.out.print(") ∨ (");
                }
            }
        }
        System.out.print(")");

    }

}
