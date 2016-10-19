package com.astrofizzbizz.pixie;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.astrofizzbizz.numericalrecipes.NR;


/**
 * @author mcginnis
 *
 */
public class PixiePuddle 
{
	/**
	 * 
	 */
	Map <String, Pixie> puddleMap = null;
	Map <String, Pixie> borderMap = null;
	/**
	 * 
	 */
	public PixiePuddle()
	{
		puddleMap = new HashMap<String, Pixie>();
		borderMap = new HashMap<String, Pixie>();
	}
	/**
	 * @param pc
	 */
	@SuppressWarnings("rawtypes")
	public PixiePuddle(PixiePuddle pc)
	{
		puddleMap = new HashMap<String, Pixie>();
		borderMap = new HashMap<String, Pixie>();
		
		Iterator keys = pc.puddleMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie p1 = pc.puddleMap.get(currentKey);
			puddleMap.put(p1.getKey(), new Pixie(p1));
		}
		keys = pc.borderMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie p1 = pc.borderMap.get(currentKey);
			borderMap.put(p1.getKey(), new Pixie(p1));
		}
	}
	/**
	 * Copies image pixies from PixieImage pi to coordinates of the PixiePuddle
	 * and places the pixel value in the color array of the pixie
	 * @param pi
	 * @param icolor 
	 */
	@SuppressWarnings("rawtypes")
	public void addPixieColor(PixieImage pi, int icolor)
	{
		double[][]	dpix = pi.getPix();
		
		Iterator keys = puddleMapKeyIterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie pcPixie = getPuddlePixie(currentKey);
			if ((pcPixie.getRow() < pi.getRowCount()) 
					&& (pcPixie.getCol() < pi.getColCount()))
			{
				double val = dpix[pcPixie.getRow()][pcPixie.getCol()];
				pcPixie.setColorVal(icolor, val);
				puddleMap.put(currentKey, new Pixie(pcPixie));
			}
		}
		keys = borderMapKeyIterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie pcPixie = getBorderPixie(currentKey);
			if ((pcPixie.getRow() < pi.getRowCount()) 
					&& (pcPixie.getCol() < pi.getColCount()))
			{
				double val = dpix[pcPixie.getRow()][pcPixie.getCol()];
				pcPixie.setColorVal(icolor, val);
				borderMap.put(currentKey, new Pixie(pcPixie));
			}
		}
	}
	/**
	 * @return PixiePuddle
	 */
	@SuppressWarnings("rawtypes")
	public PixiePuddle makeCopy()
	{
		PixiePuddle pdCopy = new PixiePuddle();
		Iterator keys = puddleMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie pcopy = puddleMap.get(currentKey);
			pdCopy.puddleMap.put(currentKey, pcopy );
		}
		keys = borderMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie pcopy = borderMap.get(currentKey);
			pdCopy.borderMap.put(currentKey, pcopy );
		}
		return pdCopy; 
	}
	@SuppressWarnings("rawtypes")
	private boolean checkMapKeys(Map <String, Pixie> map, String message)
	{
		boolean goodKeys = true;
		Iterator keys = map.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie pcopy = map.get(currentKey);
			if (!pcopy.getCoord().checkKey(currentKey))
			{
				System.out.println(message + "Bad Key: Key=" + currentKey + " Coord=" + pcopy.getKey());
				goodKeys = false;
			}
		}
		return goodKeys;
	}
	/**
	 * @param pseed
	 */
	private int checkForAdjacentPixies(Pixie pseed)
	{
		Pixie ptest = puddleMap.get(pseed.getKey());
		if (ptest == null) return -1;
		int isum = 0;
		for (int ii = -1; ii <= 1; ++ii)
		{
			for (int ij = -1; ij <= 1; ++ij)
			{
				boolean centerPixie = (ii == 0 ) && (ij == 0); 
				if (!centerPixie)
				{
					PixieCoord ctest = new PixieCoord(pseed.getRow() + ii, pseed.getCol() + ij);
					ptest = puddleMap.get(ctest.getKey());
					if (ptest != null) isum = isum + 1;
				}
			}
		}
		return isum;
	}
	private void addPixie(Pixie padd)
	{
		puddleMap.put(padd.getKey(), new Pixie(padd));
		int[][]	isumMatrix = new int[3][3];
		Pixie[][] pmatrix = new Pixie[3][3];
		for (int ii = -1; ii <= 1; ++ii)
		{
			for (int ij = -1; ij <= 1; ++ij)
			{
				isumMatrix[ii + 1][ij + 1] = -1;
				PixieCoord ctest = new PixieCoord(padd.getRow() + ii, padd.getCol() + ij);
				if (puddleMap.containsKey(ctest.getKey()))
				{
					pmatrix[ii + 1][ij + 1] = puddleMap.get(ctest.getKey());
					isumMatrix[ii + 1][ij + 1] = checkForAdjacentPixies(pmatrix[ii + 1][ij + 1]);
				}
			}
		}
		for (int ii = -1; ii <= 1; ++ii)
		{
			for (int ij = -1; ij <= 1; ++ij)
			{
				if (isumMatrix[ii + 1][ij + 1] == 8)
				{
					if (borderMap.containsKey(pmatrix[ii + 1][ij + 1].getKey()))
					{
						borderMap.remove(pmatrix[ii + 1][ij + 1].getKey());
					}
				}
				else
				{
					if (isumMatrix[ii + 1][ij + 1] >= 0) 
						borderMap.put(pmatrix[ii + 1][ij + 1].getKey(), new Pixie(pmatrix[ii + 1][ij + 1]));
				}
			}
		}
	}
	private void removePixie(PixieCoord cremove)
	{
		if (!puddleMap.containsKey(cremove.getKey())) return;
		puddleMap.remove(cremove.getKey());
		if (borderMap.containsKey(cremove.getKey()))
			borderMap.remove(cremove.getKey());
		for (int ii = -1; ii <= 1; ++ii)
		{
			for (int ij = -1; ij <= 1; ++ij)
			{
				boolean centerPixie = (ii == 0 ) && (ij == 0);
				if (!centerPixie)
				{
					PixieCoord ctest = new PixieCoord(cremove.getRow() + ii, cremove.getCol() + ij);
					if (puddleMap.containsKey(ctest.getKey()))
					{
						Pixie pborder = puddleMap.get(ctest.getKey());
						borderMap.put(pborder.getKey(), new Pixie(pborder));
					}
				}
			}
		}
	}
	/**
	 * Makes a non-contigous puddle from PixieImage pimage
	 * @param pimage
	 * @param dthreshold
	 * @param dmax_threshold
	 */
	public PixiePuddle(PixieImage pimage, double dthreshold, double dmax_threshold)
	{
		puddleMap = new HashMap<String, Pixie>();
		borderMap = new HashMap<String, Pixie>();
		
		double[][] dpix = pimage.getPix();
		for (int ii = 0; ii < pimage.getRowCount(); ++ii)
		{
			for (int ij = 0; ij < pimage.getColCount(); ++ij)
			{
				if ((dthreshold <= dpix[ii][ij]) && (dpix[ii][ij] <= dmax_threshold))
				{
					PixieCoord cadd = new PixieCoord(ii,ij);
					cadd.setRaDec(pimage.getHeader());
					addPixie(new Pixie(cadd, dpix[ii][ij]));
				}
			}
		}
		return;
	}
	/**
	 * @param pi
	 */
	@SuppressWarnings("rawtypes")
	public void removeFromPixieImage(PixieImage pi)
	{
		double[][]	dpix = pi.getPix();
		Iterator keys = puddleMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie premove = puddleMap.get(currentKey);
			if ((premove.getRow() < pi.getRowCount()) && (premove.getCol() < pi.getColCount()))
			{
				dpix[premove.getRow()][premove.getCol()] = 0.0;
			}
		}
	}
	@SuppressWarnings("rawtypes")
	private void addToExcludeMap(Map <String, Pixie> excludeMap)
	{
		Iterator keys = puddleMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie p1 = puddleMap.get(currentKey);
			excludeMap.put(p1.getKey(), new Pixie(p1));
		}
		
	}
	/**
	 * @param pc2
	 */
	@SuppressWarnings("rawtypes")
	public void addPuddle(PixiePuddle pc2)
	{
		Iterator keys = pc2.puddleMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			addPixie(pc2.puddleMap.get(currentKey));
		}
		return;
	}
	/**
	 * @param pc2
	 */
	@SuppressWarnings("rawtypes")
	public  void subtractPuddle(PixiePuddle pc2)
	{
		Iterator keys = pc2.puddleMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie premove = pc2.puddleMap.get(currentKey);
			removePixie(premove.getCoord());
		}
		return;

	}
	/**
	 * Returns a PixieImage where the minimum Pixie of the puddle 
	 * is at row=0, col=0 of the PixieImage
     * @param pmin
	 * @param pmax
	 * @param autofit
	 * @return PixieImage
	 */
	@SuppressWarnings("rawtypes")
	public PixieImage makePixieImage(PixieCoord pmin, PixieCoord pmax, boolean autofit )
	{
		if (autofit)
		{
			pmax.setRow(0);
			pmax.setCol(0);
			Iterator keys = puddleMap.keySet().iterator();
			while (keys.hasNext())
			{
				String currentKey = (String) keys.next();
				Pixie ptest = puddleMap.get(currentKey);
				if (pmax.getRow() < ptest.getRow()) pmax.setRow(ptest.getRow());
				if (pmax.getCol() < ptest.getCol()) pmax.setCol(ptest.getCol());
			}
			pmin.setRow(pmax.getRow());
			pmin.setCol(pmax.getCol());
			keys = puddleMap.keySet().iterator();
			while (keys.hasNext())
			{
				String currentKey = (String) keys.next();
				Pixie ptest = puddleMap.get(currentKey);
				if (pmin.getRow() > ptest.getRow()) pmin.setRow(ptest.getRow());
				if (pmin.getCol() > ptest.getCol()) pmin.setCol(ptest.getCol());
			}
		}
		PixieImage pi = new PixieImage(pmax.getRow() - pmin.getRow() + 1, pmax.getCol() - pmin.getCol() + 1);
		double[][] dpix = pi.getPix();
		Iterator keys = puddleMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie ptest = puddleMap.get(currentKey);
			if ((ptest.getRow() <= pmax.getRow()) && (ptest.getCol() <= pmax.getCol()))
			{
				if ((ptest.getRow() >= pmin.getRow()) && (ptest.getCol() >= pmin.getCol()))
				{
					dpix[ptest.getRow() - pmin.getRow()][ptest.getCol() - pmin.getCol()] = ptest.getCompVal();
				}
			}
		}
		return pi;
	}
	/**
	 * @param pi
	 * @param borderValue
	 */
	@SuppressWarnings("rawtypes")
	public void drawPuddleBorder(PixieImage pi, double borderValue)
	{
		Pixie pmax = pi.getURHCPixie();
		Iterator keys = borderMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie p = new Pixie(borderMap.get(currentKey));
			p.setCompVal( borderValue );
			if ((p.getRow() <= pmax.getRow()) && (p.getCol() <= pmax.getCol()))
			{
				pi.setPixieValue(p);
			}
		}
	}
	/**
	 * @param pcList
	 * @param pmax
	 * @param borderValue
	 * @return pi
	 */
	public static PixieImage drawPuddleBorders(ArrayList<PixiePuddle> pcList, PixieCoord pmax, double borderValue)
	{
		PixieImage pi = new PixieImage(pmax);
		
		for (int ii = 0; ii < pcList.size(); ++ii)
		{
			PixiePuddle pc = pcList.get(ii);
			pc.drawPuddleBorder(pi, borderValue);
		}
		return pi;
	}
	/**
	 * makes an Pixie image in which each puddle is defined as a number
	 * used as a diagnostic to see how the puddles look
	 * @param pcList
	 * @param pmax
	 * @param randomCount 
	 * @param scale 
	 * @return PixieImage
	 */
	@SuppressWarnings("rawtypes")
	public static PixieImage puddleCountImage(ArrayList<PixiePuddle> pcList, PixieCoord pmax, boolean randomCount, double scale)
	{
		PixieImage pi = new PixieImage(pmax);
		double pixieValue;
		
		double[] randomVec = new double[10];
		randomVec[0] = 10.0;
		randomVec[1] = 4.0;
		randomVec[2] = 7.0;
		randomVec[3] = 2.0;
		randomVec[4] = 5.0;
		randomVec[5] = 8.0;
		randomVec[6] = 3.0;
		randomVec[7] = 6.0;
		randomVec[8] = 9.0;
		randomVec[9] = 1.0;
		int icount = 0;
		
		for (int ii = 0; ii < pcList.size(); ++ii)
		{
			if (randomCount)
			{
				pixieValue =  scale * randomVec[icount] / 10.0;
				icount = icount + 1;
				if (icount == 10) icount = 0;
			}
			else
			{
				pixieValue = scale * ((double) (ii + 1)) / ((double) pcList.size());
			}
			PixiePuddle pc = pcList.get(ii);
			Iterator keys = pc.puddleMap.keySet().iterator();
			while (keys.hasNext())
			{
				String currentKey = (String) keys.next();
				Pixie p = new Pixie(pc.puddleMap.get(currentKey));
				p.setCompVal(pixieValue);
				if ((p.getRow() <= pmax.getRow()) && (p.getCol() <= pmax.getCol()))
				{
					pi.setPixieValue(p);
				}
			}
		}
		return pi;
	}
	/**
	 * @param pcList
	 * @return number of pixies
	 */
	public static int countNumPixiesInList(ArrayList<PixiePuddle> pcList)
	{
		int	inumPixies = 0;
		for (int ii = 0; ii < pcList.size(); ++ii)
		{
			inumPixies = inumPixies + pcList.get(ii).getNumPixies();
		}
		return inumPixies;
	}
	/**
	 * @param excludeMap 
	 * @return Pixie
	 */
	@SuppressWarnings("rawtypes")
	public Pixie minValPixie(Map <String, Pixie> excludeMap)
	{
		Pixie pmin = new Pixie();
		Iterator keys = puddleMap.keySet().iterator();
		boolean startPixieFound = false;
		while (keys.hasNext() && !startPixieFound)
		{
			String currentKey = (String) keys.next();
			if (!excludeMap.containsKey(currentKey))
			{
				pmin = new Pixie(puddleMap.get(currentKey));
				startPixieFound = true;
			}
		}
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			if (!excludeMap.containsKey(currentKey))
			{
				Pixie ptest = puddleMap.get(currentKey);
				if (pmin.getCompVal() > ptest.getCompVal())
				{
					pmin = new Pixie(ptest);
				}
			}
		}
		return pmin;
	}
	/**
	 * @return Pixie
	 */
	public Pixie minValPixie()
	{
		return minValPixie(new HashMap<String, Pixie> ());
	}
	/**
	 * @param excludeMap 
	 * @param border 
	 * @return Pixie
	 */
	@SuppressWarnings("rawtypes")
	public Pixie maxValPixie (Map <String, Pixie> excludeMap, boolean border)
	{
		Pixie pmax = new Pixie();
		Map <String, Pixie> lookAtMap = null;
		if (border)
		{
			lookAtMap = borderMap;
		}
		else
		{
			lookAtMap = puddleMap;
		}
		Iterator keys = lookAtMap.keySet().iterator();
		boolean startPixieFound = false;
		while (keys.hasNext() && !startPixieFound)
		{
			String currentKey = (String) keys.next();
			if (!excludeMap.containsKey(currentKey))
			{
				pmax = new Pixie(lookAtMap.get(currentKey));
				startPixieFound = true;
			}
		}
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			if (!excludeMap.containsKey(currentKey))
			{
				Pixie ptest = lookAtMap.get(currentKey);
				if (pmax.getCompVal() < ptest.getCompVal())
				{
					pmax = new Pixie(ptest);
				}
			}
		}
		return pmax;
	}
	/**
	 * @param border 
	 * @return Pixie
	 */
	public Pixie maxValPixie(boolean border)
	{
		return maxValPixie(new HashMap<String, Pixie> (), border);
	}
	/**
	 * @return int
	 */
	public int getNumPixies()
	{
		return puddleMap.size();
	}
	@SuppressWarnings("rawtypes")
	private double growAdjacentPixies(Map <String, Pixie> excludeMap, PixiePuddle pcParent, double dthreshold, double dmax_threshold, Map <String, Pixie> foundPixieMap)
	{
		foundPixieMap.clear();
		Iterator keys = borderMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie pi = borderMap.get(currentKey);
			for (int im = -1; im <= 1; ++im)
			{
				for (int in = -1; in <= 1; ++in)
				{
					boolean centerPixie = (im == 0) && (in == 0);
					if ( !centerPixie )
					{
						PixieCoord cx = new PixieCoord(pi.getRow() + im, pi.getCol() + in);
						String cxKey = cx.getKey();
						boolean inPcParent = pcParent.puddleMap.containsKey(cxKey);
						boolean notInPuddleMap = !puddleMap.containsKey(cxKey);
						boolean notInExcludeMap = !excludeMap.containsKey(cxKey);
						boolean okayPixie = inPcParent & notInPuddleMap & notInExcludeMap;
						if (okayPixie)
						{
							Pixie px = pcParent.puddleMap.get(cxKey);
							if (px.getCompVal() > dthreshold)
							{
								if (dmax_threshold < 0.0)
								{
									foundPixieMap.put(px.getKey(), new Pixie(px));
								}
								else
								{
									if (px.getCompVal() <= dmax_threshold)
									{
										foundPixieMap.put(px.getKey(), new Pixie(px));
									}
								}	
							}
						}
					}
				}
			}
		}
		double dminPixieValueFound = 1.0e+33;
		keys = foundPixieMap.keySet().iterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie foundPixie = foundPixieMap.get(currentKey);
			if (dminPixieValueFound > foundPixie.getCompVal()) dminPixieValueFound = foundPixie.getCompVal();
			addPixie(foundPixie);
		}
		return dminPixieValueFound;
	}
	private void growRing(Map <String, Pixie> excludeMap, PixiePuddle pcParent, double dthreshold, double dmax_threshold)
	{
		Map <String, Pixie> foundPixieMap = new HashMap<String, Pixie>();
		growAdjacentPixies(excludeMap, pcParent, dthreshold, dmax_threshold, foundPixieMap);
		int addedPixies = foundPixieMap.size();
		while (foundPixieMap.size() > 0)
		{
			growAdjacentPixies(excludeMap, pcParent, dthreshold, dmax_threshold, foundPixieMap);
			addedPixies = addedPixies + foundPixieMap.size();
		}
		return;
	}
	/**
	 * @param pi
	 * @param dthreshold
	 * @return ArrayList<PixiePuddle>
	 */
	public static ArrayList<PixiePuddle> findBlendedPixiePuddles(PixieImage pi, double dthreshold)
	{
		ArrayList<PixiePuddle> pcList = new ArrayList<PixiePuddle>();
		PixiePuddle pcParent = new PixiePuddle(pi, dthreshold, 1.0e+33);
		int		pcListIndex = 0;
		Pixie pseed = pcParent.maxValPixie(false);
		Map <String, Pixie> excludeMap = new HashMap<String, Pixie>();
		
		while (pseed.getCompVal() >= dthreshold)
		{
			PixiePuddle pc = new PixiePuddle();
			pc.addPixie(pseed);
			pc.growRing(excludeMap, pcParent, dthreshold, 1.0e+33);
			pcList.add(pcListIndex, (PixiePuddle) pc);
			pcListIndex = pcListIndex + 1;
			pc.addToExcludeMap(excludeMap);
			pseed = pcParent.maxValPixie(excludeMap, false);
		}
		return pcList;
	}
	/**
	 * @param dthreshold
	 * @param iminPixelDistance
	 * @param iminNumOfPixies 
	 * @return ArrayList<PixiePuddle>
	 */
	public ArrayList<PixiePuddle> deblend(double dthreshold, int iminPixelDistance, int iminNumOfPixies)
	{
		ArrayList<PixiePuddle> pcList = new ArrayList<PixiePuddle>();
		if (getNumPixies() < iminNumOfPixies)
		{
			pcList.add(0, makeCopy());
			return pcList;
		}
		pcList.add(0, new PixiePuddle());
		Pixie pseed = maxValPixie(false);
		pcList.get(0).addPixie(pseed);
		Map <String, Pixie> excludeMap = new HashMap<String, Pixie>();
		pcList.get(0).addToExcludeMap(excludeMap);
		PixiePuddle origPuddle = makeCopy();
		checkMapKeys(origPuddle.puddleMap,"origPuddle");
		checkMapKeys(origPuddle.borderMap,"origPuddle");
		
		double dthresholdAllRings = 1.0e+33;
		int inumOfPixiesOld = 0; countNumPixiesInList(pcList);
		int inumOfPixiesNew = 1; countNumPixiesInList(pcList);
		
		while ((inumOfPixiesNew - inumOfPixiesOld) > 0)
		{
			dthresholdAllRings = 1.0e+33;
			excludeMap.clear();
			for (int icol = 0; icol < pcList.size(); ++icol)
			{
				PixiePuddle pcTest = new PixiePuddle(pcList.get(icol));
				Map <String, Pixie> foundPixieMap = new HashMap<String, Pixie>();
				double dminPixieValueFound = pcTest.growAdjacentPixies(excludeMap, origPuddle, dthreshold, 1.0e+33, foundPixieMap);
// Find the min value of all the ring thresholds
				if (dthresholdAllRings > dminPixieValueFound) dthresholdAllRings = dminPixieValueFound;

			}
			ArrayList<PixiePuddle> pcListOldCopy = new ArrayList<PixiePuddle>();
			pcListOldCopy.clear();
			
			for (int icol = 0; icol < pcList.size(); ++icol)
			{
				pcListOldCopy.add(icol, new PixiePuddle(pcList.get(icol)));
			}
			for (int icol = 0; icol < pcList.size(); ++icol)
			{
// Go back and find rings with new threshold so that rings grow at the same rate
// Need to remove the puddle that the seed is growing around from hte exclude list.
				excludeMap.clear();
				for (int jcol = 0; jcol < pcList.size(); ++jcol)
				{
					if (icol != jcol)
					{
						pcListOldCopy.get(jcol).addToExcludeMap(excludeMap);
					}
				}
				pcList.get(icol).growRing(excludeMap, origPuddle, dthresholdAllRings, 1.0e+33);
			}
			removeSharedPixels(pcListOldCopy, pcList);
/*
			PixieImage pi = PixiePuddle.puddleCountImage(pcList, getURHCoord());
			pi.quickWriteToFitsFile("objnew.fits");
			pi = PixiePuddle.puddleCountImage(pcListOldCopy, getURHCoord());
			pi.quickWriteToFitsFile("objold.fits");
*/
			excludeMap.clear();
			for (int icol = 0; icol < pcList.size(); ++icol)
			{
				pcList.get(icol).addToExcludeMap(excludeMap);
			}
// Look for new peaks to be new puddles
			PixiePuddle pcCopy = new PixiePuddle(origPuddle);
			pseed = pcCopy.maxValPixie(excludeMap, false);
			while (pseed.getCompVal() > dthresholdAllRings)
			{
				int imin = pcList.get(0).findClosestSquareDistanceToPuddleEdge(pseed.getCoord());
				for (int icol = 0; icol < pcList.size(); ++icol)
				{
					int itest = pcList.get(icol).findClosestSquareDistanceToPuddleEdge(pseed.getCoord());
					if (imin > itest) imin = itest;
				}
				if (imin > (iminPixelDistance * iminPixelDistance))
				{
					PixiePuddle newPc = new PixiePuddle();
					newPc.addPixie(pseed);
					newPc.growRing(excludeMap, origPuddle, dthresholdAllRings, 1.0e+33);
					pcList.add(newPc);
					newPc.addToExcludeMap(excludeMap);

// Make sure to account that new pixies have been found					
				}
				else
				{
					pseed.setCompVal(0.0);
					pcCopy.removePixie(pseed.getCoord());
				}
				pseed = pcCopy.maxValPixie(false);
			}
			inumOfPixiesOld = inumOfPixiesNew;
			inumOfPixiesNew = countNumPixiesInList(pcList);
		}
		return pcList;
	}
	@SuppressWarnings("rawtypes")
	private int findClosestSquareDistanceToPuddleEdge(PixieCoord ctest)
	{
		Iterator keys = borderMap.keySet().iterator();
		String kBorder = "";
		PixieCoord cBorder = new PixieCoord();
		if (keys.hasNext())
		{
			kBorder = (String) keys.next();
			cBorder = borderMap.get(kBorder).getCoord();
		}
		else
		{
			return -1;
		}

		int 	imin = (ctest.getRow() - cBorder.getRow()) * (ctest.getRow() - cBorder.getRow())
			 		 + (ctest.getCol() - cBorder.getCol()) * (ctest.getCol() - cBorder.getCol());
		while (keys.hasNext())
		{
			kBorder = (String) keys.next();
			cBorder = borderMap.get(kBorder).getCoord();
			int itest = (ctest.getRow() - cBorder.getRow()) * (ctest.getRow() - cBorder.getRow())
	 		 		  + (ctest.getCol() - cBorder.getCol()) * (ctest.getCol() - cBorder.getCol());
			if (itest < imin) imin = itest;
		}
		return imin;
	}
	@SuppressWarnings("rawtypes")
	private void removeSharedPixels(ArrayList<PixiePuddle> pcListOldCopy, ArrayList<PixiePuddle> pcList)
	{
		if (pcList.size() != pcListOldCopy.size()) return;
		for (int icol = 0; icol < pcList.size(); ++icol)
		{
			Iterator keyTest = pcList.get(icol).puddleMap.keySet().iterator();
			while (keyTest.hasNext())
			{
				String testKey = (String) keyTest.next();
				if (pcList.get(icol).puddleMap.containsKey(testKey))
				{
					PixieCoord ctest = pcList.get(icol).puddleMap.get(testKey).getCoord();
					int itestDist = pcListOldCopy.get(icol).findClosestSquareDistanceToPuddleEdge(ctest);
					for (int jcol = 0; jcol < pcList.size(); ++jcol)
					{
						if (icol != jcol)
						{
							if ( pcList.get(jcol).puddleMap.containsKey(testKey) )
							{
								int icheckDist = pcListOldCopy.get(jcol).findClosestSquareDistanceToPuddleEdge(ctest);
								if (icheckDist >= itestDist)
								{
									pcList.get(jcol).removePixie(ctest);
								}
							}
						}
					}
				}
			}
		}
		
	}
	/**
	 * @param pllhc
	 * @param purhc
	 * @param fitsFileName
	 */
	public void writetoFitsFile(PixieCoord pllhc, PixieCoord purhc, String fitsFileName)
	{
		PixieImage piTestImage = makePixieImage(pllhc, purhc, false);
		try 
		{
			piTestImage.writeToFitsFile(fitsFileName);
		} 
		catch (PixieImageException e) 
		{
			e.printStackTrace();
		}

	}
	/**
	 * @return Coord
	 */
	@SuppressWarnings("rawtypes")
	public PixieCoord getURHCoord()
	{
		Iterator keys = puddleMap.keySet().iterator();
		PixieCoord cmax = new PixieCoord();
		if (keys.hasNext())
		{
			String kmax = (String) keys.next();
			cmax = new PixieCoord(puddleMap.get(kmax).getCoord());
		}
		while(keys.hasNext())
		{
			String nextKey = (String) keys.next();
			PixieCoord nextCoord = puddleMap.get(nextKey).getCoord();
			if (cmax.getRow() < nextCoord.getRow()) cmax.setRow(nextCoord.getRow());
			if (cmax.getCol() < nextCoord.getCol()) cmax.setCol(nextCoord.getCol());
			
		}
		return cmax;	
	}
	/**
	 * @return Coord
	 */
	@SuppressWarnings("rawtypes")
	public PixieCoord getLLHCoord()
	{
		Iterator keys = puddleMap.keySet().iterator();
		PixieCoord cmin = new PixieCoord();
		if (keys.hasNext())
		{
			String kmin = (String) keys.next();
			cmin = new PixieCoord(puddleMap.get(kmin).getCoord());
		}
		while(keys.hasNext())
		{
			String nextKey = (String) keys.next();
			PixieCoord nextCoord = puddleMap.get(nextKey).getCoord();
			if (cmin.getRow() > nextCoord.getRow()) cmin.setRow(nextCoord.getRow());
			if (cmin.getCol() > nextCoord.getCol()) cmin.setCol(nextCoord.getCol());
			
		}
		return cmin;	
	}
	/**
	 * @return Iterator
	 */
	@SuppressWarnings("rawtypes")
	public Iterator puddleMapKeyIterator()
	{
		Iterator keys = puddleMap.keySet().iterator();
		return keys;
	}
	/**
	 * @return Iterator
	 */
	@SuppressWarnings("rawtypes")
	public Iterator borderMapKeyIterator()
	{
		Iterator keys = borderMap.keySet().iterator();
		return keys;
	}
	/**
	 * @param key
	 * @return Pixie
	 */
	public Pixie getPuddlePixie(String key)
	{
		Pixie p1 = puddleMap.get(key);
		return p1;
	}
	/**
	 * @param pc
	 * @return Pixie
	 */
	public Pixie getPuddlePixie(PixieCoord pc)
	{
		Pixie p1 = puddleMap.get(pc.getKey());
		return p1;
	}
	/**
	 * @param key
	 * @return border pixie
	 */
	public Pixie getBorderPixie(String key)
	{
		Pixie p1 = borderMap.get(key);
		return p1;
	}
	/**
	 * @param pc
	 * @return Pixie
	 */
	public Pixie getBorderPixie(PixieCoord pc)
	{
		Pixie p1 = borderMap.get(pc.getKey());
		return p1;
	}
	/**
	 * @param piComposite
	 * @param piGBand
	 * @param piRBand
	 * @param piIBand
	 * @param dthreshold
	 * @param pixieResolution
	 * @param minNumPixiesInPuddle
	 * @param minPuddleDepth 
	 * @return ArrayList
	 */
	public static ArrayList<PixiePuddle> findAndDeblendPixiePuddles(
			PixieImage piComposite, 
			PixieImage piGBand, 
			PixieImage piRBand, 
			PixieImage piIBand, 
			double dthreshold, int pixieResolution, int minNumPixiesInPuddle,
			double minPuddleDepth)
	{
		ArrayList<PixiePuddle> pcList = findBlendedPixiePuddles(piComposite, dthreshold);
		ArrayList<PixiePuddle> pcListDeblended = new ArrayList<PixiePuddle>();

		for (int ipud = 0; ipud < pcList.size(); ++ipud)
		{
//			pcList.get(ipud).medianFilter();
//			pcList.get(ipud).gaussianFilter();
			ArrayList<PixiePuddle> pcSubList = pcList.get(ipud).deblend(dthreshold, pixieResolution, minNumPixiesInPuddle);
			reBlend(pcSubList, minPuddleDepth);
			for (int isubpud = 0; isubpud < pcSubList.size(); ++isubpud)
			{
				if (pcSubList.get(isubpud).getNumPixies() >= minNumPixiesInPuddle)
				{
					pcListDeblended.add(new PixiePuddle(pcSubList.get(isubpud)));
				}
			}
		}
		for (int ipud = 0; ipud < pcListDeblended.size(); ++ipud)
		{
			if (piGBand != null) pcListDeblended.get(ipud).addPixieColor(piGBand, Pixie.GBAND);
			if (piRBand != null) pcListDeblended.get(ipud).addPixieColor(piRBand, Pixie.RBAND);
			if (piIBand != null) pcListDeblended.get(ipud).addPixieColor(piIBand, Pixie.IBAND);
		}
		return pcListDeblended;
	}
	/**
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public void gaussianFilter()
	{
		PixiePuddle pdCopy = makeCopy();
		double[][] mask = new double[3][3];
		mask[0][0] = 0.0625;
		mask[0][1] = 0.125;
		mask[0][2] = 0.0625;
		mask[1][0] = 0.125;
		mask[1][1] = 0.250;
		mask[1][2] = 0.125;
		mask[2][0] = 0.0625;
		mask[2][1] = 0.125;
		mask[2][2] = 0.0625;
		Iterator keys = puddleMapKeyIterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie p1 = puddleMap.get(currentKey);
			Pixie[][] padjacent = adjacentPixies(p1);
			double val = 0.0;
			double maskSum = 0.0;
			for (int ii = 0; ii <= 2; ++ii)
			{
				for (int ij = 0; ij <= 2; ++ij)
				{
					if (padjacent[ii][ij] != null)
					{
						val = val + padjacent[ii][ij].getCompVal() * mask[ii][ij];
						maskSum = maskSum + mask[ii][ij];
					}
				}
			}
			val = val / maskSum;
			pdCopy.puddleMap.get(currentKey).setCompVal(val);
		}
		keys = puddleMapKeyIterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			double val = pdCopy.puddleMap.get(currentKey).getCompVal();
			puddleMap.get(currentKey).setCompVal(val);
			if (borderMap.containsKey(currentKey)) borderMap.get(currentKey).setCompVal(val);
		}
	}
	/**
	 * Computes median filter
	 */
	@SuppressWarnings("rawtypes")
	public void medianFilter()
	{
		PixiePuddle pdCopy = makeCopy();
		Iterator keys = puddleMapKeyIterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			Pixie p1 = puddleMap.get(currentKey);
			Pixie[][] padjacent = adjacentPixies(p1);
			double[] val = new double[10];
			int	icount = 1;
			for (int ii = 0; ii <= 2; ++ii)
			{
				for (int ij = 0; ij <= 2; ++ij)
				{
					if (padjacent[ii][ij] != null)
					{
						val[icount] = padjacent[ii][ij].getCompVal();
						icount = icount + 1;
					}
				}
			}
			icount = icount - 1;
			double medVal = NR.select(icount / 2 + 1, icount, val);
			pdCopy.puddleMap.get(currentKey).setCompVal(medVal);
		}
		keys = puddleMapKeyIterator();
		while (keys.hasNext())
		{
			String currentKey = (String) keys.next();
			double val = pdCopy.puddleMap.get(currentKey).getCompVal();
			puddleMap.get(currentKey).setCompVal(val);
			if (borderMap.containsKey(currentKey)) borderMap.get(currentKey).setCompVal(val);
		}
	}
	private Pixie[][] adjacentPixies(Pixie pcenter)
	{
		Pixie[][] padjacent = new Pixie[3][3];
		for (int ii = -1; ii <= 1; ++ii)
		{
			for (int ij = -1; ij <= 1; ++ij)
			{
				padjacent[ii + 1][ij + 1] = null;
				PixieCoord pcoord = new PixieCoord(pcenter.getRow() + ii, pcenter.getCol() + ij);
				Pixie ptest = puddleMap.get(pcoord.getKey());
				if (ptest != null)
				{
					padjacent[ii + 1][ij + 1] = new Pixie(ptest);
				}
			}
		}
		return padjacent;
	}
	@SuppressWarnings("rawtypes")
	private Map<String, Pixie> findCommonBorder(PixiePuddle pd2)
	{
		Map<String, Pixie> commonBorder = new HashMap<String, Pixie>();
		Iterator keys = borderMapKeyIterator();
		while (keys.hasNext())
		{
			Pixie pb = getBorderPixie((String) keys.next());
			int idistSquare = pd2.findClosestSquareDistanceToPuddleEdge(pb.getCoord());
			if ( (0 <= idistSquare) && (idistSquare <= 2) )
			{
				commonBorder.put(pb.getKey(), new Pixie(pb));
			}
		}
		return commonBorder;
	}
	@SuppressWarnings("rawtypes")
	private boolean shouldReblend(PixiePuddle pd2, double minPuddleDepth)
	{
		Map<String, Pixie> commonBorder = findCommonBorder(pd2);
		double dborderValue = 0.0;
		double dpuddleValue = 0.0;
		if (commonBorder.size() < 1) return false;

		Iterator keys = commonBorder.keySet().iterator();
		Pixie pborderMax = new Pixie(commonBorder.get(keys.next()));
		while (keys.hasNext())
		{
			Pixie ptest = commonBorder.get(keys.next());
			if (ptest.getCompVal() > pborderMax.getCompVal())
			{
				pborderMax = new Pixie(ptest);
			}
		}
		dborderValue = pborderMax.getCompVal();
		dpuddleValue = medianValueAdjacentPixies(maxValPixie(false));
		double depth = dborderValue / dpuddleValue; 
		if (depth < minPuddleDepth) return false;
		return true;
	}
	/**
	 * @param pdList
	 * @param minPuddleDepth
	 */
	public  static void reBlend(ArrayList<PixiePuddle> pdList, double minPuddleDepth)
	{
		int ipuddleListSize = pdList.size();
		if (ipuddleListSize <= 1) return;
		int ipuddle = 0;
		while (ipuddle < ipuddleListSize)
		{
			int itestPuddle = 0;
			while (itestPuddle < ipuddleListSize)
			{
				if (itestPuddle != ipuddle)
				{
					if( pdList.get(ipuddle).shouldReblend(pdList.get(itestPuddle), minPuddleDepth) )
					{
						pdList.get(ipuddle).addPuddle(pdList.get(itestPuddle));
						pdList.remove(itestPuddle);
						ipuddleListSize = pdList.size();
						itestPuddle = ipuddleListSize;
						ipuddle = -1;
					}
				}
				itestPuddle = itestPuddle + 1;
			}
			ipuddle = ipuddle + 1;
		}
	}
	private double medianValueAdjacentPixies(Pixie pcenter)
	{
		Pixie[][] padjacent = adjacentPixies(pcenter);
		double[] val = new double[10];
		int	icount = 1;
		for (int ii = 0; ii <= 2; ++ii)
		{
			for (int ij = 0; ij <= 2; ++ij)
			{
				if (padjacent[ii][ij] != null)
				{
					val[icount] = padjacent[ii][ij].getCompVal();
					icount = icount + 1;
				}
			}
		}
		icount = icount - 1;
		double medVal = NR.select(icount / 2 + 1, icount, val);
		return medVal;
	}
}