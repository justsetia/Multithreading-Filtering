import sun.plugin.ClassLoaderInfo;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class Process implements Runnable {

    // the start is the first location of the column
    int start;
    //this is the start for row
    int down=0;
    static  int total ;
    private static final Lock lock = new ReentrantLock();
    static int [][] MXD = new int[3][3];
    static int [][] MYD = new int[3][3];
    static int Last;
    public static int[][] ImgMatrix;



    Process (Boolean main, int start) throws IOException {

        // initialing the X-direction
        MXD[0][0]= -1;
        MXD[0][1]=0;
        MXD[0][2] = 1;
        MXD [1][0]= -2;
        MXD [1][1] =0;
        MXD [1][2] =2;
        MXD [2][0] =-1;
        MXD [2][1] =0;
        MXD [2][2]= 1;

        // initialing the Y-direction
        MYD[0][0]= -1;
        MYD[0][1]= -2;
        MYD[0][2] = -1;
        MYD [1][0]= -0;
        MYD [1][1] =0;
        MYD [1][2] =0;
        MYD [2][0] = 1;
        MYD [2][1] = 2;
        MYD [2][2]= 1;

        // initialing the start
        this.start= start;
        // to be sure that only the main thread will Read image from memory
        if( main){
           //getting the image from memory
           BufferedImage img = ImageIO.read(new File("F:\\Project_AZ_OS\\MUlax0Ho.jpg"));

           //getting the width and the height
           int width = img.getWidth();
           int height = img.getHeight();

           //turn it to the matrix
           ImgMatrix= new int[height][width];

           for (int i = 0; i < height; i++) {
               for (int j = 0; j < width; j++) {
                   ImgMatrix[i][j] = img.getRGB(j, i);
               }
           }
       }
    }

    public int Multiply(int row ,int col){

        // The matrix Y-direction is not needed
        // int [][] MYD = new int[3][3];
        int XRes=0;
        int YRes=0;
        for ( int i=0 ; i<3 ; i+=1){
            for ( int j=0 ; j<3 ; j+=1){
                XRes += ImgMatrix[i + row][j + col] * MXD[i][j];
                YRes += ImgMatrix[i + row][j + col] * MYD[i][j];

            }
        }
        // here before returning must o the sqrt and the formula
        XRes = XRes * XRes;
        YRes = YRes * YRes;
        int Res = XRes + YRes;

        double result = Math.sqrt(Res);

        Res =(int) Math.round(result);

        return Res;

    }

    @Override
    public void run(){
        // this list is for having the sum and use the mean of them
        List<Integer> list = new ArrayList<>();

        int sum;
        // to be sure that it goes to the last row
        while (this.down < Last) {
            sum=0;
            int tmp = this.start;

            // each thread has 6*6 matrix
            while ((tmp < 6 + this.start) && (this.down + 5 < Last)) {
                tmp += 1;
                for (int row = this.down; row < this.down + 6; row += 1) {

                    int rgb = ImgMatrix[row][tmp];
                    //now extracting each color
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    // calculating the gray
                    double gray = 0.2989 * red + 0.5870 * green + 0.1140 * blue;
                    // replace the value with gray
                    ImgMatrix[row][tmp] = (int) gray;
                    sum+=(int) gray;
                }
            }
            //to save the mean
            list.add(sum/36);
            //as each matrix is 6*6, so it must go to the next 6 row
            this.down += 6;
        }
        //as we want to iterate the matrix from the first
        this.down=0;

        //because of changing the total
        // it is critical section
        lock.lock(); // Acquire the lock
        try {
            total -= 1;
            // Other operations related to total...
        } finally {
            lock.unlock(); // Release the lock
        }

        // this loop waits for other threads to be finished
        while(true){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // means all threads reaches to this part
            // and ready to continue the rest
            if(total<1)break;
        }
        // as we divide to sub-matrix must check it to not have error
        while( this.down< (Last- Last%6)){

            int tmp = this.start;
            // defining the mode for better accurate
            boolean mode=false; // if false it is 0 if not it is 255

            for( int i=this.down ; i < this.down+4 ; i+=1 ){
                for( int j=tmp ; j<tmp+4 ; j+=1){
                    int res=Multiply(i,j);
                    if (res > list.get(this.down/6) ){
                        lock.lock(); // Acquire the lock
                        try {
                            ImgMatrix[i][j]=0;
                            // Other operations related to total...
                        } finally {
                            lock.unlock(); // Release the lock
                        }

                        mode =false;
                    }
                    else {
                        lock.lock(); // Acquire the lock
                        try {
                            ImgMatrix[i][j]=255;
                            // Other operations related to total...
                        } finally {
                            lock.unlock(); // Release the lock
                        }
                        mode =true;

                    }
                }
                if(mode) {
                    ImgMatrix[i][tmp + 4] = 255;
                    ImgMatrix[i][tmp + 5] = 255;
                }
                else  {
                    ImgMatrix[i][tmp + 4] = 0;
                    ImgMatrix[i][tmp + 5] = 0;
                }

            }

            for(int f=tmp; f<tmp+6 ; f+=1){
                if(mode) {
                    ImgMatrix[this.down + 4][f] = 255;
                    ImgMatrix[this.down + 5][f] = 255;
                }
                else {
                    ImgMatrix[this.down + 4][f] = 0;
                    ImgMatrix[this.down + 5][f] = 0;
                }

                }
            this.down+=6;

        }
        //this.down< (Last- Last%6)
        for(int f=this.start; f<this.start+6 ; f+=1){
            for (int k=Last - Last%6 ; k<Last ; k+=1) {
                ImgMatrix[k][f] = 0;
                ImgMatrix[k][f] = 0;
            }
        }


        // this is for the edge of outer matrix (the whole matrix)
        int restRow = ImgMatrix.length %6;
        int restCol = ImgMatrix[0].length %6;

        for(int i=ImgMatrix.length-1; i>ImgMatrix.length-restRow-1 ; i=-1){
            for(int j=0 ; j< ImgMatrix[0].length ; j+=1){
                ImgMatrix[i][j]=255;
            }
        }
        for( int j= ImgMatrix[0].length-1 ; j>ImgMatrix[0].length-restCol-1 ; j-=1){
            for(int i=0 ; i< ImgMatrix.length; i+=1){
                ImgMatrix[i][j]=255;
            }
        }
    }

}
