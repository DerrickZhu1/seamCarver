/*************************************************************************
 *  Compilation:  javac ResizeDemo.java
 *  Execution:    java ResizeDemo input.png columnsToRemove rowsToRemove
 *  Dependencies: SeamCarver.java SCUtility.java Picture.java Stopwatch.java
 *                StdDraw.java
 *                
 *
 *  Read image from file specified as command line argument. Use SeamCarver
 *  to remove number of rows and columns specified as command line arguments.
 *  Show the images in StdDraw and print time elapsed to screen.
 *
 *************************************************************************/

public class ResizeDemo {
    public static void main(String[] args)
    {
    	/*
        if (args.length != 3)
        {
            System.out.println("Usage:\njava ResizeDemo [image filename] [num cols to remove] [num rows to remove]");
            return;
        }
		*/
    	String img = "img/HJocean.png";
    	
        Picture inputImg = new Picture(img);
        int removeColumns = 75;
        int removeRows = 50; 

        System.out.printf("image is %d columns by %d rows\n", inputImg.width(), inputImg.height());
        SeamCarver sc = new SeamCarver(inputImg);

        Stopwatch sw = new Stopwatch();

        for (int i = 0; i < removeRows; i++) {
            int[] horizontalSeam = sc.findHorizontalSeam();
            sc.removeHorizontalSeam(horizontalSeam);
        }
        
        for (int i = 0; i < removeColumns; i++) 
        {
        	try
        	{
	            int[] verticalSeam = sc.findVerticalSeam();
	            sc.removeVerticalSeam(verticalSeam);
        	}
        	catch(IllegalArgumentException ex)
        	{
        		System.out.println(ex.getMessage());
        	}
        	
        }
        
        Picture outputImg = sc.picture();

        System.out.printf("new image size is %d columns by %d rows\n", sc.width(), sc.height());

        System.out.println("Resizing time: " + sw.elapsedTime() + " seconds.");
        inputImg.show();
        outputImg.show();
    }
    
}
