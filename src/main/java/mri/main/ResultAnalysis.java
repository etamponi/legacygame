package mri.main;

import java.io.*;
import java.util.Arrays;

public class ResultAnalysis {

    public static void main(String... args) throws Exception {
        File folder = new File(".");
        double[][][][][] complete = new double[5][5][3][10][5];
        for (File file: folder.listFiles()) {
            if (file.getName().startsWith("results_Multi")) {
                double[][] values = new double[10][5];

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                int i, j;
                String resultName = line;

                for(i = 1, j = -1, line = reader.readLine(); line != null; i = (i+1) % 20, line = reader.readLine()) {
                    if (line.startsWith("Analysis for")) {
                        i = -1;
                        j++;
                        continue;
                    }
                    if (i % 4 == 0 && !line.isEmpty()) {
                        values[j][i/4] = Double.valueOf(line.split(" ")[1]);
                    }
                }

                //printResult(resultName, values);

                storeResult(complete, resultName, values);

                reader.close();
            }
        }

        for(int k = 0; k < 5; k++) {
            for(int j = 0; j < 3; j++) {
                System.out.print("\\textbf{"+(j+3)+"} ");
                for(int i = 0; i < 5; i++) {
                    printResult("", complete[i][k][j], 4);
                }
                System.out.println(" \\\\");
            }
            System.out.println();
        }

//        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        String s;
//        System.out.print("Insert indices: ");
//
//        while ((s = in.readLine()) != null && s.length() != 0) {
//            int[] is = splitIndices(s);
//            printResult(s, complete[is[0]][is[1]][is[2]]);
//            System.out.print("Insert indices: ");
//        }
    }

    private static int[] splitIndices(String s) {
        int[] ret = new int[3];
        String[] tokens = s.split(" ");
        ret[0] = Integer.valueOf(tokens[0]);
        ret[1] = Integer.valueOf(tokens[1]);
        ret[2] = Integer.valueOf(tokens[2]);
        return ret;
    }

    private static void storeResult(double[][][][][] complete, String resultName, double[][] values) {
        String[] integers = resultName.split("\\D+");
        complete[Integer.parseInt(integers[3])/10 - 1][Integer.parseInt(integers[5])/5 - 2][Integer.parseInt(integers[1])-3] = values;
    }

    private static void printResult(String resultName, double[][] values, int col) {
//        System.out.println(resultName);
//        for(int i = 0; i < values.length; i++) {
//            for(int j = 0; j < values[i].length; j++) {
//                System.out.print(String.format("%8.3f", values[i][j]));
//            }
//            System.out.println();
//        }
//        for(int j = 0; j < values[0].length; j++) {
            int count = 0;
            for(int i = 0; i < values.length; i++) {
                count -= (int)values[i][col];
            }
            System.out.print(String.format(" &%7d", count));
//        }
//        System.out.println();
    }

}
