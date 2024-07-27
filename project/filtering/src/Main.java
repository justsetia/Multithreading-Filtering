import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class Main {
    public static void main(String[] args) throws IOException {

        // creating the main threads
        Process main = new Process(true,0);
        main.run();
        // There is need for have the bottom of the matrix
        Process.Last = Process.ImgMatrix.length;
        //Having the numbers of columns for dividing and create the threads
        int Col = Process.ImgMatrix[0].length;
        //the numbers of threads
        //as the numbers of threads must be at least 9
        int numThreads = Math.max(9 , Col / 12); //12 is 6 *2

        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

        int chunkSize = 6;
        //the value for controlling
        Process.total = Col/12;

        //the numbers of threads is less than what we need
        //so each threads after finishing will start agian with another sub-matrix
        for (int i = 0 ; i < Col/6; i+=1) {
            threadPool.execute(new Process(false, i*chunkSize));
        }
        threadPool.shutdown();

        // waiting for all threads to be finished
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // The code under is for reassemble the image
        int[][] processedMatrix = Process.ImgMatrix;
        //System.out.println(Arrays.deepToString(Process.ImgMatrix));


        BufferedImage image = new BufferedImage(processedMatrix[0].length,
                processedMatrix.length, BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < processedMatrix.length; i++) {
            for (int j = 0; j < processedMatrix[i].length; j++) {
                int pixelValue = processedMatrix[i][j];
                Color newColor = new Color(pixelValue, pixelValue, pixelValue);
                image.setRGB(j, i, newColor.getRGB());
            }
        }

        File output = new File("F:\\Project_AZ_OS\\Result2.jpg");
        ImageIO.write(image, "jpg", output);



    }
}