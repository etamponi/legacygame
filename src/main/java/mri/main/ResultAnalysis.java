package mri.main;

import game.utils.Utils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;

public class ResultAnalysis {

    private static final int PRECISION = 0;
    private static final int RECALL = 1;
    private static final int ACCURACY = 2;
    private static final int FSCORE = 3;
    private static final int MATTHEWS = 4;

    private static final Object[][] symbols = {
            {"x", "red", 1},
            {"o", "blue", 0},
            {"+", "green", 1},
            {"asterisk", "violet", 1},
            {"star", "cyan", 0},
            {"square", "yellow", 1},
            {"triangle", "black", 0},
            {"diamond", "brown", 0},
            {"pentagon", "orange", 0},
            {"|", "magenta", 1}
    };

    private static class Pairs {
        double[] metrics, mris;
        double pearson, spearman;

        public Pairs(String metricLine, String mriLine) {
            String[] values = metricLine.substring(1, metricLine.length()-1).split(", ");
            metrics = new double[values.length];
            mris = new double[values.length];
            store(metrics, metricLine);
            store(mris, mriLine);
            normalize(mris);
            normalize(metrics);
            try {
            pearson = -1 * new PearsonsCorrelation().correlation(metrics, mris);
            spearman = -1 * new SpearmansCorrelation().correlation(metrics, mris);
            } catch (Exception e) {
                System.out.println("OH?");
            }
        }

        private void normalize(double[] v) {
            double min = Utils.getMin(v);
            double max = Utils.getMax(v);
            double diff = max - min;

            for(int i = 0; i < v.length; i++) {
                if (diff == 0) {
                    v[i] = 1;
                } else {
                    v[i] = (v[i] - min) / diff;
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            double[] copy = Arrays.copyOf(mris, mris.length);
            for(int k = 0; k < mris.length; k++) {
                int i = weka.core.Utils.maxIndex(copy);
                copy[i] = Double.NEGATIVE_INFINITY;
                ret.insert(0, String.format("(%.4f, %.4f) ", mris[i], metrics[i]));
            }
            return ret.toString();
        }

        private void store(double[] v, String line) {
            String[] tokens = line.substring(1, line.length()-1).split(", ");
            try {
                for(int i = 0; i < tokens.length; i++) {
                    v[i] = Double.parseDouble(tokens[i]);
                }
            } catch (Exception e) {
                System.out.println(line + "\n" + Arrays.toString(tokens));
                throw e;
            }
        }


    }

    public static void main(String... args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        File folder = new File("./results");
        double[][][][][] complete = new double[5][5][3][][];
        Pairs[][][][][] allpairs = new Pairs[5][5][3][][];

        for (File file: folder.listFiles()) {
            if (file.getName().startsWith("results_noprior_Multi")) {
                double[][] values = new double[10][5];
                Pairs[][] pairs = new Pairs[10][5];

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                int i, j;
                String resultName = line;
                String metricLine = null;

                for(i = 1, j = -1, line = reader.readLine(); line != null; i = (i+1) % 20, line = reader.readLine()) {
                    if (line.startsWith("Analysis for")) {
                        i = -1;
                        j++;
                        continue;
                    }
                    if (i % 4 == 0 && !line.isEmpty()) {
                        values[j][i/4] = Double.valueOf(line.split(" ")[1]);
                    }
                    if (i % 4 == 2) {
                        metricLine = line;
                    }
                    if (i % 4 == 3) {
                        pairs[j][i/4] = new Pairs(metricLine, line);
                    }
                }

                storeResult(complete, allpairs, resultName, values, pairs);

                reader.close();
            }
        }

        for(int i = 0; i < 5; i++) {
            System.out.println("\\midrule");
            System.out.print("\\multirow{3}*{\\textbf{"+ ((i+1)*10) +"}}");
            for(int j = 0; j < 3; j++) {
                System.out.print(" & \\textbf{"+(j+3)+"} ");
                for(int k = 0; k < 5; k++) {
                    //printSpearmanResult("", complete[i][k][j], ACCURACY);
                    printPearsonResult("", allpairs[i][k][j], MATTHEWS);
                }
                System.out.println(" \\\\");
            }
            System.out.println();
        }
/*
        for(int profileSize = 0; profileSize < 5; profileSize++) {
            for(int sigmaMaxPercent = 0; sigmaMaxPercent < 5; sigmaMaxPercent++) {
                for(int clusters = 0; clusters < 3; clusters++) {
                    printGraphics(profileSize, sigmaMaxPercent, clusters, allpairs[profileSize][sigmaMaxPercent][clusters], ACCURACY);
                }
            }
        }
        */
/*
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s;
        System.out.print("Insert indices: ");

        while ((s = in.readLine()) != null && s.length() != 0) {
            int[] is = splitIndices(s);
            System.out.println(allpairs[is[0]][is[1]][is[2]][is[3]][is[4]]);
            System.out.print("Insert indices: ");
        }
*/
    }

private static final String preamble =
"\\begin{tikzpicture}\n" +
"\\begin{axis}\n" +
"[xmin=0,xmax=1,ymin=0,ymax=1,title={%s, $m = %d$, $\\sigma_{max} = %d\\%%$, %d clusters},\n" +
"grid=major,xlabel=$MRI$,ylabel=$%s$,\n" +
"width=0.5\\textwidth,height=0.5\\textwidth,\n" +
"legend style={anchor=west, at={(1.02,0.5)}}]\n\n";

private static final String[] end =
        {"\n\\legend{$banana$,$phoneme$,$ring$,$spambase$,$twonorm$}\n" +
         "\\end{axis}\n" +
         "\\end{tikzpicture}\n",
         "\n\\legend{$appendicitis$,$breast$,$mammographic$,$pima$,$wdbc$}\n" +
         "\\end{axis}\n" +
         "\\end{tikzpicture}\n"};

private static final String plot = "\\addplot[mark=%s,color=%s] coordinates { %s };\n";

    private static void printGraphics(int profileSize, int sigmaMaxPercent, int clusters, Pairs[][] pairs, int what) {
        StringBuilder[] ret = {new StringBuilder(), new StringBuilder()};
        ret[0].append(String.format(preamble, "Large", (profileSize+1)*10, (sigmaMaxPercent+2)*5, clusters+3, sayWhat(what)));
        ret[1].append(String.format(preamble, "Small", (profileSize+1)*10, (sigmaMaxPercent+2)*5, clusters+3, sayWhat(what)));

        for(int i = 0; i < 10; i++) {
            ret[(int)symbols[i][2]].append(String.format(plot, symbols[i][0], symbols[i][1], pairs[i][what]));
        }

        ret[0].append(end[0]);
        ret[1].append(end[1]);
        System.out.println(ret[0]);
        System.out.println(ret[1]);
    }

    private static String sayWhat(int what) {
        switch(what) {
            case 0:
                return "Precision";
            case 1:
                return "Recall";
            case 2:
                return "Accuracy";
            case 3:
                return "F-Score";
            case 4:
                return "MCC";
        }
        return null;
    }

    private static int[] splitIndices(String s) {
        int[] ret = new int[5];
        String[] tokens = s.split(" ");
        for(int i = 0; i < 5; i++) {
            ret[i] = Integer.valueOf(tokens[i]);
        }
        return ret;
    }

    private static void storeResult(double[][][][][] complete, Pairs[][][][][] allpairs, String resultName, double[][] values, Pairs[][] pairs) {
        String[] integers = resultName.split("\\D+");
        int profileSize = Integer.parseInt(integers[3])/10 - 1;
        int sigmaMaxPercent = Integer.parseInt(integers[5])/5 - 2;
        if (sigmaMaxPercent < 0)
            return;
        int clusters = Integer.parseInt(integers[1])-3;
        complete[profileSize][sigmaMaxPercent][clusters] = values;
        allpairs[profileSize][sigmaMaxPercent][clusters] = pairs;
    }

    private static void printSpearmanResult(String resultName, double[][] values, int col) {
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
                if (symbols[i][2] == 0)
                    count -= ((int)values[i][col])*100;
                else
                    count -= (int)values[i][col];
            }
            System.out.print(String.format(" &%3d,%3d", count/100, count%100));
//        }
//        System.out.println();
    }

    private static void printPearsonResult(String resultName, Pairs[][] pairs, int col) {
        double[] corr = new double[pairs.length];
        for(int i = 0; i < pairs.length; i++) {
            try { corr[i] = pairs[i][col].pearson; } catch(Exception e) {
                System.out.print(String.format(" & $%4.1f\\%% \\pm %4.1f$", 0.0, 0.0));
                return;
            }
        }
        double mean = weka.core.Utils.mean(corr);
        System.out.print(String.format(" & $%4.1f\\%% \\pm %4.1f$", 100*mean, 100*confidence(corr, mean)));
    }

    private static double confidence(double[] corr, double mean) {
        return 1.96 * Math.sqrt(weka.core.Utils.variance(corr) / corr.length);
    }

    private static double maxDistance(double[] v, double val) {
        double min = Utils.getMin(v);
        double max = Utils.getMax(v);

        return Math.max(val - min, max - val);
    }

}
