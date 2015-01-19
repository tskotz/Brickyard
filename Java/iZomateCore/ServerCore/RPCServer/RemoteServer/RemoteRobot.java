package iZomateCore.ServerCore.RPCServer.RemoteServer;

import iZomateCore.AppCore.AppEnums.Images;
import iZomateCore.AppCore.AppEnums.WindowControls.MouseButtons;
import iZomateCore.ServerCore.CoreEnums.RPCImageFormat;
import iZomateCore.ServerCore.RPCServer.IncomingReply;
import iZomateCore.ServerCore.RPCServer.OutgoingRequest;
import iZomateCore.ServerCore.RPCServer.RPCServer;
import iZomateCore.UtilityCore.TimeUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;

/**
 * Represents a java.awt.Robot object on a remote system, accessed through the
 * RemoteServer. This object can be used to manipulate the remote system by sending it events.
 * It also supports capturing screen shots.
 */
public final class RemoteRobot
{
	/**
     * The iZomate RPC server from which this object originated.
     */
    protected final RPCServer mServer;
    private int mImageClickFuzzFactor= 40;

    /**
     * Constructor.
     *
     * @param srvr the RemoteLauncher which will host the actual java.awt.Robot that this object represents.
     * @throws Exception
     */
    public RemoteRobot(RPCServer srvr) throws Exception
    {
        this.mServer= srvr;
    }

    /**
     * Returns the current value set for the allowable RGB value delta between pixels in image file and desktop image when _ImageClick call is used
     * 
     * @param fuzzFactor
     * @throws Exception
     */
    public int _GetImageClickFuzzFactor() throws Exception
    {
        return this.mImageClickFuzzFactor;
    }

    /**
     * Sets the allowable RGB value delta between pixels in image file and desktop image when _ImageClick call is used.
     * Range is from 0 to 255
     * 
     * @param fuzzFactor 
     * @throws Exception
     */
    public void _SetImageClickFuzzFactor( int fuzzFactor ) throws Exception
    {
        this.mImageClickFuzzFactor= fuzzFactor>255 ? 255 : fuzzFactor<0 ? 0 : fuzzFactor;
    }

    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @param clicks
     * @param offset
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(Images imageFile, int clicks, Point offset) throws Exception
    {
    	return this._imageClickII( imageFile._GetValue(), clicks, offset );
    }

    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @param clicks
     * @param offset
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(String imageFile, int clicks, Point offset) throws Exception
    {
    	Point p = this._imageClick(imageFile, InputEvent.BUTTON1_MASK, clicks, offset, TimeUtils._GetDefaultTimeout());
    	if (p != null)
    		TimeUtils.sleep(.8);
    	return p;
    }
    
    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @param button
     * @param clicks
     * @param offset
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(String imageFile, int button, int clicks, Point offset, int timeout) throws Exception
    {
    	RandomAccessFile ras = null;
    	byte[] bytes = null;
    	Point p = null;
    	
    	try
    	{
    		ras = new RandomAccessFile(imageFile, "r");
    		bytes = new byte[(int) ras.length()];
    		ras.read(bytes, 0, bytes.length); 
     	}
    	catch (Exception e)
    	{
    		if (ras != null)
    			ras.close();
    		throw e;
    	}
    	
    	OutgoingRequest req = this.mServer._createRequest("clickImage");
        req._addInt32(button, "button");
        req._addInt32(clicks, "clicks");
        req._addInt32(this.mImageClickFuzzFactor, "fuzzFactor");
        req._addBuffer(bytes, "data");
        
        if (offset != null)
        {
            req._addInt32(offset.x, "xOffset");
            req._addInt32(offset.y, "yOffset");       	
        }            

        IncomingReply reply = this.mServer._processRequest(req);
        
        if (reply._exists("Error"))
        {
        	String error = reply._getString("Error");
        	if (!error.equals("Could not find image!")) {
        		if (ras != null)
        			ras.close();
        		throw new Exception(error);
        	}
        }
        else
        	p = new Point(reply._getInt32("x"), reply._getInt32("y"));

        if (ras != null)
			ras.close();
        
    	return p;
    }

    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @param clicks
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(Images imageFile, int clicks) throws Exception
    {
    	return this._imageClick(imageFile._GetValue(), clicks, null);
    }

    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @param clicks
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(String imageFile, int clicks) throws Exception
    {
    	return this._imageClick(imageFile, clicks, null);
    }

    /**
     * Searches the screen for the image and clicks on it if found.  Attempts numTries times if not found.
     * 
     * @param imageFile
     * @param clicks
     * @param numTries
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(String imageFile, int clicks, int numTries) throws Exception
    {
    	return this._imageClick( imageFile, clicks, numTries, false );
    }

    /**
     * Searches the screen for the image and clicks on it if found.  Attempts numTries times if not found.
     * 
     * @param imageFile
     * @param clicks
     * @param numTries
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(Images imageFile, int clicks, int numTries) throws Exception
    {
    	return this._imageClick( imageFile._GetValue(), clicks, numTries, false );
    }

    /**
     * Searches the screen for the image and clicks on it if found.  Will try numTries times.
     * Throws exception if not found after last try if require is true
     * 
     * @param imageFile
     * @param clicks
     * @param numTries
     * @param require
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(Images imageFile, int clicks, int numTries, boolean require) throws Exception
    {
    	return this._imageClick( imageFile._GetValue(), clicks, numTries, require );
    }

    /**
     * Searches the screen for the image and clicks on it if found.  Will try numTries times.
     * Throws exception if not found after last try if require is true
     * 
     * @param imageFile
     * @param clicks
     * @param numTries
     * @param require
     * @param timeout
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(Images imageFile, int clicks, int numTries, boolean require, int timeout) throws Exception
    {
    	return this._imageClick( imageFile._GetValue(), clicks, numTries, require, timeout );
    }

    /**
     * Searches the screen for the image and clicks on it if found.  Will try numTries times.
     * Throws exception if not found after last try if require is true
     * 
     * @param imageFile
     * @param clicks
     * @param numTries
     * @param require
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(String imageFile, int clicks, int numTries, boolean require) throws Exception
    {
    	return this._imageClick(imageFile, clicks, numTries, require, TimeUtils._GetDefaultTimeout());
    }
    
    /**
     * Searches the screen for the image and clicks on it if found.  Will try numTries times.
     * Throws exception if not found after last try if require is true
     * 
     * @param imageFile
     * @param clicks
     * @param numTries
     * @param require
     * @param timeout
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClick(String imageFile, int clicks, int numTries, boolean require, int timeout) throws Exception
    {
    	Point p = this._imageClick(imageFile, clicks, null);
    	for (int i = 0; i < numTries && p == null; ++i)
    	{
    		TimeUtils.sleep(.5);
    		p = this._imageClick(imageFile, clicks, null); //try again
    	}
    	
    	if( require && p == null )
    		throw new Exception("Required Image not found: " + imageFile );
    	
    	return p;
    }

    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClickII(String imageFile, int clicks) throws Exception
    {
    	Point p = this._imageClick(imageFile, clicks, null);
    	for (int i = 0; i < 10 && p == null; ++i)
    	{
    		TimeUtils.sleep(.5);
    		p = this._imageClick(imageFile, clicks, null); //try again
    	}
    	
    	return p;
    }

    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageClickII(String imageFile, int clicks, Point offset) throws Exception
    {
    	Point p = this._imageClick(imageFile, clicks, offset);
    	
    	if (p == null)
    	{
    		TimeUtils.sleep(.7);
    		p = this._imageClick(imageFile, clicks, offset); //try again
    	}
    	
    	return p;
    }

    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @param clicks
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageFind(String imageFile) throws Exception
    {
    	return this._imageClick(imageFile, 0, null);
    }
    
    /**
     * Searches the screen for the image and clicks on it if found.
     * 
     * @param imageFile
     * @param clicks
     * @return The click point or null
     * @throws Exception
     */
    public Point _imageFind(Images imageFile) throws Exception
    {
    	return this._imageClick(imageFile._GetValue(), 0, null);
    }
    
    /**
     * 
     * @param imageFile
     * @param numTries
     * @param require
     * @return
     * @throws Exception
     */
    public Point _imageFind(Images imageFile, int numTries, boolean require) throws Exception
    {
    	return this._imageFind( imageFile._GetValue(), numTries, require );
    }
    
    /**
     * 
     * @param imageFile
     * @param numTries
     * @param require
     * @return
     * @throws Exception
     */
    public Point _imageFind(String imageFile, int numTries, boolean require) throws Exception
    {
    	Point p= this._imageFind( imageFile );
		for (int i = 0; i < numTries && p == null; ++i)
		{
			TimeUtils.sleep(.5);
			p = this._imageFind(imageFile); //try again
		}
	
		if( require && p == null )
			throw new Exception("Required Image not found: " + imageFile );
	
		return p;
    }


    /**
     * Gets the screen image between to mouse points.  You must step through to use properly.
     * 
     * @param localFilePath
     * @return
     * @throws Exception
     */
	public Point _imageGetMouseImage(String localDirPath, String defaultName) throws Exception
	{
		JOptionPane.showMessageDialog( null, "Move mouse to top left corner of image to capture and press enter. " );
		Point p = this._mouseGetLocation();
		JOptionPane.showMessageDialog( null, "Move mouse to bottom right corner of image to capture and press enter. " );
		Point p2 = this._mouseGetLocation();
		JOptionPane.showMessageDialog( null, "Move mouse to desired click point to calculate an optional offset relative to image and press enter. " );
		Point offset = this._mouseGetLocation();

		JOptionPane.showMessageDialog( null, "Press enter to grab screen image.  Make sure mouse is out of the way" );

		int w = p.equals(p2) ? 58 : Math.abs(p2.x - p.x);
		int h = p.equals(p2) ? 10 : Math.abs(p2.y - p.y);
		
		String fname = JOptionPane.showInputDialog( null, "Give the image a name:", defaultName );

		String imageFile= this._imageGrabAndSave( new Rectangle(p.x, p.y, w, h), localDirPath, fname );
		
		JOptionPane.showInputDialog( null, "The image has been saved to:", imageFile );
	
		offset.setLocation(offset.x-p.x, offset.y-p.y);
		return offset;
	}

	/**
	 * 
	 * @param rect
	 * @param localDirPath
	 * @param fname
	 * @throws Exception
	 */
	public String _imageGrabAndSave(Rectangle rect, String localDirPath, String fname) throws Exception
	{
		byte[] scrData = this._screenDataGet(rect, RPCImageFormat.BMP, null);

		if( !fname.endsWith( ".bmp" ) )
			fname += ".bmp";
		
		File dir= new File(localDirPath);
		if( !dir.exists() )
			dir.mkdirs();

		ImageIO.write(ImageIO.read(new ByteArrayInputStream(scrData)), "bmp", new File(dir.getPath() + "/" + fname));
		
		return dir.getPath() + "/" + fname;
	}

    /**
     * Captures the remote system's default screen(s) and saves it as a JPEG file on the remote system. NOTE:
     * On systems with multiple monitors, the corresponding createScreenCapture method of ExportRobot will
     * replace ScreenShot1234_123456.jpg with ScreenShot1234_1234561.jpg, and then ScreenShot1234_1234562.jpg
     * and so on.
     *
     * @param pathName The full file path and name for the JPEG files. (For Example:
     *            "C://iZomateData//output//screenshots//ScreenShot1234_123456.jpg")
     * @return The path(s) for the newly created screen shots on the remote system.
     * @throws Exception
     */
    public String[] _createScreenCapture(String pathName) throws Exception
    {
        return this._createScreenCapture(pathName, ".jpg");
    }

    /**
     * Captures all of the remote system's screens and saves them as JPEG files on the remote system. This
     * method will create screenshots for one and/or multiple monitor systems. NOTE: By passing the string
     * value ".jpg" as the placeHolder parameter, you will cause the corresponding createScreenCapture method
     * of ExportRobot to replace ScreenShot1234_123456.jpg with ScreenShot1234_1234561.jpg, and then
     * ScreenShot1234_1234562.jpg and so on.
     *
     * @param pathName The full file path and name for the JPEG files. (For Example:
     *            "C://iZomateData//output//screenshots//ScreenShot1234_123456.jpg")
     * @param placeHolder This string must appear exactly once in pathName. It will be replaced by sequential
     *            integers, one for each screen on the remote system, starting with 1. (For Example: if the
     *            original name is 'MyScreenShot', and you pass 'Shot' as the placeholder, the new name will
     *            become 'MyScreen1', then 'MyScreen2', and so on.)
     * @return The path(s) for the newly created screen shots on the remote system.
     * @throws Exception
     */
    public String[] _createScreenCapture(String pathName, String placeHolder) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("createScreenCapture");
        req._addString(pathName, "path");
        req._addString(placeHolder, "placeHolder");
        IncomingReply reply = this.mServer._processRequest(req);
        int n = reply._getCount();
        String[] ret = new String[n];
        for(int i = 0; i < n; i++)
            ret[i] = reply._getString(i);
        return ret;
    }

    /**
     * Gets the current text in the remote system's clipboard
     *
     * @return The string contents of the remote system's clipboard
     * @throws Exception
     */
    public String _clipboardGetText() throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("getClipboardText");
        return this.mServer._processRequest(req)._getString("text");
    }

    /**
     * Places text into the remote system's clipboard.  Useful for pasting ICS data because it does not require typing the keys directly.
     *
     * @param text The text to place in the clipboard
     * @param pasteIt performs a ctrl-v/cmd-v after the clipboard has been updated
     * @throws Exception
     */
    public void _clipboardSetText(String text, boolean pasteIt) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("setClipboardText");
        req._addString(text, "text");
        req._addBoolean(pasteIt, "pasteIt");
        this.mServer._processRequest(req);
    }

	/**
     * Returns the remote system's screen as jpg image data.  If a filename is specified then
     * it will also create and write it to that file on the remote system.
     *
     * @param file a file on the remote system to write the screen data to.  Use null to ignore.
     * @return the jpg screen data
     * @throws Exception
     */
	public byte[] _screenDataGet(String file) throws Exception
	{
		return this._screenDataGet(null, RPCImageFormat.JPG, file);
	}

    /**
     * Returns the remote system's screen data for the specified screen rectangle and imageFormat.
     * If a filename is specified then it will also create and write it to that file on the remote system.
     * Pass null rect to retrieve entire screen.
     *
     * @param rect the screen rect to retrieve the image data from.  If null it will return the entire screen.
     * @param imageFormt the desired format of the screen data
     * @param file if not null then the screen data is also written to this file on the remote system.  Use null to ignore.
     * @return the screen data in the specified image data format
     * @throws Exception
     */
    public byte[] _screenDataGet(Rectangle rect, RPCImageFormat imageFormt, String file) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("getScreenData");
        if (rect != null)
        {
        	req._addInt32(rect.x, "x");
        	req._addInt32(rect.y, "y");
        	req._addInt32(rect.height, "height");
        	req._addInt32(rect.width,  "width");
        }

        if (file != null && !"".equals(file))
        	req._addString(file, "outputFile");

    	req._addString(imageFormt.value(), "imageFormat");

        IncomingReply reply = this.mServer._processRequest(req);
        return reply._getBuffer("imageData");
    }

    /**
     * Presses a given key.
     *
     * @param keyCode see java.awt.event.KeyEvent
     * @throws Exception
     */
    public void _keyPress(int... keyCode) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("keyPress");
        for (int k : keyCode)
        	req._addInt32(k);
        
        this.mServer._processRequest(req);
    }

    /**
     * Releases a given key.
     *
     * @param keyCode see java.awt.event.KeyEvent
     * @throws Exception
     */
    public void _keyRelease(int... keyCode) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("keyRelease");
        for (int k : keyCode)
        	req._addInt32(k);
        this.mServer._processRequest(req);
    }

    /**
     * Presses and releases a sequence of keys. Upper and lower case are supported, but no other shift or
     * function keys.
     *
     * IMPORTANT NOTE: If typing an uppercase letter(s), this method will use the SHIFT key.
     * If you are typing these keys to do something such as execute play, the shift
     * key could modify the behavior of the key.  In such situations, use the lower-case
     * letter if possible.  If you need to type the capitalized letter, use the keyPress and
     * keyRelease methods to enable Caps Lock, then use those same methods to type the letter(s),
     * and then turn Caps Lock off afterwards.
     *
     * @param chars a string containing the characters to be typed.
     * @throws Exception
     */
    public void _keyType(String chars) throws Exception
    {
    	if( chars == null || chars.isEmpty() )
    		return; //nothing to do
    	
        OutgoingRequest req = this.mServer._createRequest("typeKeys");
        req._addString(chars, "chars");
        this.mServer._processRequest(req);
    }

	/**
     * Presses and releases a given key.
     *
     * @param keyCode see java.awt.event.KeyEvent
     * @throws Exception
     */
	public void _keyType(int... keyCode) throws Exception
	{
		this._keyTypePause(0.0, keyCode);
	}

	/**
     * Presses and releases a given key.
     *
     * @param pause number of seconds to pause before issuing keyRelease
     * @param keyCode see java.awt.event.KeyEvent
     * @throws Exception
     */
	public void _keyTypePause(double pause, int... keyCode) throws Exception
	{
        OutgoingRequest req = this.mServer._createRequest("keyType");
        req._addInt32( (int) ( pause*1000 ), "pause" );
        for (int k : keyCode)
        	req._addInt32(k);
        this.mServer._processRequest(req);
	}

	/**
	 * Performs a mouse press and mouse release
     * @param buttons java.awt.event.InputEvent.BUTTON1_MASK, java.awt.event.InputEvent.BUTTON2_MASK,
     *            java.awt.event.InputEvent.BUTTON3_MASK
	 * 
	 * @throws Exception
	 */
	public void _mouseClick(int clicks, MouseButtons... buttons) throws Exception
	{
		this._mouseClick(-1, null, clicks, buttons);
	}

	/**
     * Clicks on a point a number of times with the specified mouse buttons.
     *
     * @param x
     * @param y
     * @param clicks
     * @param buttons java.awt.event.InputEvent.BUTTON1_MASK, java.awt.event.InputEvent.BUTTON2_MASK,
     *            java.awt.event.InputEvent.BUTTON3_MASK
     * @throws Exception
     */
	public void _mouseClick(Point p, int clicks, MouseButtons... buttons) throws Exception
	{
        this._mouseClick(-1, p, clicks, buttons);
    }

	/**
     * Clicks on a point a number of times with the specified mouse buttons.
     *
     * @param x
     * @param y
     * @param clicks
     * @param buttons java.awt.event.InputEvent.BUTTON1_MASK, java.awt.event.InputEvent.BUTTON2_MASK,
     *            java.awt.event.InputEvent.BUTTON3_MASK
     * @throws Exception
     */
	public void _mouseClick(int timeout, Point p, int clicks, MouseButtons... buttons) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("mouseClick");
        if (p != null)
        {
        	req._addInt32(p.x, "x");
        	req._addInt32(p.y, "y");
        }
        
        req._addInt32(clicks, "clicks");
        
        for (MouseButtons button : buttons)
        	req._addInt32(button._getValue(), "button");
   
        if( timeout >= 0 ) //else use the default timeout
        	req._setTimeoutVal(timeout);
        
        this.mServer._processRequest(req);
    }
    
	/**
	 * Performs a drag and drop operation between two points.
	 *
	 * @param dragStartPoint The start point to down click on to start the drag operation.
	 * @param dropPoint The end point to up click to perform the drop operation.
	 * @throws Exception
	 */
    public void _mouseDragAndDrop(Point dragStartPoint, Point dropPoint) throws Exception
    {
    	this._mouseDragAndDrop(dragStartPoint, dropPoint, 10, true, true);
    }

    /**
	 * Performs a customizable drag and drop operation between two points.
	 *
	 * @param dragStartPoint The start point to down click on to start the drag operation.
	 * @param dropPoint The end point to up click to perform the drop operation.
	 * @param pressMouse True to automatically perform the mouse press on dragStartPoint.
	 * @param releaseMouse True to automatically perform the mouse release on dropPoint
	 * @throws Exception
	 */
    public void _mouseDragAndDrop(Point dragStartPoint, Point dropPoint, boolean pressMouse, boolean releaseMouse) throws Exception
    {
    	this._mouseDragAndDrop(dragStartPoint, dropPoint, 10, pressMouse, releaseMouse);
    }

    /**
	 * Performs a drag and drop operation between two points.
	 *
	 * @param dragStartPoint The start point to down click on to start the drag operation.  Use -1 for current mouse position.
	 * @param dropPoint The end point to up click to perform the drop operation.
	 * @param maxPixelsPerStep The max number of pixels between drag operation's mouse moves.
	 * @param pressMouse True to automatically perform the mouse press on dragStartPoint.
	 * @param releaseMouse True to automatically perform the mouse release on dropPoint
	 * @throws Exception
	 */
    public void _mouseDragAndDrop(Point dragStartPoint, Point dropPoint, int maxPixelsPerStep, boolean pressMouse, boolean releaseMouse) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("mouseDragAndDrop");
        if (dragStartPoint != null)
        {
        	req._addInt32(dragStartPoint.x, "clickX");
        	req._addInt32(dragStartPoint.y, "clickY");
        }
        req._addInt32(dropPoint.x, "dropX");
        req._addInt32(dropPoint.y, "dropY");
        req._addInt32(maxPixelsPerStep, "pixelsPerDrag");
        req._addBoolean(pressMouse,   "pressMouse");
        req._addBoolean(releaseMouse, "releaseMouse");
        this.mServer._processRequest(req);
    }

    /**
     * Returns the remote system's screen mouse location.
     * 
     * @return the Point
     * @throws Exception
     */
	public Point _mouseGetLocation() throws Exception
	{
        IncomingReply reply = this.mServer._createAndProcessRequest("getMouseLocation");
        return new Point(reply._getInt32("x"), reply._getInt32("y"));
	}

    /**
     * Moves the mouse pointer to given screen coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @throws Exception
     */
    public void _mouseMove(int x, int y) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("mouseMove");
        req._addInt32(x, "x");
        req._addInt32(y, "y");
        this.mServer._processRequest(req);
    }

    /**
     * Presses one or more mouse buttons.
     *
     * @param buttons java.awt.event.InputEvent.BUTTON1_MASK, java.awt.event.InputEvent.BUTTON2_MASK,
     *            java.awt.event.InputEvent.BUTTON3_MASK
     * @throws Exception
     */
    public void _mousePress(MouseButtons button) throws Exception
    {
    	this._mousePress(new MouseButtons[]{button});
    }
    
    /**
     * Presses one or more mouse buttons.
     *
     * @param buttons java.awt.event.InputEvent.BUTTON1_MASK, java.awt.event.InputEvent.BUTTON2_MASK,
     *            java.awt.event.InputEvent.BUTTON3_MASK
     * @throws Exception
     */
    public void _mousePress(MouseButtons... buttons) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("mousePress");
        for (MouseButtons button : buttons)
        	req._addInt32(button._getValue());
        this.mServer._processRequest(req);
    }

    /**
     * Releases one mouse button.
     *
     * @param buttons java.awt.event.InputEvent.BUTTON1_MASK, java.awt.event.InputEvent.BUTTON2_MASK,
     *            java.awt.event.InputEvent.BUTTON3_MASK
     * @throws Exception
     */
    public void _mouseRelease(MouseButtons button) throws Exception
    {
    	this._mouseRelease(new MouseButtons[]{button});
    }

    /**
     * Releases one or more mouse buttons.
     *
     * @param buttons java.awt.event.InputEvent.BUTTON1_MASK, java.awt.event.InputEvent.BUTTON2_MASK,
     *            java.awt.event.InputEvent.BUTTON3_MASK
     * @throws Exception
     */
    public void _mouseRelease(MouseButtons... buttons) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("mouseRelease");
        for (MouseButtons button : buttons)
        	req._addInt32(button._getValue());
        this.mServer._processRequest(req);
    }

    /**
     * Releases one or more mouse buttons.
     *
     * @param buttons java.awt.event.InputEvent.BUTTON1_MASK, java.awt.event.InputEvent.BUTTON2_MASK,
     *            java.awt.event.InputEvent.BUTTON3_MASK
     * @throws Exception
     */
    public void _mouseWheel(int amount) throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("mouseWheel");
       	req._addInt32(amount, "amount");
        this.mServer._processRequest(req);
    }

    /**
     * Waits until all events currently on the remote robot event queue have been processed.
     *
     * @throws Exception
     */
    public void _waitForIdle() throws Exception
    {
        OutgoingRequest req = this.mServer._createRequest("waitForIdle");
        this.mServer._processRequest(req);
    }

}
