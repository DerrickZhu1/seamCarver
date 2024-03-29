
public class SeamCarver
{
	private Picture picture;
	private double[][] distTo;
	private double[][] energyMatrix;
	private double[][] transEnergyMatrix;
	//stores only columns, no row indication.
	private int[][] edgeTo;
	
	public SeamCarver(Picture picture)
	{
		this.picture = new Picture(picture);
		this.energyMatrix = energyMatrix();
		this.transEnergyMatrix = transposeEnergyMatrix(this.energyMatrix);
	}
	
	private void init(double[][] eMatrix)
	{
		distTo = new double[eMatrix.length][eMatrix[0].length];
		
		for(int i = 0; i < distTo.length; i++)
		{
			for(int j = 0; j < distTo[i].length; j++)
			{
				if(j == 0)
					distTo[i][j] = Math.pow(255, 2) * 3;
				else
					distTo[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		
		this.edgeTo = new int[eMatrix.length][eMatrix[0].length];
	}
	
	public Picture picture()
	{
		return this.picture;
	}
	/**
	 * Getting the width of the picture
	 * @return
	 */
	public int width()
	{
		return this.picture.width();
	}
	
	/**
	 * Getting the height of the picture
	 * @return
	 */
	public int height()
	{
		return this.picture.height();
	}
	
	/**
	 * Getting the energy of the picture
	 * @param x
	 * @param y
	 * @return
	 */
	public double energy(int x, int y) throws IllegalArgumentException
	{
		//if a pixel at the border, then 255^2 + 255^2 + 255^2
		if(x < 0 || y < 0 || x >= width() || y >= height())
			throw new IllegalArgumentException("Bad pixel coordinates: x = " + x + "; y = " + y);
			
		if(x == 0 || x == this.picture.width() - 1 || y == 0 || y == this.height() - 1)
			return Math.pow(255, 2) * 3;
		
		//non-trivial cases
		int r1 = this.picture.get(x - 1, y).getRed();
		int g1 = this.picture.get(x - 1, y).getGreen();
		int b1 = this.picture.get(x - 1, y).getBlue();
		
		int r2 = this.picture.get(x + 1, y).getRed();
		int g2 = this.picture.get(x + 1, y).getGreen();
		int b2 = this.picture.get(x + 1, y).getBlue();
		
		double xenergy = (double)(Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2));
		
		r1 = this.picture.get(x, y - 1).getRed();
		g1 = this.picture.get(x, y - 1).getGreen();
		b1 = this.picture.get(x, y - 1).getBlue();
		
		r2 = this.picture.get(x, y + 1).getRed();
		g2 = this.picture.get(x, y + 1).getGreen();
		b2 = this.picture.get(x, y + 1 ).getBlue();
		
		double yenergy = (double)(Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2));
		
		return xenergy + yenergy;
	}
	
	private double[][] energyMatrix()
	{
		double[][] result = new double[this.picture.width()][this.picture.height()];
		for(int i = 0; i < this.picture.width(); i++)
		{
			for(int j = 0; j < this.picture.height(); j++)
				result[i][j] = energy(i, j);
		}
		return result;
	}
	
	private double[][] transposeEnergyMatrix(double[][] eMatrix)
	{
		double[][] result = new double[eMatrix[0].length][eMatrix.length];
		for(int i = 0; i < eMatrix.length; i++)
		{
			for(int j = 0; j < eMatrix[i].length; j++)
				result[j][i] = eMatrix[i][j];
		}
		
		return result;
	}
	
	public int[] findVerticalSeam()
	{
		init(energyMatrix);
		return findMinSeam(this.energyMatrix);
	}
	
	private int[] findMinSeam(double[][] eMatrix)
	{
		//the matrix is already in topological order (by definition)
		//we need to traverse it top-down and relax all edges 
		//and thus calculate minimum pixel energies in the process
		
		/**/
		for(int j = 0; j < eMatrix[0].length - 1; j++)
		{
			for(int i = 0; i < eMatrix.length; i++)
			{
				//relaxation of edges
				//the neighbor down left
				if(i > 0) 
					relax(i, j, i - 1, j + 1, eMatrix[i - 1][j + 1]);
					
				//the neighbor below
				relax(i, j, i, j + 1, eMatrix[i][j + 1]);
				
				//the neighbor down right
				if(i + 1 < eMatrix.length) 
					relax(i, j, i + 1, j + 1, eMatrix[i + 1][j + 1]);
			}
		}

		return getMinSeam();
	}
	
	private void relax(int x1, int y1, int x2, int y2, double energy)
	{
		if(distTo[x2][y2] > distTo[x1][y1] + energy)
		{
			distTo[x2][y2] = distTo[x1][y1] + energy;
			//record the column of the parent
			edgeTo[x2][y2] = x1;
		}
	}
	
	private int[] getMinSeam()
	{
		//last row
		int y = distTo[0].length - 1;
		//find the min dist on the last row
		int x = 0;
		double minDist = distTo[0][y];
		for(int i = 0; i < distTo.length; i++)
		{
			if(distTo[i][y] < minDist)
			{
				minDist = distTo[i][y];
				x = i;
			}
		}
		//back tracing
		int [] result = new int[y + 1];
		result[y] = x;
		for(int i = y; i > 0; i--)
		{
			x = edgeTo[x][i];
			result[i - 1] = x;
		}
		
		return result;
	}
	
	public int[] findHorizontalSeam()
	{
		init(transEnergyMatrix);
		return findMinSeam(transEnergyMatrix);
	}
	
	public void removeHorizontalSeam(int[] a)
	{
		Picture newPicture = new Picture(this.picture.width(), this.picture.height() - 1);
		for(int i = 0; i < this.picture.width(); i++)
		{
			for(int j = 0; j < this.picture.height(); j++)
			{
				if(j < a[i])
					newPicture.set(i, j, this.picture.get(i, j));
				else if(j > a[i])
					newPicture.set(i, j - 1, this.picture.get(i, j));
			}
		}
		this.energyMatrix = energyMatrix();
		this.transEnergyMatrix = transposeEnergyMatrix(this.energyMatrix());

		this.picture = newPicture;
	}
	
	public void removeVerticalSeam(int[] a)
	{
		Picture newPicture = new Picture(this.picture.width() - 1, this.picture.height());
		for(int i = 0; i < this.picture.width(); i++)
		{
			for(int j = 0; j < this.picture.height(); j++)
			{
				try
				{
					if(i < a[j])
						newPicture.set(i, j, this.picture.get(i, j));
					else if(i > a[j])
						newPicture.set(i - 1, j, this.picture.get(i, j));
				}
				catch(Exception ex)
				{
					throw new IllegalArgumentException("Bad argument! i =  " + i + " j = " + j + " a[j] = " + a[j]);
				}
			}
		}
		
		this.energyMatrix = energyMatrix();
		this.transEnergyMatrix = transposeEnergyMatrix(this.energyMatrix());

		
		this.picture = newPicture;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		System.out.println(Math.pow(255, 2) * 3);
	}
}
