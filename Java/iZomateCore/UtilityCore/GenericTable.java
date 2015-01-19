package iZomateCore.UtilityCore;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class GenericTable {
	private XYSeries[] 	mXYSeries;
	private JFreeChart 	mChart;
	private int			mDataSetCount= 0;
	private int			mXAxisStartVal= -1;
	private String 		mTableName;
	private String		mXLabel, mYLabel;
	private Range		mYAxisRange= null;
	private Color[]		mColors= null;
	
	/**
	 * 
	 * @param tableName
	 * @param xLabel
	 * @param yLabel
	 * @param plots
	 * @throws Exception
	 */
	public GenericTable( String tableName, String xLabel, String yLabel, String[] plots ) throws Exception {
		this.mTableName= tableName;
		this.mXLabel= xLabel;
		this.mYLabel= yLabel;

		this.mXYSeries= new XYSeries[ plots.length ];
		for( int i= 0; i < plots.length; ++i )
			this.mXYSeries[i]= new XYSeries( plots[i] );		
	}
	
	/**
	 * 
	 * @param startVal
	 * @return
	 */
	public GenericTable _SetXAxisStartVal( int startVal ) {
		this.mXAxisStartVal= startVal;
		return this;
	}
	
	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public GenericTable _SetYAxisRange( double min, double max ) {
		this.mYAxisRange= new Range( min, max );
		return this;
	}

	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public GenericTable _SetLineColors( Color... colors  ) {
		this.mColors= colors;
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public int _GetXAxisStartVal() {
		if( this.mXAxisStartVal == -1 )
			return this.mDataSetCount;

		return this.mXAxisStartVal;
	}
	
	/**
	 * 
	 * @param dataArrays
	 * @param bCalcMinMaxYRange
	 * @return
	 * @throws Exception
	 */
	public GenericTable _SetDataFromArray( ArrayList<ArrayList<Double>> dataArrays, boolean bCalcMinMaxYRange ) throws Exception {
		if( dataArrays == null )
			return this;
		
		if( dataArrays.size() != this.mXYSeries.length )
			throw new Exception( "SetData error: dataArrays size does not match the XY Series size for table " + this.mTableName );
		
		double[] d= new double[dataArrays.size() * dataArrays.get( 0 ).size()];
		for( int i= 0; i < dataArrays.get( 0 ).size(); i++ ) {
			for( int x= 0; x < dataArrays.size(); x++ )
				d[i*dataArrays.size() + x]= dataArrays.get( x ).get( i ).doubleValue();
		}		
		return this._SetData( d, bCalcMinMaxYRange );
	}	
	
	/**
	 * 
	 * @param data
	 * @param bCalcMinMaxYRange
	 * @return
	 * @throws Exception
	 */
	public GenericTable _SetData( ArrayList<Double> data, boolean bCalcMinMaxYRange ) throws Exception {
		if( data.size() % this.mXYSeries.length != 0 )
			throw new Exception( "SetData error: data size is not a multiple of the XY Series size for table " + this.mTableName );
		
		double[] d= new double[data.size()];
		for (int i = 0; i < data.size(); i++)
		    d[i] = data.get( i ).doubleValue();
		
		return this._SetData( d, bCalcMinMaxYRange );
	}	

	/**
	 * 
	 * @param data
	 * @param bCalcMinMaxYRange
	 * @return
	 * @throws Exception
	 */
	public GenericTable _SetData( double[] data, boolean bCalcMinMaxYRange  ) throws Exception {
		return this._SetData( data, null, bCalcMinMaxYRange );
	}

	/**
	 * 
	 * @param data
	 * @param strHistoryDataFile
	 * @param bCalcMinMaxYRange
	 * @return
	 * @throws Exception
	 */
	public GenericTable _SetData( double[] data, String strHistoryDataFile, boolean bCalcMinMaxYRange ) throws Exception {
		double dMin=0, dMax=0;
		boolean bMinMaxSet= false;
		RandomAccessFile statHistoryFile= null;
		
		if( data == null )
			return this;
		
		if( data.length % this.mXYSeries.length != 0 )
			throw new Exception( "SetData error: data size is not a multiple of the XY Series size for table " + this.mTableName );
		
		if( strHistoryDataFile != null ) {
			// Make the data dir it does not exist
			File f= new File( strHistoryDataFile );
			if( !f.getParentFile().exists() )
				f.getParentFile().mkdirs();
			
			statHistoryFile= new RandomAccessFile( strHistoryDataFile, "rw" );
		}
		
		if( statHistoryFile != null ) {
			// First get the data from the history file on disk
			String theLine= null;
			while ( (theLine= statHistoryFile.readLine()) != null ) {
				this.mXYSeries[0].add( Double.parseDouble( theLine ), Double.parseDouble( statHistoryFile.readLine() ) );
				for( int i= 1; i < this.mXYSeries.length; ++i ) {
					double x= Double.parseDouble( statHistoryFile.readLine() );
					double y= Double.parseDouble( statHistoryFile.readLine() );
					this.mXYSeries[i].add( x, y );
				}
				
				this.mDataSetCount++;

				if( bCalcMinMaxYRange ) {
					for( int i=0; i< this.mXYSeries.length; i++ ) {
						double dVal= this.mXYSeries[i].getY( this.mXYSeries[i].getItemCount()-1 ).doubleValue();
					
						if( !bMinMaxSet ) {
							dMin= dVal;
							dMax= dVal;
							bMinMaxSet= true;
						}
						else if( dVal < dMin )
							dMin= dVal;
						else if( dVal > dMax )
							dMax= dVal;
					}
				}
			}
			
			if( this.mXAxisStartVal == -1 && !this.mXYSeries[0].isEmpty() )
				this._SetXAxisStartVal( this.mXYSeries[0].getX( this.mXYSeries[0].getItemCount()-1 ).intValue() + 1 );			
		}
		
		// If the new data start with the last data in the tabe then remove the old data before adding the new data
//		if( this.mXYSeries[0].getItemCount() > 0 && data[0] == this.mXYSeries[0].getX( this.mXYSeries[0].getItemCount()-1 ).doubleValue() )
//			for( int s= 0; s < this.mXYSeries.length; ++s )
//				this.mXYSeries[s].remove( this.mXYSeries[s].getItemCount()-1 );
		
		int xStart= this._GetXAxisStartVal();
		// Append the new data and write it out to the history file
		for( int i= 0; i < data.length; ) {
			for( int s= 0; s < this.mXYSeries.length; ++s, i++ ) {
				this.mXYSeries[s].add( xStart, data[i] );
				
				if( bCalcMinMaxYRange ) {					
					if( !bMinMaxSet ) {
						dMin= data[i];
						dMax= data[i];
						bMinMaxSet= true;
					}
					else if( data[i] < dMin )
						dMin= data[i];
					else if( data[i] > dMax )
						dMax= data[i];
				}

				if( statHistoryFile != null ) {
					statHistoryFile.write( (this.mXYSeries[s].getX( this.mDataSetCount ) + "\n" + 
											this.mXYSeries[s].getY( this.mDataSetCount ) + "\n").getBytes() );
				}
			}
			this.mDataSetCount++;
			xStart++;
		}
				
		if( statHistoryFile != null )
			statHistoryFile.close();
		
		if( bCalcMinMaxYRange && bMinMaxSet ) {
			if( dMin != dMax )
				this._SetYAxisRange( dMin, dMax );
			else if( dMin == 0 )
				this._SetYAxisRange( -1, 1 );
			else
				this._SetYAxisRange( dMin - Math.abs(dMin*.1), dMax + Math.abs(dMax*.1) );
		}
		
		return this;
	}
	
	/**
	 * 
	 * @param strFile
	 * @param x
	 * @return
	 * @throws IOException
	 */
	public File _CreateImageFile( String strFile ) throws IOException {
		strFile= strFile.replace( ".txt", ".png" );
		File jpegFile= new File( strFile.endsWith( ".png" ) ? strFile : (strFile + ".png") );

		XYSeriesCollection xyDataCollection = new XYSeriesCollection();
		for( int i= 0; i < this.mXYSeries.length; ++i )
			xyDataCollection.addSeries( this.mXYSeries[i] );
		
		this.mChart= ChartFactory.createXYLineChart( this.mTableName, this.mXLabel, this.mYLabel, xyDataCollection, PlotOrientation.VERTICAL, true, true, true );
        
		if( this.mYAxisRange != null )
			this.mChart.getXYPlot().getRangeAxis().setRange( this.mYAxisRange );	
		
        this.mChart.getXYPlot().getDomainAxis().setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
		this.mChart.getXYPlot().setDomainCrosshairVisible( true );
		this.mChart.getXYPlot().setRangeCrosshairVisible( true );
		
		XYItemRenderer r = this.mChart.getXYPlot().getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
        }
        
        if( this.mColors != null ) {
        	int n= this.mXYSeries.length < this.mColors.length ? this.mXYSeries.length : this.mColors.length;
			for( int i= 0; i < n; ++i )
				((XYPlot) this.mChart.getPlot()).getRenderer().setSeriesPaint(i, this.mColors[i]);
        }

		ChartUtilities.saveChartAsPNG( jpegFile, this.mChart, 600, 300 );
		return jpegFile;
	}

}
