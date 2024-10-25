import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 25526693 | CJ TELFER
 */

/**
 * The findWordsToBlack class provides a method for finding black squares in a
 * given even, square black and white image and returning the corresponding
 * word list
 */
public class FindWordsToBlack {
    private final ArrayList<String> wordList = new ArrayList<>();

    /**
     * Processes the given image to find black squares and return the corresponding
     * word list
     * 
     * @param image      -> the image to process
     * @param maxWordLen - > the maximum length of words to be included in the word
     *                   list
     * @return -> the list of words found during image processing
     */
    public List<String> process(BufferedImage image, int maxWordLen) {
        findBlackSquares(image, new ArrayList<>(), maxWordLen);
        return wordList;
    }

    /**
     * Recursively searches for black squares in the given image and adds the
     * corresponding coordinates to the word list
     * 
     * @param image         -> the image to search
     * @param currentCoords -> the coordinates of the current square being searched
     * @param maxWordLen    -> the maximum length of words to be included in the
     *                      word list (max recursive depth)
     */
    private void findBlackSquares(BufferedImage image, List<Integer> currentCoords,
            int maxWordLen) {
        if (image.getWidth() == 1 && image.getHeight() == 1) {
            if (image.getRGB(0, 0) == 0xFF000000) { // Black pixel
                addWord(currentCoords);
            }
            return;
        }

        if (allPixelsBlack(image)) {
            addWord(currentCoords);
            return;
        }

        if (maxWordLen == 0) {
            return;
        }

        Image[] quadrants = divideImage(image);
        for (int i = 0; i < quadrants.length; i++) {
            BufferedImage subImage = (BufferedImage) quadrants[i];
            List<Integer> newCoords = new ArrayList<>(currentCoords);
            newCoords.add(i);
            findBlackSquares(subImage, newCoords, maxWordLen - 1);
        }
    }

    /**
     * Checks whether all pixels in the given image are black
     * 
     * @param image -> the image to check
     * @return -> true if all pixels are black, false otherwise
     */
    private boolean allPixelsBlack(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) != 0xFF000000) { // Not a black pixel
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Divides the given image into four quadrants
     * -> each quadrant becomes a new subimage
     * | 1 3 |
     * | 0 2 |
     * 
     * @param image -> image to divide
     * @return -> array of the four resulting quadrant images
     */
    private Image[] divideImage(BufferedImage image) {
        Image[] quadrants = new Image[4];
        quadrants[0] = image.getSubimage(0, image.getHeight() / 2, image.getWidth() / 2,
                image.getHeight() / 2);
        quadrants[1] = image.getSubimage(0, 0, image.getWidth() / 2,
                image.getHeight() / 2);
        quadrants[2] = image.getSubimage(image.getWidth() / 2, image.getHeight() / 2,
                image.getWidth() / 2, image.getHeight() / 2);
        quadrants[3] = image.getSubimage(image.getWidth() / 2, 0, image.getWidth() / 2,
                image.getHeight() / 2);
        return quadrants;
    }

    /**
     * Converts the given coordinates to a word and adds it to the word list
     * 
     * @param coords -> coordinates of the black square to add to the word list
     */
    private void addWord(List<Integer> coords) {
        StringBuilder wordBuilder = new StringBuilder();
        for (Integer coord : coords) {
            wordBuilder.append(coord);
        }
        wordList.add(wordBuilder.toString());
    }

}
