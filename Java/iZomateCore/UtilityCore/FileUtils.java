package iZomateCore.UtilityCore;

import iZomateCore.LogCore.Logs;
import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileUtils {
	public static String fileName( String strFile ) {
		return strFile.substring( strFile.lastIndexOf( "/" )+1 );
	}
	
	public static File copyFile(String sourceFile, String destFile) throws IOException {
		return FileUtils.copyFile( new File(sourceFile), new File(destFile) );
	}

	public static File copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists())
	        destFile.createNewFile();

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null)
	            source.close();
	        if(destination != null)
	            destination.close();
	    }
	    
	    return destFile;
	}
	
	public static boolean _CompareImages( String strImageFile1, String strImageFile2, String strDiffImageFile, Logs pLogs ) throws IOException {
		boolean bStatus= false;
    	RandomAccessFile raf1 = null;
    	RandomAccessFile raf2 = null;
    	RandomAccessFile raf3 = null;
    	byte[] imageData1 = null;
    	byte[] imageData2 = null;

    	try
    	{
    		File f1= new File( strImageFile1 );
    		File f2= new File( strImageFile2 );
    		raf1 = new RandomAccessFile(f1, "r");
    		raf2 = new RandomAccessFile(f2, "r");
    		imageData1 = new byte[(int) raf1.length()];
    		imageData2 = new byte[(int) raf2.length()];
    		raf1.read(imageData1, 0, imageData1.length); 
    		raf2.read(imageData2, 0, imageData2.length);
    		
    		if (imageData1[0] == 'B' && imageData1[1] == 'M')
    		{
    			int pixelSize= 3; //RGB
    			int img1FileSize = BaseChunk.convertByte4ToInt(imageData1, 2, true);
    			int img1DataStart = BaseChunk.convertByte4ToInt(imageData1, 10, true);
    			int img1width = BaseChunk.convertByte4ToInt(imageData1, 18, true);
    			int img1height = BaseChunk.convertByte4ToInt(imageData1, 22, true);
    			int img1LineSize = pixelSize*img1width;

    			int img2FileSize = BaseChunk.convertByte4ToInt(imageData2, 2, true);
    			//int img2DataStart = BaseChunk.convertByte4ToInt(imageData2, 10, true);
    			int img2width = BaseChunk.convertByte4ToInt(imageData2, 18, true);
    			int img2height = BaseChunk.convertByte4ToInt(imageData2, 22, true);
    			
    			int imgByteAlgnmntDelta = img1LineSize % 4 > 0 ? 4 - img1LineSize % 4 : 0;
    			
    			if( img1FileSize != img2FileSize )
    				pLogs._ResultLog()._logError( "Images size mismatch: " + img1FileSize + " : " + img2FileSize, false );
    			else if( img1height != img2height || img1width != img2width )
    				pLogs._ResultLog()._logError( "Images dimension mismatch: " + img1width + " x " + img1height + " : " + img2width + " x " + img2height, false );
    			else {
    				String badPixels= "";
	    			int imgByte = img1DataStart;
	    			for (int h = 0; h < img1height; ++h)
	    			{					
	    				for (int p = 0; p < img1width; p++)
	    				{
	    					boolean bMatch= true;
	    					// Compare each byte in the pixel
	    					for( int b= 0; b < pixelSize; ++b) {
	    						if( (0xFF&imageData1[imgByte+(p*pixelSize)+b]) != (0xFF&imageData2[imgByte+(p*pixelSize)+b]) ) {
	    							bMatch= false;
	    							if( !badPixels.isEmpty() )
	    								badPixels+= ", ";
	    							
	    							badPixels+= "(" + h + ", " + p + ")";	    							
	    							break;
	    						}
	    					}
	    					
	    					// If it is a match then turn it black.  If not a match then turn it white
	    	    			byte sVal= bMatch ? (byte)0xF0 : (byte)0;
		    				for( int b= 0; b < pixelSize; ++b)
		    					imageData2[imgByte+(p*pixelSize)+b]= sVal;
	    				}
	    				
	    				//move to the start of the next line
	    				imgByte+= img1LineSize + imgByteAlgnmntDelta;
	    			}
	    			
	    			if( badPixels.isEmpty() ) {
	    				pLogs._ResultLog()._logLine( "Images are identical\n" );
    					pLogs._ResultLog()._logImage( f2.getName() );
	    			}
	    			else {
	    				File f3= new File( strDiffImageFile );
	        			raf3 = new RandomAccessFile(f3, "rw");
	            		raf3.write( imageData2 );
	            		raf3.close();
	    				File fRef= FileUtils.copyFile( strImageFile1, pLogs._GetLogsDir() + "/" + FileUtils.fileName(strImageFile1).replace( ".", "_ref.") );
	    				
	    				pLogs._ResultLog()._logError( "Differences found at pixels: " + badPixels, false );
    					pLogs._ResultLog()._logString( "Ref: " );
    					pLogs._ResultLog()._logImage( fRef.getName() );
    					pLogs._ResultLog()._logString( "   New: " );
    					pLogs._ResultLog()._logImage( f2.getName() );
    					pLogs._ResultLog()._logString( "   Diff: " );
	    				pLogs._ResultLog()._logImage( f3.getName() );
	    			}
        		}
    		}
    		else
    			pLogs._ResultLog()._logError( "Wrong Image Format", false );
     	}
    	catch (Exception e)
    	{
    		bStatus= false;
    		if (raf1 != null)
    			raf1.close();
    		if (raf2 != null)
    			raf2.close();
    		if (raf3 != null)
    			raf3.close();
    	}
	
		return bStatus;
	}


}
