package com.astrofizzbizz.pixie;

/**
 * @author mcginnis
 *
 */
public class PixieImageFilters 
{
	private static int skeletonPass(PixieImage image, double dthreshold)
	{
		int		ii;
		int		ij;
		int		ik;
		PixieImage tempImage = new PixieImage(image);
		double[][] doutpix = null;
		double[][] dimage = null;
		int		inum_pixel_cut;
		int[]	ipix = new int[11];
		int		isum;
		int		istep;
		Pixie	pmax = new Pixie();
		
		dimage = image.getPix();
		doutpix = tempImage.getPix();
		pmax.setRow(image.getRowCount());
		pmax.setCol(image.getColCount());

		inum_pixel_cut = 0;
		for (ii = 0; ii < pmax.getRow(); ii++)
		{  
			for (ij = 0; ij < pmax.getCol(); ij++) 
			{

				if (dimage[ii][ij] < dthreshold) 
				{
					dimage[ii][ij] = 0.0;
					doutpix[ii][ij] = 0.0;
				}
			}
		}

		for (ii = 1; ii < pmax.getRow() - 1; ii++)
		{  
			for (ij = 1; ij < pmax.getCol() - 1; ij++) 
			{
				if (dimage[ii][ij] >= dthreshold) 
				{
					for (ik = 0; ik <= 10; ik++) ipix[ik] = 0;
					if (dimage[ii][ij]         >= dthreshold) ipix[1] = 1;
					if (dimage[ii - 1][ij]     >= dthreshold) ipix[2] = 1;
					if (dimage[ii - 1][ij + 1] >= dthreshold) ipix[3] = 1;
					if (dimage[ii][ij + 1]     >= dthreshold) ipix[4] = 1;
					if (dimage[ii + 1][ij + 1] >= dthreshold) ipix[5] = 1;
					if (dimage[ii + 1][ij]     >= dthreshold) ipix[6] = 1;
					if (dimage[ii + 1][ij - 1] >= dthreshold) ipix[7] = 1;
					if (dimage[ii][ij - 1]     >= dthreshold) ipix[8] = 1;
					if (dimage[ii - 1][ij - 1] >= dthreshold) ipix[9] = 1;
					if (dimage[ii - 1][ij]     >= dthreshold) ipix[10] = 1;
					isum = 0;
					for (ik = 2; ik <= 9; ik++) isum = isum + ipix[ik];
					if ((2 <= isum) && (isum <= 6)) 
					{
						istep = 0;
						for (ik = 2; ik <= 9; ik++) 
						{
							if ((ipix[ik] == 0) && (ipix[ik + 1] == 1)) 
								istep = istep + 1;
						}
						if (istep == 1) 
						{
							if ((ipix[2] * ipix[4] * ipix[6]) == 0)
							{
								if ((ipix[4] * ipix[6] * ipix[8]) == 0)
								{
									doutpix[ii][ij] = 0.0;
									inum_pixel_cut = inum_pixel_cut + 1;
								}
							}
						}
					}
				}
			}

		}
		for (ii = 0; ii < pmax.getRow(); ii++)
		{  
			for (ij = 0; ij < pmax.getCol(); ij++) 
			{
				dimage[ii][ij] = doutpix[ii][ij];
			}
		}
		for (ii = 1; ii < pmax.getRow() - 1; ii++)
		{  
			for (ij = 1; ij < pmax.getCol() - 1; ij++) 
			{
				if (doutpix[ii][ij] >= dthreshold) 
				{
					for (ik = 0; ik <= 10; ik++) ipix[ik] = 0;
					if (doutpix[ii][ij]         >= dthreshold) ipix[1] = 1;
					if (doutpix[ii - 1][ij]     >= dthreshold) ipix[2] = 1;
					if (doutpix[ii - 1][ij + 1] >= dthreshold) ipix[3] = 1;
					if (doutpix[ii][ij + 1]     >= dthreshold) ipix[4] = 1;
					if (doutpix[ii + 1][ij + 1] >= dthreshold) ipix[5] = 1;
					if (doutpix[ii + 1][ij]     >= dthreshold) ipix[6] = 1;
					if (doutpix[ii + 1][ij - 1] >= dthreshold) ipix[7] = 1;
					if (doutpix[ii][ij - 1]     >= dthreshold) ipix[8] = 1;
					if (doutpix[ii - 1][ij - 1] >= dthreshold) ipix[9] = 1;
					if (doutpix[ii - 1][ij]     >= dthreshold) ipix[10] = 1;
					isum = 0;
					for (ik = 2; ik <= 9; ik++) isum = isum + ipix[ik];
					if ((2 <= isum) && (isum <= 6)) 
					{
						istep = 0;
						for (ik = 2; ik <= 9; ik++) 
						{
							if ((ipix[ik] == 0) && (ipix[ik + 1] == 1)) 
								istep = istep + 1;
						}
						if (istep == 1) 
						{
							if ((ipix[2] * ipix[4] * ipix[8]) == 0)
							{
								if ((ipix[2] * ipix[6] * ipix[8]) == 0)
								{
									dimage[ii][ij] = 0.0;
									inum_pixel_cut = inum_pixel_cut + 1;
								}
							}
						}
					}
				}
			}
		}
		tempImage = null;
		return inum_pixel_cut;
	}
	/**
	 * @param inputImage
	 * @param dthreshold
	 * @return PixieImage
	 */
	public static PixieImage skeletonFilter(PixieImage inputImage, double dthreshold)
	{
		PixieImage outputImage = new PixieImage(inputImage);
		int inum_pixel_cut = 1;
		while (inum_pixel_cut > 0)
		{
			inum_pixel_cut = skeletonPass(outputImage, dthreshold);
		}
		return outputImage;
	}
}