import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class HW1_weiming_liu {

	public static void main(String[] args) throws IOException {
		
		//assuming the size of all sample images is 400 x 400
		int width = 400;
		int height = 400;
		
		//if the number of parameter is incorrect, print the usage and terminate
		if (args.length != 4){
			System.out.println("Usage: java HW1_weiming_liu file_name scale rotation anti-aliasing");
			System.exit(0);
		}
		
		//get parameters from command line
		String fileName = args[0];
		double scale = Double.parseDouble(args[1]);
		double rotation = -Double.parseDouble(args[2])*Math.PI/180;
		int anti_aliasing = Integer.parseInt(args[3]);
		if (scale == 0){ //check if scale is 0
			System.out.println("Scale cannot be 0");
			System.exit(0);
		}
		
		File file = new File(fileName);
		byte[] bytes = new byte[(int)file.length()];
		
		//copy the data in file into a byte array
		try {
			bytes = fileToBytes(file);
		} catch (IOException e) {
			System.out.println("Unable to load data from the file");
			System.out.println("Please doublecheck the file name");
			System.exit(0);
		}

		//put the bytes in the byte array into a 3-dimensional integer array
		int[][][] inputImageArray = new int[3][height][width];
		bytesToArray(bytes, inputImageArray);

		//do anti-aliasing when the parameter is not 0
		int[][][] tempImageArray = new int[3][height][width];
		if (anti_aliasing != 0){ //do anti-aliasing
			tempImageArray = antiAliasing(inputImageArray);
		}
		else {
			tempImageArray = inputImageArray;
		}
		
		//create another array to store the modified output data
		//first, calculate the height and width of the modified image
		int new_height = (int)(scale *
						 (Math.abs(height * Math.cos(rotation)) + 
						  Math.abs(width * Math.sin(rotation))));
		int new_width = (int)(scale *
		 				(Math.abs(height * Math.cos(rotation)) + 
		 				 Math.abs(width * Math.sin(rotation))));
		
		//create an output image array, and store the modified image data to the new array
		int[][][] outputImageArray = new int[3][new_height][new_width];
		rotateAndScale(tempImageArray, outputImageArray, rotation, scale);
		
		//create a BufferedImage object using the data from input image
		//so we can show the original image
		BufferedImage inputImg = bytesToImg(bytes, height, width);
		
		//convert the 3-dimensional array to a byte array
		bytes = arrayToBytes(outputImageArray); 
        
		//create a BufferedImage object using the modified data
		//so we can the modified image
		BufferedImage outputImg = bytesToImg(bytes, new_height, new_width);
        
		//use a frame to display the original image
		JFrame originalFrame = new JFrame();
	    JLabel originalLabel = new JLabel(new ImageIcon(inputImg));
	    originalFrame.getContentPane().add(originalLabel, BorderLayout.CENTER);
	    originalFrame.pack();
	    originalFrame.setTitle("Original Image");
	    originalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    originalFrame.setVisible(true);
		
		//use a frame to display the modified image
	    JFrame newFrame = new JFrame();
	    JLabel newLabel = new JLabel(new ImageIcon(outputImg));
	    newFrame.getContentPane().add(newLabel, BorderLayout.CENTER);
	    newFrame.pack();
	    newFrame.setTitle("Modified Image");
	    newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    newFrame.setVisible(true);
	    
	    System.out.println("Please close the images to terminate the program");
	}//end of main
	
	//this method take a file as an input 
	//and output the data of the file into a byte array
	public static byte[] fileToBytes(File file) throws IOException{
		InputStream is = new FileInputStream(file);
		byte[] bytes = new byte[(int)file.length()];
		int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && 
        		(numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        return bytes;
	}

	//this method copy the data in a byte array into a 3-dimensional integer array
    //first dimension means color, (RGB) 0=R, 1=G, 2=B
    //second dimension means height, 0=first row, 1=second row, and so on
    //third dimension means width, 0=first column, 1=second column, and so on
    //the values range from 0 to 255. 0 means most dark, 255 means most bright
	public static void bytesToArray(byte[] bytes, int[][][] imageArray){
		int height = imageArray[0].length;
		int width = imageArray[0][0].length;
		int index = 0;
        for (int color = 0; color <= 2; color++){
        	for (int i = 0; i <= height - 1; i++){
        		for (int j = 0; j <= width - 1; j++){
        			int x = (int)bytes[index];
        			imageArray[color][i][j] = x + ((x < 0)?256:0); //x = x + 256 if x < 0
        			index++;
        		}
        	}
        }
	}
	
	//this method take inpputImageArray as input
	//rotate and scale then output to outputImageArray
	public static void rotateAndScale
	(int[][][] inputImageArray, int[][][] outputImageArray, double rotation, double scale){
		int inputImageHeight = inputImageArray[0].length;
		int inputImageWidth = inputImageArray[0][0].length;
		int input_origin_x = inputImageWidth/2;
		int input_origin_y = inputImageHeight/2;
		int outputImageHeight = outputImageArray[0].length;
		int outputImageWidth = outputImageArray[0][0].length;
		int output_origin_x = outputImageWidth/2;
		int output_origin_y = outputImageHeight/2;
		for (int color = 0; color <= 2; color++){
        	for (int y = 0; y <= outputImageHeight - 1; y++){
        		for (int x = 0; x <= outputImageWidth - 1; x++){
        			//use the rotation formula
        			int x_origin = x - output_origin_x;
        			int y_origin = y - output_origin_y;
        			int x_transformed = (int)((x_origin*Math.cos(rotation)-y_origin*Math.sin(rotation))/scale);
        			int y_transformed = (int)((x_origin*Math.sin(rotation)+y_origin*Math.cos(rotation))/scale);
        			int x_new = x_transformed + input_origin_x;
        			int y_new = y_transformed + input_origin_y;
        			if (x_new >=0 && x_new <= inputImageWidth - 1 &&
        				y_new >= 0 && y_new <= inputImageHeight - 1){ //if the pixel maps to a point on image
        				outputImageArray[color][y][x] = inputImageArray[color][y_new][x_new];
        			}
        			else { //if the pixel maps to a point out of image boundary
        				outputImageArray[color][y][x] = 0;
        			}
        		}
        	}
        } 
	}
	
	//this method do anti-aliasing to input array, and output the result to another array
	public static int[][][] antiAliasing(int[][][] imageArray){
		int height = imageArray[0].length;
		int width = imageArray[0][0].length;
		int[][][] outputImageArray = new int[3][height][width];
		int radius = 1; //radius of the filter
		for (int color = 0; color <= 2; color++){
        	for (int i = 0 + radius; i <= height - 1 - radius; i++){
        		for (int j = 0 + radius; j <= width - 1 - radius; j++){
        			int sum = 0;
        			for (int k = -radius; k <= radius; k++){
        				for (int l = -radius; l <= radius; l++){
        					sum += imageArray[color][i+k][j+l];
        				}
        			}
        			outputImageArray[color][i][j] = (int)(sum/Math.pow((2*radius+1),2));
        		}
        	}
        }
        return outputImageArray;
	}
	
	//this method convert a 3-dimensional image array into a byte array
	public static byte[] arrayToBytes(int[][][] imageArray){
		int height = imageArray[0].length;
		int width = imageArray[0][0].length;
		byte[] bytes = new byte[3*height*width];
		int index = 0;
        for (int color = 0; color <= 2; color++){
        	for (int i = 0; i <= height - 1; i++){
        		for (int j = 0; j <= width - 1; j++){
        			bytes[index] = (byte)imageArray[color][i][j];
        			index++;
        		}
        	}
        }
        return bytes;
	}

	//this method create a BufferedImage object from a byte array
	public static BufferedImage bytesToImg(byte[] bytes, int height, int width){
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
       	int offsetOfG = height * width;
       	int offsetOfB = 2 * height * width;
		int index = 0;
		for(int y = 0; y <= height - 1; y++){
			for(int x = 0; x <= width - 1; x++){
				byte r = bytes[index];
				byte g = bytes[offsetOfG + index];
				byte b = bytes[offsetOfB + index]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x,y,pix);
				index++;
			}
		}
		return img;
	}
	
}//end of HW1
