import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * @author 25526693 | CJ TELFER
 */

/**
 * Compress & decompress images (with multi-resolution functionality)
 * using finite automata to encode and decode black pixels as strings
 * from the alphabet {0,1,2,3}
 */
public class Compress {

    private static int accState = 0;

    /**
     * Main class for the compression/decompression
     * -> handles input errors
     * -> makes calls to other classes to build the data strcuture and create GUI
     * -> reads from input arguments & creates output files
     * 
     * @param args -> <GUI mode> <mode> <multi-resolution flag> <filepath/name>
     */
    public static void main(String[] args) {
        int gui, mode, wordLen;
        String multiRes, filePath;

        validateArguments(args);
        gui = Integer.parseInt(args[0]);
        mode = Integer.parseInt(args[1]);
        multiRes = args[2];
        wordLen = -1;
        int multiResMode = 0;

        // Multi res -> extra argument of word length
        if (multiRes.equalsIgnoreCase("t") && mode == 1) {
            if (args.length != 5) {
                System.err.println("Input Error - Invalid number of arguments");
                System.exit(0);
            }
            File file = new File(args[4]);
            try {
                Scanner sc = new Scanner(file);
                sc.close();
            } catch (FileNotFoundException e) {
                System.err.println("Input Error - Invalid or missing file");
                System.exit(0);
            }
            try {
                wordLen = Integer.parseInt(args[3]);
                if (wordLen < 0) {
                    System.err.println("Decompress Error - Invalid word length");
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                System.err.println("Input Error - Invalid argument type");
                System.exit(0);
            }
            filePath = args[4];

        } else if (multiRes.equalsIgnoreCase("t") && mode == 2) {
            if (args.length != 5) {
                System.err.println("Input Error - Invalid number of arguments");
                System.exit(0);
            }
            File file = new File(args[4]);

            try {
                Scanner sc = new Scanner(file);
                sc.close();
            } catch (FileNotFoundException e) {
                System.err.println("Input Error - Invalid or missing file");
                System.exit(0);
            }
            try {
                multiResMode = Integer.parseInt(args[3]);
                if (!(multiResMode == 1 || multiResMode == 2 || multiResMode == 3)) {
                    System.err.println("Compress Error - Invalid multi-resolution method");
                    System.exit(0);
                }
            } catch (NumberFormatException e) {
                System.err.println("Input Error - Invalid argument type");
                System.exit(0);
            }
            filePath = args[4];

        } else {
            filePath = args[3];
        }

        if (mode == 1) { // Decompression
            decompress(gui, wordLen, filePath);
        } else if (mode == 2) { // Compression
            compressImage(gui, multiResMode, filePath);
        }

    }

    /**
     * Decompress method
     * -> scans text file
     * -> handles file formatting errors
     * -> Builds hashmap using Automaton object
     * -> Finds valid words to accept states
     * -> Creates and writes to image
     * 
     * @param gui      -> int value for gui (0 = no gui, 1 = gui active)
     * @param wordLen  -> int value for multires mode
     * @param filePath -> String value for the path and filename of input file
     */
    private static void decompress(int gui, int wordLen, String filePath) {
        File file = new File(filePath);
        int numStates = 0;
        Automaton aut = new Automaton();

        try {
            Scanner sc = new Scanner(file);
            String line = sc.nextLine();
            try {
                numStates = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.err.println("Decompress Error - Invalid automaton formatting");
                System.exit(0);
            }
            for (int i = 1; i < numStates; i++) {
                aut.addState(i);
            }

            line = sc.nextLine();
            Scanner scLine = new Scanner(line);
            while (scLine.hasNext()) {
                String num = scLine.next();
                try {
                    if (Integer.parseInt(num) < numStates) {
                        aut.addAcceptState(Integer.parseInt(num));
                    } else {
                        System.err.println("Decompress Error - Invalid accept state");
                        System.exit(0);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Decompress Error - Invalid automaton formatting");
                    System.exit(0);
                }
            }
            scLine.close();
            while (sc.hasNextLine()) {
                int si, sf, next, symbol, lineCount;

                si = 0;
                sf = 0;
                next = 0;
                symbol = 0;

                lineCount = 0;
                line = sc.nextLine();
                scLine = new Scanner(line);
                while (scLine.hasNext()) {
                    try {
                        next = Integer.parseInt(scLine.next());
                    } catch (NumberFormatException e) {
                        System.err.println("Decompress Error - Invalid automaton formatting");
                        System.exit(0);
                    }
                    lineCount++;
                    switch (lineCount) {
                        case 1:
                            si = next;
                            if (si > numStates - 1) {
                                System.err.println("Decompress Error - Invalid transition");
                                System.exit(0);
                            }
                            break;
                        case 2:
                            sf = next;
                            if (sf > numStates - 1) {
                                System.err.println("Decompress Error - Invalid transition");
                                System.exit(0);
                            }
                            break;
                        case 3:
                            symbol = next;
                            if (symbol < 0 || symbol > 3) {
                                System.err.println("Decompress Error - Invalid transition");
                                System.exit(0);
                            }
                            break;
                        default:
                            System.err.println("Decompress Error - Invalid automaton formatting");
                    }
                }
                aut.addTransition(si, sf, symbol);
                scLine.close();
            }
            sc.close();

            aut.getTransitions(0);

            List<String> words = aut.findWords(wordLen);
            filePath = filePath.substring(filePath.lastIndexOf("/"),
                    filePath.indexOf(".txt"));

            int size = 0;
            int maxLen = 0;
            for (int i = 0; i < words.size(); i++) {
                if (words.get(i).length() > maxLen) {
                    maxLen = words.get(i).length();
                }
            }
            size = (int) Math.pow(2.0, maxLen);
            if (size <= 0) {
                size = 1;
            }
            if (wordLen > 0) {
                size = (int) Math.pow(2.0, wordLen);
            }
            // Create image and set all pixels to white
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            int white = 0xFFFFFF;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    image.setRGB(x, y, white);
                }
            }

            // Sets coordinates on image to black from list of found words
            // to accept states
            for (int i = 0; i < words.size(); i++) {
                set(getCoordinates(words.get(i), size), image);
            }

            // Save the image as a PNG file
            File output = new File("out/" + filePath + "_dec.png");
            try {
                ImageIO.write(image, "png", output);
            } catch (IOException e) {
                System.err.println("Input Error - Invalid or missing file");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Input Error - Invalid or missing file");
            System.exit(0);
        }
    }

    /**
     * Main compression method
     * -> Makes calls to buildDFA, findWordsToBlack and writeToFile
     * -> Opens image from filepath and handles errors
     * -> Finds all black quadrants and the words describing the addresses
     * -> Builds DFA
     * -> Writes output transitions to new file
     * 
     * @param gui          -> Int for gui mode (0 = off, 1 = on)
     * @param multiResMode -> value for multi-res ciompression mode {1,2,3}
     * @param filePath     -> String for filepath to the image to be compressed
     */
    public static void compressImage(int gui, int multiResMode, String filePath) {
        try {
            File imageFile = new File(filePath);
            BufferedImage image = ImageIO.read(imageFile);

            int width = image.getWidth();
            int height = image.getHeight();

            if (width != height) {
                System.err.println("Compress Error - Invalid input image");
                System.exit(0);
            }

            FindWordsToBlack language = new FindWordsToBlack();
            int size = width;
            int maxlen = 0;
            while (size >= 2) {
                size = size / 2;
                maxlen++;
            }
            List<String> words = language.process(image, maxlen);

            List<String> output = buildDFA(words, multiResMode);

            if (multiResMode == 1) {

                int[] count = {0, 0, 0, 0};
                for (int i = 0; i < words.size(); i++) {
                    int quadrant = Integer.parseInt(words.get(i).charAt(0) + "");
                    switch (quadrant) {
                        case 0:
                            count[0]++;
                            break;
                        case 1:
                            count[1]++;
                            break;
                        case 2:
                            count[2]++;
                            break;
                        case 3:
                            count[3]++;
                            break;
                        default:
                            break;
                    }
                }
                for (int i = 0; i < count.length; i++) {
                    System.out.println("Quadrant " + i + " :" + count[i]);
                }

                int max = 999999;
                List<Integer> whiteQuadrants = new ArrayList<>();
                for (int i = 0; i < count.length; i++) {
                    if (count[i] <= max) {
                        max = count[i];
                    }
                }
                for (int i = 0; i < count.length; i++) {
                    if (count[i] == max) {
                        whiteQuadrants.add(i);
                    }
                }
                int min = 999999;
                for (int i = 0; i < whiteQuadrants.size(); i++) {
                    if (whiteQuadrants.get(i) < min) {
                        min = whiteQuadrants.get(i);
                    }
                }
                int whiteQuadrant = min;
                System.out.println("WHITE: " + whiteQuadrant + " " + accState);

                for (int i = 0; i < 4; i++) {
                    if (i != whiteQuadrant) {
                        output.add(accState + " " + accState + " " + i);
                    }
                }

            }
            writeWordsToFile(output, filePath);

        } catch (IOException e) {
            System.err.println("Compress Error - Invalid input image");
            System.exit(0);
        }
    }

    /**
     * Validates input arguments from input stream
     * Terminates program with exit error input is invalid
     * 
     * @param args Array of input arguments -> expected form:
     *             <GUI mode> <mode> <multi-resolution flag> <optional wordlen>
     *             <filepath/name>
     */
    private static void validateArguments(String[] args) {
        String error = "";

        if (args.length > 5 || args.length < 4) { // Invalid no. arguments
            error = "Input Error - Invalid number of arguments";
        } else {
            try { // Invalid argument types
                int gui = Integer.parseInt(args[0]);
                int mode = Integer.parseInt(args[1]);
                char multiResFlag = args[2].charAt(0);
                String validFlags = "ftFT";
                if (!Character.isLetter(multiResFlag)) {
                    error = "Input Error - Invalid argument type";
                } else if (!(gui == 0 || gui == 1)) {
                    error = "Input Error - Invalid GUI argument";
                } else if (!(mode == 1 || mode == 2)) {
                    error = "Input Error - Invalid mode";
                } else if (args[2].length() > 1 || !(validFlags.contains(multiResFlag + ""))) {
                    error = "Input Error - Invalid multi-resolution flag";
                }
            } catch (NumberFormatException e) {
                error = "Input Error - Invalid argument type";
            }
        }

        if (!error.equals("")) {
            System.err.println(error);
            System.exit(0);
        }
    }

    /**
     * Sets pixels at coordinates (given in parameters) to be coloured black
     * 
     * @param image -> decompressed png image
     * @param x     -> x-coordinate of pixel to be coloured
     * @param y     -> y-coordinate of pixel to be coloured
     */
    private static void setPixel(BufferedImage image, int y, int x) {
        int black = 0;
        int rgb = (black << 16) | (black << 8) | black;
        image.setRGB(x - 1, y - 1, rgb);
    }

    /**
     * Methods that recieves a word over the alphabet {0,1,2,3} and the calculated
     * size of the image -> uses this to create a 2D array of the coordinate sof
     * pixels
     * to be coloured black
     * 
     * @param sequence -> word(string) or sequence over given alphabet (gives
     *                 coordinates)
     * @param size     -> size(integer) of the current image (in pixels)
     * @return -> returns a 2D array(integer) of the coordinates associated with the
     *         word
     */
    private static int[][] getCoordinates(String sequence, int size) {
        int[][] pos = new int[2][size];
        int[] yArr = new int[size];
        int[] xArr = new int[size];
        int length;

        for (int i = 0; i < size; i++) {
            yArr[i] = i + 1;
            xArr[i] = i + 1;
        }

        for (int i = 0; i < sequence.length(); i++) {
            size /= 2;
            length = yArr.length;
            char ch = sequence.charAt(i);

            for (int j = 0; j < length; j++) {
                switch (ch) {
                    case '0':
                        if (j + 1 > size) {
                            xArr[j] = -1;
                        } else if (j + 1 <= size) {
                            yArr[j] = -1;
                        }
                        break;
                    case '1':
                        if (j + 1 > size) {
                            yArr[j] = -1;
                            xArr[j] = -1;
                        }
                        break;
                    case '2':
                        if (j + 1 <= size) {
                            yArr[j] = -1;
                            xArr[j] = -1;
                        }
                        break;
                    case '3':
                        if (j + 1 > size) {
                            yArr[j] = -1;
                        } else if (j + 1 <= size) {
                            xArr[j] = -1;
                        }
                        break;
                    default:
                }
            }

            int count = 0;
            for (int j = 0; j < length; j++) {
                if (yArr[j] != -1) {
                    count++;
                }
            }
            int[] tempY = new int[count];
            int[] tempX = new int[count];
            int y = 0, x = 0;
            for (int j = 0; j < length; j++) {
                if (yArr[j] != -1) {
                    tempY[y] = yArr[j];
                    y++;
                }
                if (xArr[j] != -1) {
                    tempX[x] = xArr[j];
                    x++;
                }
            }
            yArr = new int[count];
            xArr = new int[count];
            yArr = tempY;
            xArr = tempX;

        }
        pos = new int[2][yArr.length];
        for (int i = 0; i < yArr.length; i++) {
            pos[0][i] = yArr[i];
            pos[1][i] = xArr[i];
        }
        return pos;
    }

    /**
     * Void method that sets the colour of pixels on the image to black, given
     * coordinates in a 2D array
     * 
     * @param pos   -> 2D array(integers) of the y[0][n] and x[1]][n] coordinates of
     *              a pixel
     * @param image -> The image to be coloured
     */
    private static void set(int[][] pos, BufferedImage image) {
        for (int i = 0; i < pos[0].length; i++) {
            for (int j = 0; j < pos[1].length; j++) {
                setPixel(image, pos[0][i], pos[1][j]);

            }
        }
    }

    /**
     * Builds a DFA unique to the list of input "words".
     * -> Uses an Automaton.java object to build automaton
     * -> Makes calls to other helper methods such as getAlphabet
     * -> Correctly adds states, transitions between states and
     * accept states
     * 
     * @param words -> The list of input words (Describing paths of transitions)
     * @param mode  -> The value for the multi-resolution compression mode {1,2,3}
     * @return -> A list of the no. of states, value of accept states, and all
     *         transitions between states
     */
    public static List<String> buildDFA(List<String> words, int mode) {
        List<String> output = new ArrayList<>();
        int n = 0;
        List<Set<String>> language = new ArrayList<>();
        language.add(new HashSet<>(words));

        Automaton automaton = new Automaton();
        Set<Character> alphabet = getAlphabet(new HashSet<>(words));

        // Transition function represented as a map of origin state to a map of input
        // character to set of destination states
        Map<Integer, Map<Character, Set<Integer>>> transitions = new HashMap<>();

        for (int i = 0; i <= n; i++) {
            int currentState = i;
            automaton.addState(i);

            for (Character a : alphabet) {
                Set<String> nextLangState = new HashSet<>();
                for (String word : language.get(i)) {
                    if (word.startsWith(a.toString())) {
                        nextLangState.add(word.substring(a.toString().length()));
                    }
                }

                if (nextLangState.isEmpty()) {
                    continue;
                }

                // Add the next state to the set of destinations for this input character
                Map<Character, Set<Integer>> destStates = transitions.computeIfAbsent(currentState,
                        k -> new HashMap<>());
                Set<Integer> destSet = destStates.computeIfAbsent(a, k -> new HashSet<>());
                int j;
                for (j = 0; j <= n; j++) {
                    if (language.get(j).equals(nextLangState)) {
                        destSet.add(j);
                        break;
                    }
                }

                if (j > n) {
                    n++;
                    language.add(nextLangState);
                    automaton.addState(n);
                    destSet.add(n);
                }
            }

            if (language.get(i).contains("")) {
                automaton.addAcceptState(i);
            }
        }

        // Add the transitions to the automaton
        for (int state : transitions.keySet()) {
            Map<Character, Set<Integer>> destStates = transitions.get(state);
            for (char a : destStates.keySet()) {
                Set<Integer> destSet = destStates.get(a);
                for (int dest : destSet) {
                    automaton.addTransition(state, dest, a);
                }
            }
        }

        // reduction -> multi-resolution
        if (mode == 3) {
            for (int i = 0; i < automaton.getNumStates(); i++) {
                automaton.addAcceptState(i);
            }
        }

        accState = automaton.getAccStates().get(0); // set accept state locally
        output = automaton.getTransitions(mode);
        return output;
    }

    /**
     * Gets the current alphabet for the buildDFA method
     * 
     * @param language -> Set of the language (L element of {0, 1, 2, 3})
     * @return -> Set of characters of new current alphabet
     */
    public static Set<Character> getAlphabet(Set<String> language) {
        Set<Character> alphabet = new HashSet<>();
        for (String word : language) {
            for (char c : word.toCharArray()) {
                alphabet.add(c);
            }
        }
        return alphabet;
    }

    /**
     * Accepts the list of words (transitions between states) and writes them to a
     * new file with
     * the "_cmp.txt" extension.
     * 
     * @param words    -> List<String> of input list to be witten to file
     * @param filePath -> The filepath and name of the file to be created
     */
    public static void writeWordsToFile(List<String> words, String filePath) {
        String outputFile;
        try {
            outputFile = filePath.substring(filePath.lastIndexOf("/"),
                    filePath.indexOf(".png"));
            filePath = "out/" + outputFile + "_cmp.txt";
            FileWriter fw = new FileWriter(filePath);
            BufferedWriter writer = new BufferedWriter(fw);
            for (String word : words) {
                writer.write(word);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Input Error - Invalid or missing file");
        }
    }
}
