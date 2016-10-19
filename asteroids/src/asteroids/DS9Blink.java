package asteroids;
import java.awt.GraphicsDevice;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

import javax.swing.JFileChooser;

public class DS9Blink {
	
	//main
    public static void main(String[] args) {
    	
    	boolean doRename = true; //rename input files to ###.fits, helps to avoid long command line errors   	
    	String ds9Command = "C:/ds9/ds9.exe";
    	String tempDir = "temp"; //no slashes
    	String ds9Batch = "ds9.bat";
    	
    	//get screen size
    	GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    	int width = gd.getDisplayMode().getWidth();
    	int height = gd.getDisplayMode().getHeight();
    	
    	//test that ds9 is installed
		File f = new File(ds9Command);
		if(!f.exists() || f.isDirectory()) { 
			System.out.println("Error. Could not find SAO Image DS9 (" + ds9Command + ").");
			return;
		}
    	String ds9Parameters = " -geometry "+width+"x"+height+" -invert -zscale";	
		
    	//allow user to choose folder containing asteroid images
		String path;
		JFileChooser chooser = new JFileChooser();
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Select a folder containing your FITS images.");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	      path = chooser.getSelectedFile().toString();
	    } else {
	      System.out.println("Error. No folder selected.");
	      return;
	    } 
	    
	    String tempPath = path + "/" + tempDir;
	    
	    //build a list of FITS files, if any
    	File dir = new File(path);
    	File [] files = dir.listFiles(new FilenameFilter() {
    	    @Override
    	    public boolean accept(File dir, String name) {
    	        return name.endsWith(".fits");
    	    }
    	});

    	try {
    		if(files.length == 0) {
    			System.out.println("No input files found in " + path + ".");
    			return;
    		}
    		
    		System.out.println("Processing " + files.length + " file(s) in " + path + ".");
    		            	
        	//build ds9 command line string
	    	int count = 0;
	    	for (File tempFile : files) {
	    		count++;
		        String sequence = String.format("%d", count);
	    		
		        Pattern rSequence = Pattern.compile("\\_([0-9]+)\\.fits");
		        Matcher m = rSequence.matcher(tempFile.getName());
                if(m.find()) {
                	sequence = m.group(1).trim();
                } 
	    		
	    		File inputFile = new File(tempPath + "/" + sequence + ".fits");
    			inputFile.mkdirs();
    			Files.copy(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    		System.out.println("Processed " + tempFile.getName());
	    		ds9Parameters += " \"" + inputFile.getName() + "\""; 
	    	} 

	    	System.out.println("Processed " + count + " input files.");
	    		    	
	    	ds9Parameters += " -single -zoom to fit";
	    	for(int i=1; i<count; i++) {
	    		ds9Parameters += " -frame next -zoom to fit";
	    	}
	    	
	    	ds9Parameters += " -frame first -frame match wcs -blink";  
		
	    	//for long filenames, we can run into this problem
	    	if(ds9Command.length() + ds9Parameters.length() > 8191) {
	    		System.out.println("Error! Command line maximum length (8192) exceeded!");
	    		return;
	    	}
	    	
	    	ds9Batch = tempPath + "/" + ds9Batch;
        	FileWriter ds9BatchFile = new FileWriter(ds9Batch);
        	ds9BatchFile.write("\"" + ds9Command + "\" " + ds9Parameters);
        	ds9BatchFile.close();	    	
	    	
			try {
				Process proc = Runtime.getRuntime().exec(new String [] {ds9Batch}, null, new File(tempPath));
				InputStream stdin = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(stdin);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while((line = br.readLine()) != null) {
					//System.out.println(line);
				}	
			} catch (IOException e) {
				System.out.println("Error starting " + ds9Batch + ".");
			}    	
	    	
        } catch (Exception e) {         
            e.printStackTrace();
        }
    }	
}

/*Command Line Options

    DS9 will process each command line option, one at a time, as the last step in the initialization process. Therefore, it is possible to use command line options as a little script. For example, the following command line option is used:
    $ds9 -tile foo.fits -cmap Heat -zscale bar.fits -cmap I8

    First DS9 is put in tile mode, then foo.fits is loaded. Then the colormap for foo.fits is changed to Heat and the scale changed to zscale. Next, bar.fits is loaded and the colormap for bar.fits is changed to I8.
    2mass
    3d
    about
    align
    analysis
    array
    asinh
    background
    backup
    bin
    blink
    block
    blue
    catalog
    cd
    cmap
    colorbar
    console
    contour
    crop
    crosshair
    cube
    cursor
    dsssao
    dsseso
    dssstsci
    envi
    exit
    export
    fifo
    fifo_only
    fits
    frame
    geometry
    gif
    green
    grid
    header
    height
    help
    histequ
    iconify
    import
    inet_only
    invert
    iis
    jpeg
    language
    linear
    lock
    log
    lower
    magnifier
    mask
    match
    mecube
    minmax
    mode
    mosaic
    mosaicimage
    movie
    msg
    multiframe
    nameserver
    nan
    nrrd
    nvss
    orient
    pagesetup
    pan
    pixeltable
    plot
    png
    prefs
    preserve
    psprint
    print
    private
    port
    port_only
    pow
    quit
    raise
    regions
    red
    restore
    rgb
    rgbarray
    rgbcube
    rgbimage
    rotate
    samp
    save
    saveimage
    scale
    shm
    single
    sinh
    skyview
    sleep
    slice
    smooth
    squared
    sqrt
    source
    tcl
    threads
    tiff
    tile
    title
    unix
    unix_only
    update
    url
    version
    view
    visual
    vla
    vo
    wcs
    web
    width
    xpa
    zmax
    zoom
    zscale

    2mass

    Support for 2MASS Digital Sky Survey.
    Syntax:
    -2mass []
           [<object>]
           [name <object>]
           [coord <ra> <dec> degrees|sexagesimal] # in wcs fk5
           [size <width> <height> degrees|arcmin|arcsec]
           [save yes|no]
           [frame new|current]
           [update frame|crosshair]
           [survey j|h|k]
           [open|close]
     
    Example:
    $ds9 -2mass
    $ds9 -2mass m31
    $ds9 -2mass name m31
    $ds9 -2mass coord 00:42:44.404 +41:16:08.78 sexagesimal
    $ds9 -2mass size 60 60 arcmin
    $ds9 -2mass save yes
    $ds9 -2mass frame current
    $ds9 -2mass update frame
    $ds9 -2mass survey j
    $ds9 -2mass open
    $ds9 -2mass close

    3d

    Support for 3D frame.
    Syntax:
    -3d []
        [view <az> <el>]
        [az <az>]
        [el <el>]
        [scale <scale>]
        [method mip|aip]
        [background none|azimuth|elevation]
        [border yes|no]
        [border color]
        [highlite yes|no]
        [highlite color]
        [open|close]
     
    Example:
    $ds9 -3d # create new 3D frame
    $ds9 -3d view 45 30
    $ds9 -3d az 45
    $ds9 -3d el 30
    $ds9 -3d scale 10
    $ds9 -3d method mip
    $ds9 -3d background azimuth
    $ds9 -3d border yes
    $ds9 -3d border color red
    $ds9 -3d highlite yes
    $ds9 -3d highlite color red
    $ds9 -3d open
    $ds9 -3d close

    about

    Get DS9 credits.
    Syntax:
    -about
     
    Example:
    $ds9 -about

    align

    Controls the World Coordinate System alignment for the current frame.
    Syntax:
    -align [yes|no]
     
    Example:
    $ds9 -align yes

    analysis

    Control external analysis tasks. Tasks are numbered as they are loaded, starting with 0. Can also be used to display a message and display text in the text dialog window.
    Syntax:
    -analysis [<task number>]
              [<filename>]
              [task <task number>|<task name>]
              [load <filename>]
              [clear]
              [clear][load <filename>]
              [message ok|okcancel|yesno <message>]
              [entry <message>]
              [text]
     
    Example:
    $ds9 -analysis 0 # invoke first analysis task
    $ds9 -analysis task 0
    $ds9 -analysis task foobar
    $ds9 -analysis task "{foo bar}"
    $ds9 -analysis my.ans
    $ds9 -analysis load my.ans
    $ds9 -analysis clear
    $ds9 -analysis clear load my.ans
    $ds9 -analysis message '{This is a message}'
    $ds9 -analysis message okcancel '{This is a message}'
    $ds9 -analysis text '{This is text}'

    array

    Load raw data array into current frame.
    Syntax:
    -array <filename>[[xdim=<x>,ydim=<y>|dim=<dim>],zdim=<z>,bitpix=<b>,skip=<s>,endian=[little|big]]
     
    Example:
    $ds9 -array foo.arr[dim=512,bitpix=-32,endian=little]
    $cat foo.arr | ds9 -array -[dim=512,bitpix=-32,endian=little]

    asinh

    Select ASINH scale function for the current frame.
    Syntax:
    -asinh
     
    Example:
    $ds9 -asinh

    bg
    background

    Set image background color.
    Syntax:
    -bg <color>
     
    Example:
    $ds9 -background red
    $ds9 -bg red

    backup

    Create a backup save set.
    Syntax:
    -backup <filename>
     
    Example:
    $ds9 -backup ds9.bck

    bin

    Controls binning factor, binning buffer size, and  binning function for binning FITS bin tables.
    Syntax:
    -bin [about <x> <y>]
         [about center]
         [buffersize <value>]
         [cols <x> <y>]
         [colsz <x> <y> <z>]
         [factor <value> [<value>]]
         [depth <value>]
         [filter <string>]
         [function average|sum]
         [in]
         [out]
         [to fit]
         [match]
         [lock [yes|no]]
         [open|close]
     
    Example:
    $ds9 -bin about 4096 4096
    $ds9 -bin about center
    $ds9 -bin buffersize 512
    $ds9 -bin cols detx dety
    $ds9 -bin colsz detx dety time
    $ds9 -bin factor 4
    $ds9 -bin factor 4 2
    $ds9 -bin depth 10
    $ds9 -bin filter '{pha > 5}'
    $ds9 -bin filter ''
    $ds9 -bin function sum
    $ds9 -bin in
    $ds9 -bin out
    $ds9 -bin to fit
    $ds9 -bin match
    $ds9 -bin lock yes
    $ds9 -bin open
    $ds9 -bin close

    blink

    Blink mode parameters. Interval is in seconds.
    Syntax:
    -blink []
           [yes|no]
           [interval <value>]
     
    Example:
    $ds9 -blink
    $ds9 -blink yes
    $ds9 -blink interval 1

    block

    Controls blocking parameters.
    Syntax:
    -block [<value>]
           [<value> <value>]
           [to <value>]
           [to <value> <value>]
           [in]
           [out]
           [to fit]
           [match]
           [lock [yes|no]]
           [open|close]
     
    Example:
    $ds9 -block 4
    $ds9 -block 4 2
    $ds9 -block to 4
    $ds9 -block to 4 2
    $ds9 -block in
    $ds9 -block out
    $ds9 -block to fit
    $ds9 -block match
    $ds9 -block lock yes
    $ds9 -block open
    $ds9 -block close

    blue

    For RGB frames, sets the current color channel to blue.
    Syntax:
    -blue
     
    Example:
    $ds9 -blue foo.fits

    catalog
    cat

    Support for catalogs.
    Syntax:
    -catalog []
            [ned|simbad|denis|skybot]
            [ascss|cmc|gsc1|gsc2|gsc3|ac|nomad|ppmx|sao|sdss5|sdss6|sdss7|sdss8|tycho|ua2|ub1|ucac2]
            [2mass|iras]
            [csc|xmm|rosat]
            [first|nvss]
            [chandralog|cfhtlog|esolog|stlog|xmmlog]
            [cds <catalogname>]
            [cds <catalogid>]

            [load <filename>]
            [import sb|tsv <filename>]

            [<ref>] [allcols]
            [<ref>] [allrows]
            [<ref>] [cancel]
            [<ref>] [clear]
            [<ref>] [close]
            [<ref>] [coordinate <ra> <dec> <coordsys>]
            [<ref>] [crosshair]
            [<ref>] [dec <col>]
            [<ref>] [edit yes|no]
            [<ref>] [export sb|tsv <filename>]
            [<ref>] [filter <string>]
            [<ref>] [filter load <filename>]
            [<ref>] [header]
            [<ref>] [hide]
            [<ref>] [location <code>]
            [<ref>] [match <ref> <ref>]
            [<ref>] [match error <value> degrees|arcmin|arcsec]
            [<ref>] [match function 1and2|1not2|2not1]
            [<ref>] [match return 1and2|1only|2only]
            [<ref>] [match unique yes|no]
            [<ref>] [maxrows <number>]
            [<ref>] [name <object>]
            [<ref>] [panto yes|no]
            [<ref>] [plot <xcol> <ycol> <xerrcol> <yerrcol>]
            [<ref>] [print]
            [<ref>] [psky <skyframe>]
            [<ref>] [psystem <coordsys>]
            [<ref>] [ra <col>]
            [<ref>] [regions]
            [<ref>] [retrieve]
            [<ref>] [samp]
            [<ref>] [samp broadcast]
            [<ref>] [samp send <application>]
            [<ref>] [save <filename>]
            [<ref>] [server cds|sao|cadc|adac|iucaa|bejing|cambridge|ukirt]
            [<ref>] [show]
            [<ref>] [size <width> <height> degrees|arcmin|arcsec]
            [<ref>] [sky <skyframe>]
            [<ref>] [skyformat <skyformat>]
            [<ref>] [sort <col> incr|decr]
            [<ref>] [symbol [#] condition|shape|color|text|font|fontsize|fontweight|fontslant <value>]
            [<ref>] [symbol [#] text|size|size2|units|angle <value>]
            [<ref>] [symbol shape {circle point}|{box point}|{diamond point}|
                        {cross point}|{x point}|{arrow point}|{boxcircle point}|
                        circle|ellipse|box|text]
            [<ref>] [symbol add| [#] remove]
            [<ref>] [symbol save|load <filename>]
            [<ref>] [system <coordsys>]
            [<ref>] [update]
            [<ref>] [x <col>]
            [<ref>] [y <col>]

    Example:
    $ds9 -catalog
    $ds9 -catalog 2mass
    $ds9 -catalog cds 2mass
    $ds9 -catalog cds "I/252"

    $ds9 -catalog load foo.xml
    $ds9 -catalog import tsv foo.tsv

    $ds9 -catalog allrows
    $ds9 -catalog allcols
    $ds9 -catalog cancel
    $ds9 -catalog clear
    $ds9 -catalog close
    $ds9 -catalog coordinate 202.48 47.21 fk5
    $ds9 -catalog crosshair
    $ds9 -catalog dec DEC
    $ds9 -catalog edit yes
    $ds9 -catalog export tsv bar.tsv
    $ds9 -catalog filter '\$Jmag>15'
    $ds9 -catalog filter load foo.flt
    $ds9 -catalog header
    $ds9 -catalog hide
    $ds9 -catalog location 500
    $ds9 -catalog match error 2 arcsec
    $ds9 -catalog match function 1and2
    $ds9 -catalog match unique no
    $ds9 -catalog match return 1only
    $ds9 -catalog match 2mass csc
    $ds9 -catalog maxrows 2000
    $ds9 -catalog name m51
    $ds9 -catalog panto no
    $ds9 -catalog plot '\$Jmag' '\$Hmag' '\$e_Jmag' '\$e_Hmag'
    $ds9 -catalog print
    $ds9 -catalog psky fk5
    $ds9 -catalog psystem wcs
    $ds9 -catalog ra RA
    $ds9 -catalog regions
    $ds9 -catalog retrieve
    $ds9 -catalog samp broadcast
    $ds9 -catalog samp send aladin
    $ds9 -catalog save foo.xml
    $ds9 -catalog server sao
    $ds9 -catalog show
    $ds9 -catalog size 1 1 degrees
    $ds9 -catalog symbol condition '\$Jmag>15'
    $ds9 -catalog symbol 2 shape "boxcircle point"
    $ds9 -catalog symbol color red
    $ds9 -catalog symbol font times
    $ds9 -catalog symbol fontsize 14
    $ds9 -catalog symbol fontweight bold
    $ds9 -catalog symbol fontslant italic
    $ds9 -catalog symbol add
    $ds9 -catalog symbol 2 remove
    $ds9 -catalog symbol load foo.sym
    $ds9 -catalog symbol save bar.sym
    $ds9 -catalog sky fk5
    $ds9 -catalog skyformat degrees
    $ds9 -catalog sort "Jmag" incr
    $ds9 -catalog system wcs
    $ds9 -catalog update
    $ds9 -catalog x RA
    $ds9 -catalog y DEC

    cd

    Sets the current working directory.
    Syntax:
    cd [<directory>]
     
    Example:
    $ds9 -cd /home/mrbill

    cmap

    Controls the colormap for the current frame. The colormap name is not case sensitive. A valid contrast value is  from 0 to 10 and bias value from 0 to 1.
    Syntax:
    -cmap [<colormap>]
          [file]
          [load <filename>]
          [save <filename>]
          [invert yes|no]
          [value <contrast> <bias>]
          [tag [load|save] <filename>]
          [tag delete]
          [match]
          [lock [yes|no]]
          [open|close]
     
    Example:
    $ds9 -cmap Heat
    $ds9 -cmap load foo.sao
    $ds9 -cmap save bar.sao
    $ds9 -cmap invert yes
    $ds9 -cmap value 5 .5
    $ds9 -cmap tag load foo.tag
    $ds9 -cmap tag save foo.tag
    $ds9 -cmap tag delete
    $ds9 -cmap match
    $ds9 -cmap lock yes
    $ds9 -cmap open
    $ds9 -cmap close

    colorbar

    Controls colorbar parameters.
    Syntax:
    -colorbar []
              [yes|no]
              [horizontal|vertical]
              [orientation horizontal|vertical]
              [numerics yes|no]
              [space value|distance]
              [font times|helvetica|courier]
              [fontsize <value>]
              [fontweight normal|bold]
              [fontslant roman|italic]
              [size]
              [ticks]
     
    Example:
    $ds9 -colorbar yes
    $ds9 -colorbar vertical
    $ds9 -colorbar orientation vertical
    $ds9 -colorbar numerics yes
    $ds9 -colorbar space value
    $ds9 -colorbar font times
    $ds9 -colorbar fontsize 14
    $ds9 -colorbar fontweight bold
    $ds9 -colorbar fontslant italic
    $ds9 -colorbar size 20
    $ds9 -colorbar ticks 11

    console

    Display tcl console window.
    Syntax:
    -console
     
    Example:
    $ds9 -console

    contour

    Controls contours in the current frame.
    Syntax:
    -contour []
             [yes|no]
             [clear]
             [generate]
             [load <filename> <coordsys> <skyframe> <color> <width> yes|no]
             [save <filename> <coordsys> <skyframe>]
             [convert]
             [loadlevels <filename>]
             [savelevels <filename>]
             [copy]
             [paste <coordsys> <color> <width> yes|no]
             [color <color>]
             [width <width>]
             [dash yes|no]
             [smooth <smooth>]
             [method block|smooth]
             [nlevels <number of levels>]
             [scale linear|log|pow|squared|sqrt|asinh|sinh|histequ]
             [log exp <value>]
             [mode minmax|<value>|zscale|zmax]
             [limits <min> <max>]
             [levels <value value value...>]
             [open|close]
     
    Example:
    $ds9 -contour
    $ds9 -contour yes
    $ds9 -contour generate
    $ds9 -contour clear
    $ds9 -contour load ds9.con wcs fk5 yellow 2 no
    $ds9 -contour load ds9.con wcs fk5 red 2 yes
    $ds9 -contour save ds9.con wcs fk5
    $ds9 -contour convert
    $ds9 -contour loadlevels ds9.lev
    $ds9 -contour savelevels ds9.lev
    $ds9 -contour copy
    $ds9 -contour paste wcs red 2 no
    $ds9 -contour color yellow
    $ds9 -contour width 2
    $ds9 -contour dash yes
    $ds9 -contour smooth 5
    $ds9 -contour method smooth
    $ds9 -contour nlevels 10
    $ds9 -contour scale sqrt
    $ds9 -contour log exp 1000
    $ds9 -contour mode zscale
    $ds9 -contour limits 1 100
    $ds9 -contour levels "1 10 100 1000"
    $ds9 -contour open
    $ds9 -contour close

    crop

    Set current image display area.
    Syntax:
    -crop [<x> <y> <width> <height> [<coordsys>][<skyframe>][<skyformat>][degrees|arcmin|arcsec]
          [match <coordsys>]
          [lock <coordsys>|none]
     
    Example:
    $ds9 foo.fits -crop 40 30 10 20 # set crop in physical coords
    $ds9 foo.fits -crop +104:51:06.915 +68:33:40.761  28.144405 22.000204 wcs galactic arcsec
    $ds9 foo.fits -crop match wcs
    $ds9 foo.fits -crop lock wcs

    crosshair

    Controls the current position of the crosshair in the current frame. DS9 is placed in crosshair mode when the crosshair is set.
    Syntax:
    -crosshair [<x> <h> <coordsys> [<skyframe>][<skyformat>]]
               [match <coordsys>]
               [lock <coordsys>|none]
     
    Example:
    $ds9 -crosshair 100 100 physical # set crosshair in physical
    $ds9 -crosshair 345 58.8 wcs fk5 # set crosshair in wcs coords
    $ds9 -crosshair 23:01:00 +58:52:51 wcs fk5
    $ds9 -crosshair match
    $ds9 -crosshair lock wcs

    cube

    Controls FITS cube.
    Syntax:
    -cube [play|stop|next|prev|first|last]
          [<slice> [<coordsys>][<axis>]]
          [interval <numeric>]
          [axis <axis>]
          [match <coordsys>]
          [lock <coordsys>|none]
          [order 123|132|213|231|312|321]
          [axes lock [yes|no]]
          [open|close]
     
    Example:
    $ds9 -cube play
    $ds9 -cube last
    $ds9 -cube 3
    $ds9 -cube 4.5 wcs 3
    $ds9 -cube interval 2
    $ds9 -cube axis 3
    $ds9 -cube match wcs
    $ds9 -cube lock wcs
    $ds9 -cube order 123
    $ds9 -cube axes lock yes
    $ds9 -cube open
    $ds9 -cube close

    cursor

    Move mouse pointer or crosshair in image pixels in the current frame. Note, this will move selected Regions also.
    Syntax:
    -cursor [<x> <h>]
     
    Example:
    $ds9 -cursor 10 10

    dsssao
    dss

    Support for Digital Sky Survey at SAO.
    Syntax:
    -dsssao []
            [<object>]
            [name <object>]
            [coord <ra> <dec> degrees|sexagesimal] # in wcs fk5
            [size <width> <height> degrees|arcmin|arcsec]
            [save yes|no]
            [frame new|current]
            [update frame|crosshair]
            [open|close]
     
    Example:
    $ds9 -dsssao
    $ds9 -dsssao m31
    $ds9 -dsssao name m31
    $ds9 -dsssao coord 00:42:44.404 +41:16:08.78 sexagesimal
    $ds9 -dsssao size 60 60 arcmin
    $ds9 -dsssao save yes
    $ds9 -dsssao frame current
    $ds9 -dsssao update frame
    $ds9 -dsssao open
    $ds9 -dsssao close

    dsseso

    Support for Digital Sky Survey at ESO.
    Syntax:
    -dsseso []
            [<object>]
            [name <object>]
            [coord <ra> <dec> degrees|sexagesimal] # in wcs fk5
            [size <width> <height> degrees|arcmin|arcsec]
            [save yes|no]
            [frame new|current]
            [update frame|crosshair]
            [survey DSS1|DSS2-red|DSS2-blue|DSS2-infrared]
            [open|close]
     
    Example:
    $ds9 -dsseso
    $ds9 -dsseso m31
    $ds9 -dsseso name m31
    $ds9 -dsseso coord 00:42:44.404 +41:16:08.78 sexagesimal
    $ds9 -dsseso size 60 60 arcmin
    $ds9 -dsseso save yes
    $ds9 -dsseso frame current
    $ds9 -dsseso update frame
    $ds9 -dsseso survey DSS2-red
    $ds9 -dsseso open
    $ds9 -dsseso close

    dssstsci

    Support for Digital Sky Survey at STSCI.
    Syntax:
    -dssstsci []
              [<object>]
              [name <object>]
              [coord <ra> <dec> degrees|sexagesimal] # in wcs fk5
              [size <width> <height> degrees|arcmin|arcsec]
              [save yes|no]
              [frame new|current]
              [update frame|crosshair]
              [survey poss2ukstu_red|poss2ukstu_ir|poss2ukstu_blue]
              [survey poss1_blue|poss1_red]
              [survey all|quickv|phase2_gsc2|phase2_gsc1]
              [open|close]
     
    Example:
    $ds9 -dssstsci
    $ds9 -dssstsci m31
    $ds9 -dssstsci name m31
    $ds9 -dssstsci coord 00:42:44.404 +41:16:08.78 sexagesimal
    $ds9 -dssstsci size 60 60 arcmin
    $ds9 -dssstsci save yes
    $ds9 -dssstsci frame current
    $ds9 -dssstsci update frame
    $ds9 -dssstsci survey all
    $ds9 -dssstsci open
    $ds9 -dssstsci close

    envi

    Load an ENVI header and file. Optional parameter: array endian.
    Syntax:
    -envi <header> [<filename>]
     
    Example:
    $ds9 -envi foo.hdr
    $ds9 -envi foo.hdr foo.bsq

    exit
    quit

    Quits DS9.
    Syntax:
    -exit
    -quit
     
    Example:
    $ds9 -exit

    export

    Export loaded image data of current frame in specified image format, at native resolution, using current colormap and contrast/bias settings. NOTE: not scaling, rotation, or translation is applied. If no format specified, the file name extension is used to determine the output format. Optional parameters: jpeg quality (1-100) and tiff compression method.
    Syntax:
    -export [array|nrrd|envi|gif|tiff|jpeg|png] <filename>
    -export array <filename> [big|little|native]
    -export nrrd <filename> [big|little|native]
    -export envi <header> [<filename>] [big|little|native]
    -export <filename>.jpeg [1-100]
    -export <filename>.tiff [none|jpeg|packbits|deflate]
     
    Example:
    $ds9 -export array foo.arr little
    $ds9 -export nrrd foo.nrrd little
    $ds9 -export envi foo.hdr little
    $ds9 -export envi foo.hdr foo.bsq little
    $ds9 -export tiff foo.tiff jpeg
    $ds9 -export jpeg foo.jpeg 75
    $ds9 -export png foo.png

    fifo

    Set the name of the IRAF input and output fifos. The default is /dev/imt1. These fifos are used by IRAF to communicate with DS9.
    Syntax:
    -fifo name
     
    Example:
    $ds9 -fifo /dev/imt1

    fifo_only

    Only use IRAF input and output fifos. Same as -port 0 -unix none.
    Syntax:
    -fifo_only
     
    Example:
    $ds9 -fifo_only

    fits

    Load a FITS image into the current frame.
    Syntax:
    -fits <filename>
     
    Example:
    $ds9 -fits foo.fits
    $ds9 -fits bar.fits[bin=detx,dety]
    $cat foo.fits | ds9 -fits -
    $cat bar.fits | ds9 -fits -[bin=detx,dety]

    frame

    Controls frame functions. Frames may be created, deleted, reset, and centered. While return the current frame number. If you goto a frame that does not exists, it will be created. If the frame is hidden, it will be shown. The 'frameno' option is available for backward compatibility.
    Syntax:
    -frame [center [#|all]]
           [clear [#|all]]
           [new [rgb]]
           [delete [#|all]]
           [reset [#|all]]
           [refresh [#|all]]
           [hide [#|all]]
           [show [#|all]]
           [move first]
           [move back]
           [move forward]
           [move last]
           [first]
           [prev]
           [next]
           [last]
           [frameno #]
           [#]
           [match <coordsys>]
           [lock <coordsys>|none]
     
    Example:
    $ds9 -frame center # center current frame
    $ds9 -frame center 1 # center 'Frame1'
    $ds9 -frame center all # center all frames
    $ds9 -frame clear # clear current frame
    $ds9 -frame new # create new frame
    $ds9 -frame new rgb # create new rgb frame
    $ds9 -frame delete # delete current frame
    $ds9 -frame reset # reset current frame
    $ds9 -frame refresh # refresh current frame
    $ds9 -frame hide # hide current frame
    $ds9 -frame show 1 # show frame 'Frame1'
    $ds9 -frame move first # move frame to first in order
    $ds9 -frame move back # move frame back in order
    $ds9 -frame move forward # move frame forward in order
    $ds9 -frame move last # move frame to last in order
    $ds9 -frame first # goto first frame
    $ds9 -frame prev # goto prev frame
    $ds9 -frame next # goto next frame
    $ds9 -frame last # goto last frame
    $ds9 -frame frameno 4 # goto frame 'Frame4', create if needed
    $ds9 -frame 3 # goto frame 'Frame3', create if needed
    $ds9 -frame lock wcs

    gif

    Import gif file.
    Syntax:
    -gif <filename>
     
    Example:
    $ds9 -gif foo.gif
    $cat foo.gif | ds9 -gif -

    geometry

    Define the initial window geometry. This includes all of the ds9 window, not just the image space. see X(1).
    Syntax:
    -geometry value
     
    Example:
    $ds9 -geometry 640x480

    green

    For RGB frames, sets the current color channel to green.
    Syntax:
    -green
     
    Example:
    $ds9 -green foo.fits

    grid

    Controls coordinate grid. For grid numeric format syntax,  click here.
    Syntax:
    -grid []
          [yes|no]
          [type analysis|publication]
          [system <coordsys>]
          [sky <skyframe>]
          [skyformat <skyformat>]
          [grid yes|no]
          [grid color <color>]
          [grid width <value>]
          [grid style 0|1]
          [grid gap1 <value>]
          [grid gap2 <value>]
          [axes yes|no]
          [axes color <color>]
          [axes width <value>]
          [axes style 0|1]
          [axes type interior|exterior]
          [axes origin lll|llu|lul|luu|ull|ulu|uul|uuu]
          [format1 <format>]
          [format2 <format>]
          [tickmarks yes|no]
          [tickmarks color <color>]
          [tickmarks width <value>]
          [tickmarks style 0|1]
          [border yes|no]
          [border color <color>]
          [border width <value>]
          [border style 0|1]
          [numerics yes|no]
          [numerics font times|helvetica|courier]
          [numerics fontsize <value>]
          [numerics fontweight normal|bold]
          [numerics fontslant roman|italic]
          [numerics color <color>]
          [numerics gap1 <value>]
          [numerics gap2 <value>]
          [numerics type interior|exterior]
          [numerics vertical yes|no]
          [title yes|no]
          [title text <text>]
          [title def yes|no]
          [title gap <value>]
          [title font times|helvetica|courier]
          [title fontsize <value>]
          [title fontweight normal|bold]
          [title fontslant roman|italic]
          [title color <color>]
          [labels yes|no]
          [labels text1 <text>]
          [labels def1 yes|no]
          [labels gap1 <value>]
          [labels text2 <text>]
          [labels def2 yes|no]
          [labels gap2 <value>]
          [labels font times|helvetica|courier]
          [labels fontsize <value>]
          [labels fontweight normal|bold]
          [labels fontslant roman|italic]
          [labels color <color>]
          [reset]
          [load <filename>]
          [save <filename>]
          [open|close]
     
    Example:
    $ds9 -grid
    $ds9 -grid yes
    $ds9 -grid type analysis
    $ds9 -grid system wcs
    $ds9 -grid sky fk5
    $ds9 -grid skyformat degrees
    $ds9 -grid grid yes
    $ds9 -grid grid color red
    $ds9 -grid grid width 2
    $ds9 -grid grid style 1
    $ds9 -grid grid gap1 10
    $ds9 -grid grid gap2 10
    $ds9 -grid axes yes
    $ds9 -grid axes color red
    $ds9 -grid axes width 2
    $ds9 -grid axes style 1
    $ds9 -grid axes type exterior
    $ds9 -grid axes origin lll
    $ds9 -grid format1 d.2
    $ds9 -grid format2 d.2
    $ds9 -grid tickmarks yes
    $ds9 -grid tickmarks color red
    $ds9 -grid tickmarks width 2
    $ds9 -grid tickmarks style 1
    $ds9 -grid border yes
    $ds9 -grid border color red
    $ds9 -grid border width 2
    $ds9 -grid border style 1
    $ds9 -grid numerics yes
    $ds9 -grid numerics font courier
    $ds9 -grid numerics fontsize 12
    $ds9 -grid numerics fontweight bold
    $ds9 -grid numerics fontslant italic
    $ds9 -grid numerics color red
    $ds9 -grid numerics gap1 10
    $ds9 -grid numerics gap2 10
    $ds9 -grid numerics type exterior
    $ds9 -grid numerics vertical yes
    $ds9 -grid title yes
    $ds9 -grid title text {Hello World}
    $ds9 -grid title def yes
    $ds9 -grid title gap 10
    $ds9 -grid title font courier
    $ds9 -grid title fontsize 12
    $ds9 -grid title fontweight bold
    $ds9 -grid title fontslant italic
    $ds9 -grid title color red
    $ds9 -grid labels yes
    $ds9 -grid labels text1 {Hello World}
    $ds9 -grid labels def1 yes
    $ds9 -grid labels gap1 10
    $ds9 -grid labels text2 {Hello World}
    $ds9 -grid labels def2 yes
    $ds9 -grid labels gap2 10
    $ds9 -grid labels font courier
    $ds9 -grid labels fontsize 12
    $ds9 -grid labels fontweight bold
    $ds9 -grid labels fontslant italic
    $ds9 -grid labels color red
    $ds9 -grid reset
    $ds9 -grid load foo.grd
    $ds9 -grid save foo.grd
    $ds9 -grid open
    $ds9 -grid close

    header

    Display current fits header dialog. Optional extension number maybe specified.
    Syntax:
    -header [<ext>]
            [close [<ext>]]
            [save [<ext>] <filename>]
     
    Example:
    $ds9 -header
    $ds9 -header 2
    $ds9 -header close
    $ds9 -header save 1 foo.txt

    height

    Set the height of the image display window. Use the geometry command to set the overall width and height of the ds9 window.
    Syntax:
    -height [<value>]
     
    Example:
    $ds9 -height 512

    help

    Display help information. To maintain backward compatibility, -help will display a brief help message and exit. --help will display all command line options within the built-in help facility.
    Syntax:
    -help # Display brief help message and exit.
    --help # Display command line options within help facility.
    -? # Display command line options within help facility.
     
    Example:
    $ds9 -help # Display brief help message and exit.
    $ds9 --help # Display command line options within help facility
    $ds9 -? # Display command line options within help facility.

    histequ

    Select histogram equalization scale function for the current frame.
    Syntax:
    -histequ
     
    Example:
    $ds9 -histequ

    iconify

    Toggles iconification.
    Syntax:
    -iconify []
             [yes|no]
     
    Example:
    $ds9 -iconify
    $ds9 -iconify yes

    invert

    Invert Colormap.
    Syntax:
    -invert
     
    Example:
    $ds9 -invert

    iis

    Set IIS Filename. Optional mosaic number maybe supplied.
    Syntax:
    -iis [filename <filename> [#]]
     
    Example:
    $ds9 -iis filename foo.fits
    $ds9 -iis filename bar.fits 4

    jpeg

    Load JPEG image into current frame.
    Syntax:
    -jpeg <filename>
     
    Example:
    $ds9 -jpeg foo.jpeg
    $cat foo.jpeg | ds9 -jpeg -

    language

    Select current language.
    Syntax:
    -language [locale|da|de|es|en|fr|ja|pt]
     
    Example:
    $ds9 -language fr

    linear

    Select linear scale function for the current frame.
    Syntax:
    -linear
     
    Example:
    $ds9 -linear

    lock

    Lock all other frames to the current frame.
    Syntax:
    -lock [frame <coordsys>|none]
          [crosshair <coordsys>|none]
          [crop <coordsys>|none]
          [slice <coordsys>|none]
          [bin [yes|no]]
          [axes [yes|no]]
          [scale [yes|no]]
          [scalelimits [yes|no]]
          [colorbar [yes|no]]
          [block [yes|no]]
          [smooth [yes|no]]

    Example:
    $ds9 -lock frame wcs
    $ds9 -lock crosshair wcs
    $ds9 -lock crop wcs
    $ds9 -lock slice wcs
    $ds9 -lock bin yes
    $ds9 -lock axes yes
    $ds9 -lock scale yes
    $ds9 -lock scalelimits yes
    $ds9 -lock colorbar yes
    $ds9 -lock block yes
    $ds9 -lock smooth yes

    log

    Select log scale function for the current frame.
    Syntax:
    -log
     
    Example:
    $ds9 -log

    lower

    Lower in the window stacking order.
    Syntax:
    -lower
     
    Example:
    $ds9 -lower

    magnifier

    Controls the magnifier settings.
    Syntax:
    magnifier [color <color>]
              [zoom <value>]
              [cursor yes|no]
              [region yes|no]
     
    Example:
    $ds9 -magnifier color yellow
    $ds9 -magnifier zoom 2
    $ds9 -magnifier cursor no
    $ds9 -magnifier region no

    mask
    nomask

    Controls mask parameters.
    Syntax:
    -mask [color <color>]
          [mark 1|0]
          [transparency <value>]
          [clear]
          [open|close]
    -nomask
     
    Example:
    $ds9 -mask color red
    $ds9 -mask mark 0
    $ds9 -mask transparency 50
    $ds9 -mask clear
    $ds9 -mask open
    $ds9 -mask close
    $ds9 -nomask

    match

    Match all other frames to the current frame.
    Syntax:
    -match [frame <coordsys>]
           [crosshair <coordsys>]
           [crop <coordsys>]
           [slice <coordsys>]
           [bin]
           [axes]
           [scale]
           [scalelimits]
           [colorbar]
           [block]
           [smooth]
     
    Example:
    $ds9 -match frame wcs
    $ds9 -match crosshair wcs
    $ds9 -match crop wcs
    $ds9 -match slice wcs
    $ds9 -match bin
    $ds9 -match axes
    $ds9 -match scale
    $ds9 -match scalelimits
    $ds9 -match colorbar
    $ds9 -match block
    $ds9 -match smooth

    mecube

    Load FITS multiple extension file as data cube.
    Syntax:
    mecube <filename>
     
    Example:
    $ds9 -mecube foo.fits
    $cat foo.fits | ds9 -mecube -

    minmax

    This is how DS9 determines  the min and max data values from the data. SCAN will scan all data. SAMPLE will sample the data every n samples. DATAMIN and IRAFMIN will use the values of the keywords if present. In general, it is recommended to use SCAN unless your computer is slow or your data files are very large. Select the increment  interval for determining the min and max data values during sampling. The larger the interval, the quicker the process.
    Syntax:
    -minmax [scan|sample|datamin|irafmin]
            [mode scan|sample|datamin|irafmin]
            [interval <value>]
     
    Example:
    $ds9 -minmax scan
    $ds9 -minmax mode scan
    $ds9 -minmax interval 10

    mode

    Select the current mode.
    Syntax:
    -mode [none|region|crosshair|colorbar|pan|zoom|rotate|catalog|examine]
     
    Example:
    $ds9 -mode crosshair

    mosaic

    Load FITS mosaic segment into current frame.
    Syntax:
    -mosaic [wcs|wcsa...wcsz|iraf] <filename>
     
    Example:
    $ds9 -mosaic foo.fits
    $ds9 -mosaic wcs foo.fits
    $cat foo.fits | ds9 -mosaic -
    $cat foo.fits | ds9 -mosaic wcs -

    mosaicimage

    Load FITS mosaic image into current frame.
    Syntax:
    -mosaicimage [wcs|wcsa...wcsz|iraf|wfpc2] <filename>
     
    Example:
    $ds9 -mosaicimage foo.fits
    $ds9 -mosaicimage wcs foo.fits
    $cat foo.fits | ds9 -mosaicimage
    $cat foo.fits | ds9 -mosaiimage wcs

    movie
    savempeg

    Create mpeg1 movie from snap shots of the DS9 window. A slice movie cycles though all slices of a cube. A frame movie cycles through all active frames. A 3d movie cycles through specified viewing angles. The default is frame. Optional parameters for 3d: number of frames, azimuth from/to, elevation from/to, slice from/to, oscillate/repeat times.
    Syntax:
    -movie [slice|frame|3d] <filename>
    -movie 3d <filename> [number|azfrom|azto|elfrom|elto|slfrom|slto|oscillate|repeat <#>]
     
    Example:
    $ds9 -movie slice ds9.mpg
    $ds9 -movie 3d ds9.mpg number 10 azfrom -60 azto 60 oscillate 1

    msg

    Specify a directory of translation tables to be loaded.
    Syntax:
    -msg <directory>
     
    Example:
    $ds9 -msg $HOME/msgs

    multiframe

    Load FITS multiple extension file as multiple images.
    Syntax:
    multiframe <filename>
     
    Example:
    $ds9 -multiframe foo.fits
    $cat foo.fits | ds9 -multiframe -

    nameserver

    Support Name Server functions. Coordinates are in fk5.
    Syntax:
    -nameserver [<object>]
                [name <object>]
                [server ned-sao|ned-eso|simbad-sao|simbad-eso]
                [skyformat degrees|sexagesimal]
                [pan]
                [crosshair]
                [open|close]
     
    Example:
    $ds9 -nameserver m31
    $ds9 -nameserver name m31
    $ds9 -nameserver server ned-sao
    $ds9 -nameserver skyformat sexagesimal
    $ds9 -nameserver pan
    $ds9 -nameserver crosshair
    $ds9 -nameserver open
    $ds9 -nameserver close

    nan

    Set image not-a-number color.
    Syntax:
    -nan <color>
     
    Example:
    $ds9 -nan red

    nrrd

    Load an NRRD (Nearly Raw Raster Data) file.
    Syntax:
    -nrrd <filename>
     
    Example:
    $ds9 -nrrd foo.nrrd
    $cat foo.nrrd | xpaset ds9 -nrrd -

    nvss

    Support for NRAO VLA Sky Survey.
    Syntax:
    -nvss []
          [<object>]
          [name <object>]
          [coord <ra> <dec> degrees|sexagesimal] # in wcs fk5
          [size <width> <height> degrees|arcmin|arcsec]
          [save yes|no]
          [frame new|current]
          [update frame|crosshair]
          [open|close]
     
    Example:
    $ds9 -nvss
    $ds9 -nvss m31
    $ds9 -nvss name m31
    $ds9 -nvss coord 00:42:44.404 +41:16:08.78 sexagesimal
    $ds9 -nvss size 60 60 arcmin
    $ds9 -nvss save yes
    $ds9 -nvss frame current
    $ds9 -nvss update frame
    $ds9 -nvss open
    $ds9 -nvess close

    orient

    Controls the orientation of the current frame.
    Syntax:
    -orient [none|x|y|xy]
            [open|close]
     
    Example:
    $ds9 -orient xy
    $ds9 -orient open
    $ds9 -orient close

    pagesetup

    Controls Page Setup options.
    Syntax:
    -pagesetup [orient portrait|landscape]
               [scale <numberic>]
               [size letter|legal|tabloid|poster|a4]
     
    Example:
    $ds9 -pagesetup orient portrait
    $ds9 -pagesetup scale 50
    $ds9 -pagesetup size poster

    pan

    Controls the current image cursor location for the current frame.
    Syntax:
    -pan [<x> <h> <coordsys> [<skyframe>][<skyformat>]
         [to <x> <h> <coordsys> [<skyframe>][<skyformat>]
         [open|close]
     
    Example:
    $ds9 -pan 200 200 image
    $ds9 -pan to 400 400 physical
    $ds9 -pan to 13:29:55 47:11:50 wcs fk5
    $ds9 -pan open
    $ds9 -pan close

    pixeltable

    Display/Hide the pixel table.
    Syntax:
    -pixeltable []
                [yes|open]
                [no|close]
     
    Example:
    $ds9 -pixeltable
    $ds9 -pixeltable yes
    $ds9 -pixeltable open
    $ds9 -pixeltable close

    plot

    Display and configure data plots. All plot commands take an optional second command, the plot name. If no plot name is specified, the last plot created is assumed. Plot data is assumed to be a pair of coordinates, with optional error values. The follow are valid data descriptions:

        xy        x and y coordinates
        xyex      x,y coordinates with x errors
        xyey      x,y coordinates with y errors
        xyexey    x,y coordinates with  x and y errors

    To create a new plot, use the plot new command.
    Syntax:
    # create new empty plot window
    -plot
    -plot [bar|scatter]
          [new [name <plotname>] [line|bar|scatter]]
          [new [name <plotname>] [line|bar|scatter] <title> <xaxis label> <yaxis label> xy|xyex|xyey|xyexey]

    -plot [<plotname>] load <filename> [xy|xyex|xyey|xyexey]
          [<plotname>] save <filename>
          [<plotname>] clear
          [<plotname>] duplicate
          [<plotname>] stats
          [<plotname>] list
          [<plotname>] loadconfig <filename>
          [<plotname>] saveconfig <filename>
          [<plotname>] pagesetup orient [portrait|landscape]
          [<plotname>] pagesetup size [letter|legal|tabloid|poster|a4]
          [<plotname>] print
          [<plotname>] print destination [printer|file]
          [<plotname>] print command <command>
          [<plotname>] print filename <filename>
          [<plotname>] print color [rgb|gray]
          [<plotname>] close

    -plot [<plotname>] mode [pointer|zoom]

    # configure graph
    -plot [<plotname>] axis [x|y] grid [yes|no]
          [<plotname>] axis [x|y] log [yes|no]
          [<plotname>] axis [x|y] flip [yes|no]
          [<plotname>] axis [x|y] auto [yes|no]
          [<plotname>] axis [x|y] min <value>
          [<plotname>] axis [x|y] max <value>
          [<plotname>] axis [x|y] format <string>
          [<plotname>] legend [yes|no]
          [<plotname>] legend position [right|left|top|bottom]
          [<plotname>] font [title|labels|numbers] font [times|helvetica|courier]
          [<plotname>] font [title|labels|numbers] size <value>
          [<plotname>] font [title|labels|numbers] weight [normal|bold]
          [<plotname>] font [title|labels|numbers] slant [roman|italic]
          [<plotname>] title <string>
          [<plotname>] title [x|y] <string>
          [<plotname>] barmode [normal|stacked|aligned|overlap]

    # configure current dataset
    -plot [<plotname>] show [yes|no]
          [<plotname>] shape [circle|square|diamond|plus|splus|scross|triangle|arrow]
          [<plotname>] shape fill [yes|no]
          [<plotname>] shape color <value>
          [<plotname>] smooth [step|linear|cubic|quadratic|catrom]
          [<plotname>] color <value>
          [<plotname>] width <value>
          [<plotname>] dash [yes|no]
          [<plotname>] error [yes|no]
          [<plotname>] error color <value>
          [<plotname>] error width <value>
          [<plotname>] name <string>
    # select current dataset
    -plot [<plotname>] select <value>
     
    Example:
    # create new empty plot window
    $ds9 -plot
    $ds9 -plot scatter
    $ds9 -plot new
    $ds9 -plot new bar
    $ds9 -plot new name foo
    $ds9 -plot new name foo scatter

    $ds9 -plot load foo.dat xy # load new dataset with dimension xy
    $ds9 -plot save bar.dat # save current dataset
    $ds9 -plot clear # clear all datasets
    $ds9 -plot duplicate # duplicate current dataset
    $ds9 -plot stats # display current dataset statistics
    $ds9 -plot list # list current dataset
    $ds9 -plot loadconfig foo.plt # load plot configuration
    $ds9 -plot saveconfig bar.plt # save current plot configuration
    $ds9 -plot pagesetup orient portrait
    $ds9 -plot pagesetup size letter
    $ds9 -plot print
    $ds9 -plot print destination file
    $ds9 -plot print command "lp"
    $ds9 -plot print filename "foo.ps"
    $ds9 -plot print color rgb
    $ds9 -plot close # close current plot

    $ds9 -plot mode pointer

    # configure plot
    $ds9 -plot axis x grid yes
    $ds9 -plot axis x log yes
    $ds9 -plot axis x flip yes
    $ds9 -plot axis x auto no
    $ds9 -plot axis x min 0
    $ds9 -plot axis x max 100
    $ds9 -plot axis x format {%f}
    $ds9 -plot legend yes # show legend
    $ds9 -plot legend position left
    $ds9 -plot font numbers font times
    $ds9 -plot font numbers size 12
    $ds9 -plot font numbers weight bold
    $ds9 -plot font numbers slant italic
    $ds9 -plot title {The Title}
    $ds9 -plot title x {X Axis}
    $ds9 -plot barmode aligned

    # configure current dataset
    $ds9 -plot show yes
    $ds9 -plot shape circle
    $ds9 -plot shape fill no
    $ds9 -plot shape color cyan
    $ds9 -plot smooth step
    $ds9 -plot color red
    $ds9 -plot width 2
    $ds9 -plot dash yes
    $ds9 -plot error yes
    $ds9 -plot error color red
    $ds9 -plot error width 2
    $ds9 -plot name {My Data}

    # select current dataset
    $ds9 -plot select 2

    png

    Load PNG image into current frame.
    Syntax:
    -png <filename>
     
    Example:
    $ds9 png foo.png
    $cat foo.png | ds9 -png -

    port

    Set the IRAF port number, used by IRAF to communicate with DS9. The default is 5137, the standard IRAF port used by ximtool.
    Syntax:
    -port number
     
    Example:
    $ds9 -port 5137

    port_only
    inet_only

    Only use the IRAF port number. This is the same as -fifo none -unix none.
    Syntax:
    -port_only
     
    Example:
    $ds9 -port_only

    pow

    Select power scale function for the current frame.
    Syntax:
    -pow
     
    Example:
    $ds9 -pow

    prefs

    Controls various preference settings.
    Syntax:
    -prefs [clear]
           [irafalign yes|no]
     
    Example:
    $ds9 -prefs clear
    $ds9 -prefs irafalign yes

    preserve

    Preserve the follow attributes while loading a new image.
    Syntax:
    preserve [pan yes|no]
             [regions yes|no]
     
    Example:
    $ds9 -preserve pan yes
    $ds9 -preserve regions yes

    psprint

    For MacOSX and Windows, invokes postscript printing. For all others, same as print. Please see print for further details.

    print

    Controls printing. Use print option to set printing options. Use print to actually print.
    Syntax:
    -print [destination printer|file]
           [command <command>]
           [filename <filename>]
           [color rgb|cmyk|gray]
           [level 1|2]
           [resolution 53|72|75|150|300|600]
     
    Example:
    $ds9 -print
    $ds9 -print destination file
    $ds9 -print command 'gv -'
    $ds9 -print filename foo.ps
    $ds9 -print color cmyk
    $ds9 -print level 2
    $ds9 -print resolution 75

    private

    use private colormap, valid for pseudocolor 8 mode.
    Syntax:
    -private
     
    Example:
    $ds9 -private

    raise

    Raise in the window stacking order.
    Syntax:
    -raise
     
    Example:
    $ds9 -raise

    regions

    Controls regions in the current frame.
    Syntax:
    -regions [<filename>]
             [load [all] <filename>]
             [save <filename>]
             [list [close]]
             [epsilon <integer>]
             [show yes|no]
             [showtext yes|no]
             [centroid]
             [centroid auto yes|no]
             [centroid radius <value>|iteration <value>]
             [getinfo]
             [move front]
             [move back]
             [select all]
             [select none]
             [select invert]
             [delete all]
             [delete select]
             [format ds9|xml|ciao|saotng|saoimage|pros|xy]
             [system image|physical|wcs|wcsa...wcsz]
             [sky fk4|fk5|icrs|galactic|ecliptic]
             [skyformat degrees|sexagesimal]
             [strip yes|no]
             [shape <shape>]
             [color <color>
             [width <width>]
             [fixed|edit|rotate|delete yes|no]
             [include|exclude|source|background]
             [delim [nl|<char>]]
             [command <marker command>]
             [composite]
             [dissolve]
             [template <filename>]
             [template <filename> at <ra> <dec> <coordsys> <skyframe>]
             [savetemplate <filename>]
             [group new]
             [group <tag> new]
             [group <tag> update]
             [group <tag> select]
             [group <tag> color <color>]
             [group <tag> copy]
             [group <tag> delete]
             [group <tag> cut]
             [group <tag> font <font>]
             [group <tag> move <int> <int>]
             [group <tag> movefront]
             [group <tag> moveback]
             [group <tag> property <property> yes|no]
             [copy]
             [cut]
             [paste image|physical|wcs|wcsa...wcsz]
             [undo]
     
    Example:
    $ds9 -regions foo.reg
    $ds9 -regions -format ciao bar.reg # load as ciao format
    $ds9 -regions foo.fits # FITS regions files do not need a format specification
    $ds9 -regions load foo.reg # load foo.reg into current frame
    $ds9 -regions load all foo.reg # load foo.reg into all frames
    $ds9 -regions load '*.reg'# expand *.reg and load into current frame
    $ds9 -regions load all '*.reg' # expand *.reg and load into all frames
    $ds9 -regions save foo.reg
    $ds9 -regions list
    $ds9 -regions list close
    $ds9 -regions epsilon 5
    $ds9 -regions show yes
    $ds9 -regions showtext no
    $ds9 -regions centroid
    $ds9 -regions centroid auto yes
    $ds9 -regions centroid radius 10
    $ds9 -regions centroid iteration 20
    $ds9 -regions getinfo
    $ds9 -regions move back
    $ds9 -regions move front
    $ds9 -regions select all
    $ds9 -regions select none
    $ds9 -regions select invert
    $ds9 -regions delete all
    $ds9 -regions delete select
    $ds9 -regions format ds9
    $ds9 -regions system wcs
    $ds9 -regions sky fk5
    $ds9 -regions skyformat degrees
    $ds9 -regions delim nl
    $ds9 -regions strip yes
    $ds9 -regions shape ellipse
    $ds9 -regions color red
    $ds9 -regions width 3
    $ds9 -regions edit yes
    $ds9 -regions include
    $ds9 -regions command "circle 100 100 20 # color=red"
    $ds9 -regions composite
    $ds9 -regions dissolve
    $ds9 -regions template foo.tpl
    $ds9 -regions template foo.tpl at 13:29:55.92 +47:12:48.02 fk5
    $ds9 -regions savetemplate foo.tpl
    $ds9 -regions group new
    $ds9 -regions group foo new
    $ds9 -regions group foo update
    $ds9 -regions group foo select
    $ds9 -regions group foo color red
    $ds9 -regions group foo copy
    $ds9 -regions group foo delete
    $ds9 -regions group foo cut
    $ds9 -regions group foo font {times 14 bold}
    $ds9 -regions group foo move 100 100
    $ds9 -regions group foo movefront
    $ds9 -regions group foo moveback
    $ds9 -regions group foo property delete no
    $ds9 -regions copy
    $ds9 -regions cut
    $ds9 -regions paste wcs
    $ds9 -regions undo

    red

    For RGB frames, sets the current color channel to red.
    Syntax:
    -red
     
    Example:
    $ds9 -red foo.fits

    restore

    Restore DS9 to a previous state from a backup save set.
    Syntax:
    -restore <filename>
     
    Example:
    $ds9 -restore ds9.bck

    rgb

    Create RGB frame and control RGB frame parameters.
    Syntax:
    -rgb []
         [red|green|blue]
         [channel [red|green|blue]]
         [view [red|green|blue] [yes|no]]
         [system <coordsys>]
         [lock wcs|crop|slice|bin|scale|scalelimits|colorbar|block|smooth [yes|no]]
         [open|close]
     
    Example:
    $ds9 -rgb # create new rgb frame
    $ds9 -rgb red # set current channel to red
    $ds9 -rgb channel red # set current channel to red
    $ds9 -rgb view blue no # turn off blue channel
    $ds9 -rgb system wcs # set rgb coordinate system
    $ds9 -rgb lock wcs yes
    $ds9 -rgb lock crop yes
    $ds9 -rgb lock slice yes
    $ds9 -rgb lock bin yes
    $ds9 -rgb lock scale yes
    $ds9 -rgb lock scalelimits yes
    $ds9 -rgb lock colorbar yes
    $ds9 -rgb lock block yes
    $ds9 -rgb lock smooth yes
    $ds9 -rgb open
    $ds9 -rgb close

    rgbarray

    Load raw data array cube into rgb frame.
    Syntax:
    -rgbarray <filename>[[xdim=<x>,ydim=<y>|dim=<dim>],[zdim=3],bitpix=<b>,skip=<s>,endian=[little|big]]
     
    Example:
    $ds9 -rgbarray foo.arr[dim=512,zdim=3,bitpix=-32,endian=little]
    $cat foo.arr | ds9 -rgbarray -[dim=512,zdim=3,bitpix=-32,endian=little]

    rgbcube

    Load FITS rgbcube into rgb frame.
    Syntax:
    -rgbcube <filename>
     
    Example:
    $ds9 -rgbcube foo.fits
    $cat foo.fits | ds9 -rgbcube -

    rgbimage

    Load FITS rgbimage into rgb frame.
    Syntax:
    -rgbimage <filename>
     
    Example:
    $ds9 -rgbimage foo.fits
    $cat foo.fits | ds9 -rgbimage -

    rotate

    Controls the rotation angle (in degrees) of the current frame.
    Syntax:
    -rotate [<value>]
            [to <value>]
            [open|close]
     
    Example:
    $ds9 -rotate 45
    $ds9 -rotate to 30
    $ds9 -rotate open
    $ds9 -rotate close

    samp

    Enable/Disable SAMP protocol.
    Syntax:
    -samp [yes|no]
          [broadcast [image|table]]
          [send [image|table] <application>]
     
    Example:
    $ds9 -samp yes
    $ds9 -samp broadcast image
    $ds9 -samp send image aladin

    save

    Save loaded image data of current frame as FITS.
    Syntax:
    -save [fits|rgbimage|rgbcube|mecube|mosaic|mosaicimage] <filename> [image|table|slice]
     
    Example:
    $ds9 -save foo.fits
    $ds9 -save fits foo.fits image
    $ds9 -save fits foo.fits table
    $ds9 -save fits foo.fits slice
    $ds9 -save rgbimage foo.fits
    $ds9 -save rgbcube foo.fits
    $ds9 -save mecube foo.fits
    $ds9 -save mosaic foo.fits
    $ds9 -save mosaicimage foo.fits

    saveimage

    Create a snap shot of the current DS9 window and save in specified image format. If no format specified, the file name extension is used to determine the output format. Optional parameters: jpeg quality (1-100) and tiff compression method.
    Syntax:
    -saveimage [fits|eps|gif|tiff|jpeg|png] <filename>
    -saveimage <filename>.jpeg [1-100]
    -saveimage <filename>.tiff [none|jpeg|packbits|deflate]
     
    Example:
    $ds9 -saveimage ds9.tiff
    $ds9 -saveimage jpeg ds9.jpeg 75

    scale

    Controls the limits and color scale distribution.
    Syntax:
    -scale [linear|log|pow|sqrt|squared|asinh|sinh|histequ]
           [log exp <value>]
           [datasec yes|no]
           [limits <minvalue> <maxvalue>]
           [mode minmax|<value>|zscale|zmax]
           [scope local|global]
           [match]
           [match limits]
           [lock [yes|no]]
           [lock limits [yes|no]]
           [open|close]
     
    Example:
    $ds9 -scale linear
    $ds9 -scale log exp 100
    $ds9 -scale datasec yes
    $ds9 -scale histequ
    $ds9 -scale limits 1 100
    $ds9 -scale mode zscale
    $ds9 -scale mode 99.5
    $ds9 -scale scope local
    $ds9 -scale match
    $ds9 -scale match limits
    $ds9 -scale lock yes
    $ds9 -scale lock limits yes
    $ds9 -scale open
    $ds9 -scale close

    shm

    Load a shared memory segment into the current frame.
    Syntax:
    -shm [<key> [<filename>]]
         [key <id> [<filename>]]
         [shmid <id> [<filename>]]
         [fits [key|shmid] <id> [<filename>]]
         [mosaicimage [iraf|wcs|wcsa...wcsz|wfpc2] [key|shmid] <id> [<filename>]]
         [mosaicimagenext [wcs|wcsa...wcsz] [key|shmid] <id> [<filename>]]
         [mosaic [iraf|wcs|wcsa...wcsz] [key|shmid] <id> [<filename>]]
         [rgbcube [key|shmid] <id> [<filename>]]
         [rgbimage [key|shmid] <id> [<filename>]]
         [rgbarray [key|shmid] <id> [xdim=<x>,ydim=<y>|dim=<dim>,zdim=3],bitpix=<b>,[skip=<s>]]
         [array [key|shmid] <id> [xdim=<x>,ydim=<y>|dim=<dim>],bitpix=<b>,[skip=<s>]]
     
    Example:
    $ds9 -shm 102
    $ds9 -shm key 102
    $ds9 -shm shmid 102 foo
    $ds9 -shm fits 100 foo
    $ds9 -shm mosaicimage iraf key 100 foo
    $ds9 -shm mosaicimage wcs key 100 foo
    $ds9 -shm mosaicimage wcsa key 100 foo
    $ds9 -shm mosaicimage wfpc2 key 100 foo
    $ds9 -shm mosaicimagenext wcs key 100 foo
    $ds9 -shm mosaic iraf key 100 foo
    $ds9 -shm mosaic wcs key 100 foo
    $ds9 -shm rgbcube key 100 foo
    $ds9 -shm rgbimage key 100 foo
    $ds9 -shm rgbarray shmid 102 [dim=32,zdim=3,bitpix=-32]
    $ds9 -shm array shmid 102 [dim=32,bitpix=-32]

    single

    Set display mode to single.
    Syntax:
    -single
     
    Example:
    $ds9 -single

    skyview

    Support for SkyView image server at HEASARC.
    Syntax:
    -skyview []
             [<object>]
             [name <object>]
             [coord <ra> <dec> degrees|sexagesimal] # in wcs fk5
             [size <width> <height> degrees|arcmin|arcsec]
             [pixels <width> <height>]
             [save yes|no]
             [frame new|current]
             [update frame|crosshair]
             [survey <survey>]
             [open|close]
     
    Example:
    $ds9 -skyview
    $ds9 -skyview m31
    $ds9 -skyview name m31
    $ds9 -skyview coord 00:42:44.404 +41:16:08.78 sexagesimal
    $ds9 -skyview size 60 60 arcmin
    $ds9 -skyview pixels 600 600
    $ds9 -skyview save yes
    $ds9 -skyview frame current
    $ds9 -skyview update frame
    $ds9 -skyview survey sdssi
    $ds9 -skyview open
    $ds9 -skyview close

    sleep

    Delays execution for specified number of seconds. Default is 1 second.
    Syntax:
    -sleep [#]
     
    Example:
    $ds9 -sleep
    $ds9 -sleep 2

    slice
    noslice

    Indicates next files loaded are to treated as slices of a cube. Can be disabled with noslice command.
    Syntax:
    -slice <filename>
    -noslice
     
    Example:
    $ds9 -slice *.fits
    $ds9 -noslice

    smooth

    Smooth current image or set smooth parameters.
    Syntax:
    -smooth []
            [yes|no]
            [function boxcar|tophat|gaussian]
            [radius <int>]
            [open|close]
            [match]
            [lock [yes|no]]
     
    Example:
    $ds9 -smooth
    $ds9 -smooth yes
    $ds9 -smooth function tophat
    $ds9 -smooth radius 4
    $ds9 -smooth open
    $ds9 -smooth close
    $ds9 -smooth match
    $ds9 -smooth lock yes

    squared

    Select squared scale function for the current frame.
    Syntax:
    -squared
     
    Example:
    $ds9 -squared

    sqrt

    Select square soot scale function for the current frame.
    Syntax:
    -sqrt
     
    Example:
    $ds9 -sqrt

    source

    Source TCL code from a file.
    Syntax:
    -source filename
     
    Example:
    $ds9 -source extensions.tcl

    tcl

    Enable tcl commands to be executed via XPA or SAMP. This can be a major security risk and is disabled by default. Please use with caution.
    Syntax:
    -tcl [yes|no]
     
    Example:
    $ds9 -tcl yes

    threads

    Set number of process threads for functions which are multi-threaded.
    Syntax:
    -threads #
     
    Example:
    $ds9 -threads 8

    tiff

    Load TIFF image into current frame.
    Syntax:
    -tiff <filename>
     
    Example:
    $ds9 -tiff foo.tiff
    $cat foo.fits | ds9 -tiff -

    tile

    Controls the tile display mode.
    Syntax:
    -tile []
          [yes|no]
          [mode grid|column|row]
          [grid]
          [grid mode automatic|manual]
          [grid direction x|y]
          [grid layout <col> <row>]
          [grid gap <pixels>]
          [row]
          [column]
     
    Example:
    $ds9 -tile
    $ds9 -tile yes
    $ds9 -tile mode row
    $ds9 -tile grid
    $ds9 -tile grid mode manual
    $ds9 -tile grid direction x
    $ds9 -tile grid layout 5 5
    $ds9 -tile grid gap 10
    $ds9 -tile row
    $ds9 -tile column

    title

    Changes the display window title to the specified name.
    Syntax:
    -title name
     
    Example:
    $ds9 -title Voyager

    unix

    Set the IRAF unix socket name, used by IRAF to communicate with DS9. The default is /tmp/.IMT%d, so that the standard IRAF unix socket is defined.
    Syntax:
    -unix name
     
    Example:
    $ds9 -unix "/tmp/.IMT%d"

    unix_only

    Only use the IRAF unix socket name. This is the same as -fifo none -port 0.
    Syntax:
    -unix_only
     
    Example:
    $ds9 -unix_only

    update

    Updates the current frame or region of frame. In the second form, the first argument is the number of the fits HDU (starting with 1) and the remaining args are a bounding box in IMAGE coordinates. By default, the screen is updated the next available idle cycle. However, you may force an immediate update by specifying the NOW option.
    Syntax:
    -update []
            [# x1 y1 x2 y2]         [now]
            [now # x1 y1 x2 y2]
            [on]
            [off]
     
    Example:
    $ds9 -update
    $ds9 -update 1 100 100 300 400
    $ds9 -update now
    $ds9 -update now 1 100 100 300 400
    $ds9 -update off # delay refresh of the screen while loading files
    $ds9 -update on # be sure to turn it on when you are finished loading

    url

    Load FITS from URL into the current frame
    Syntax:
    -url <url>
     
    Example:
    $ds9 -url http://foo.bar.edu/foo.fits

    version

    Returns the current version of DS9 and exits.
    Syntax:
    -version
     
    Example:
    $ds9 -version

    view

    Controls the GUI and visible RGB frame color channels.
    Syntax:
    -view [layout horizontal|vertical]
          [keyvalue <string>]
          [info yes|no]
          [panner yes|no]
          [magnifier yes|no]
          [buttons yes|no]
          [colorbar yes|no]
          [graph horizontal|vertical yes|no]
          [filename yes|no]
          [object yes|no]
          [keyword yes|no]
          [minmax yes|no]
          [lowhigh yes|no]
          [units yes|no]
          [image|physical|wcs|wcsa...wcsz yes|no]
          [frame yes|no]
          [red yes|no]
          [green yes|no]
          [blue yes|no]
     
    Example:
    $ds9 -view layout vertical
    $ds9 -view keyvalue BITPIX
    $ds9 -view info yes
    $ds9 -view panner yes
    $ds9 -view magnifier yes
    $ds9 -view buttons yes
    $ds9 -view colorbar yes
    $ds9 -view graph horizontal yes
    $ds9 -view filename yes
    $ds9 -view object yes
    $ds9 -view keyword yes
    $ds9 -view minmax yes
    $ds9 -view lowhigh yes
    $ds9 -view wcsa yes
    $ds9 -view frame yes
    $ds9 -view red yes
    $ds9 -view green yes
    $ds9 -view blue yes

    visual

    Force DS9 to use the specified color visual. This argument MUST be the first argument listed. Requires the visual be available.
    Syntax:
    -visual [pseudocolor|pseudocolor8|truecolor|truecolor8|truecolor16|truecolor24]  
    Example:
    $ds9 -visual truecolor24

    vla

    Support for VLA Sky Survey.
    Syntax:
    -vla []
           [<object>]
           [name <object>]
           [coord <ra> <dec> degrees|sexagesimal] # in wcs fk5
           [size <width> <height> degrees|arcmin|arcsec]
           [save yes|no]
           [frame new|current]
           [update frame|crosshair]
           [survey first|stripe82]
           [open|close]
     
    Example:
    $ds9 -vla
    $ds9 -vla m31
    $ds9 -vla name m31
    $ds9 -vla coord 00:42:44.404 +41:16:08.78 sexagesimal
    $ds9 -vla size 60 60 arcmin
    $ds9 -vla save yes
    $ds9 -vla frame current
    $ds9 -vla update frame
    $ds9 -vla survey stripe82
    $ds9 -vla open
    $ds9 -vla close

    vo

    Invoke an connection to a Virtual Observatory site.
    Syntax:
    -vo [method xpa|mime]
        [server <url>]
        [internal yes|no]
        [delay #]
        [<url>]
        [connect <url>]
        [disconnect <url>]
        [open|close]
     
    Example:
    $ds9 -vo method xpa
    $ds9 -vo server "http://foo.bar.edu/list.txt"
    $ds9 -vo internal yes
    $ds9 -vo delay 15 # keep-alive delay
    $ds9 -vo chandra-ed
    $ds9 -vo connect chandra-ed
    $ds9 -vo disconnect chandra-ed
    $ds9 -vo open
    $ds9 -vo close

    wcs

    Controls the World Coordinate System for the current frame. If the wcs system, skyframe, or skyformat is modified, the info panel, compass, grid, and alignment will be modified accordingly. Also, a new WCS specification can be loaded and used by the current image regardless of the WCS that was contained in the image file. Please see WCS for more information.
    Syntax:
    -wcs [[system] wcs|wcsa...wcsz]
         [[sky] fk4|fk5|icrs|galactic|ecliptic]
         [[skyformat] degrees|sexagesimal]
         [align yes|no]
         [reset [#]]
         [replace [#] <filename>]
         [append [#] <filename>]
         [open|close]
     
    Example:
    $ds9 -wcs wcs
    $ds9 -wcs system wcs
    $ds9 -wcs fk5
    $ds9 -wcs sky fk5
    $ds9 -wcs sexagesimal
    $ds9 -wcs skyformat sexagesimal
    $ds9 -wcs align yes
    $ds9 -wcs reset
    $ds9 -wcs reset 3
    $ds9 -wcs replace foo.wcs
    $ds9 -wcs replace 3 foo.wcs
    $ds9 -wcs append foo.wcs
    $ds9 -wcs append 3 foo.wcs
    $ds9 -wcs open
    $ds9 -wcs close

    web

    Display specified URL in the web display.
    Syntax:
    -web [new|<webname>] [<url>]
         [<webname>] [click back|forward|stop|reload|#]
         [<webname>] [clear]
         [<webname>] [close]
     
    Example:
    $ds9 -web www.cnn.com
    $ds9 -web new www.cnn.com
    $ds9 -web hvweb www.apple.com
    $ds9 -web click back
    $ds9 -web click 2
    $ds9 -web clear
    $ds9 -web close

    width

    Set the width of the image display window. Use the geometry command to set the overall width and height of the ds9 window.
    Syntax:
    -width [<value>]
     
    Example:
    $ds9 -width 512

    xpa

    Configure XPA at startup.
    Syntax:
    -xpa [yes|no]
         [inet|local|unix|localhost]
         [noxpans]
     
    Example:
    $ds9 -xpa no
    $ds9 -xpa local
    $ds9 -xpa noxpans

    zmax

    Set Scale Limits based  on the IRAF algorithm and maximum data value.
    Syntax:
    -zmax
     
    Example:
    $ds9 -zmax

    zscale

    Set Scale Limits based  on the IRAF algorithm.
    Syntax:
    -zscale []
            [contrast]
            [sample]
            [line]
     
    Example:
    $ds9 -zscale
    $ds9 -zscale contrast .25
    $ds9 -zscale sample 600
    $ds9 -zscale line 120

    zoom

    Controls the current zoom value for the current frame.
    Syntax:
    -zoom [<value>]
          [<value> <value>]
          [to <value>]
          [to <value> <value>]
          [in]
          [out]
          [to fit]
          [open|close]
     
    Example:
    $ds9 -zoom 2
    $ds9 -zoom 2 4
    $ds9 -zoom to 4
    $ds9 -zoom to 2 4
    $ds9 -zoom in
    $ds9 -zoom out
    $ds9 -zoom to fit
    $ds9 -zoom open
    $ds9 -zoom close
*/
