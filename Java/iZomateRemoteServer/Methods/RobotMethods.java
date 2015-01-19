package iZomateRemoteServer.Methods;

import iZomateCore.ServerCore.RPCServer.Chunks.BaseChunk;
import iZomateCore.ServerCore.RPCServer.IncomingRequest;
import iZomateCore.ServerCore.RPCServer.OutgoingReply;
import iZomateRemoteServer.ServerThread;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Wrapper class for exported Robot objects. Every public method in this class implements an RPC method. The
 * corresponding client-side class is RemoteRobot. Only one of these objects is instantiated per server, a
 * kludge to prevent a proliferation of ExportRobot objects that will never go away.
 */
public class RobotMethods
{
	private Robot robot;

    /**
     * Constructor.
     *
     * @param map the export map
     * @throws Exception
     */
    public RobotMethods() throws Exception
    {
        this.robot = new Robot();
    }

    //-----------------------------------
	//         Public Methods
	//-----------------------------------

    /**
     * Wrapper for capturing all of the remote system's screens.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void createScreenCapture(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
        String path = req._getString("path");

        if(req._getCount() > 1)
        {
            String placeHolder = req._getString("placeHolder");

            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] devices = env.getScreenDevices();
            int i;
            String tempPath = null;
            for(i = 0; i < devices.length; i++)
            {
                if(!placeHolder.equalsIgnoreCase(".jpg"))
                    tempPath = path.replace(placeHolder, Integer.toString(i + 1));
                else
                    tempPath = path.replace(placeHolder, Integer.toString(i + 1) + ".jpg");
                this.captureScreen(devices[i], tempPath);
                reply._addString(tempPath);
            }
        }
        else
        {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice dev = env.getDefaultScreenDevice();
            this.captureScreen(dev, path);
            reply._addString(path);
        }
    }

    /**
     * Wrapper for getting the current text in the clipboard.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void getClipboardText(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	Transferable data = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(Toolkit.getDefaultToolkit().getSystemClipboard());
    	String text = (String)data.getTransferData(DataFlavor.stringFlavor);
    	reply._addString(text, "text");
    }

    /**
     * Returns the current mouse location
     * 
     * @param req
     * @param reply
     * @param server
     * @throws Exception
     */
    public void getMouseLocation(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	Point p = MouseInfo.getPointerInfo().getLocation();
    	reply._addInt32(p.x, "x");
    	reply._addInt32(p.y, "y");
    }

    /**
     * Wrapper for capturing the number of screens on the remote system.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void getNumberOfMonitors(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
        reply._addInt32(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length);
    }

    /**
     * Wrapper for taking a partial screenshot or a screenshot of the entire screen.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
	public void getScreenData(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	Rectangle rect;
        String imageFormat = req._getString("imageFormat");

    	//Check for partial screen shot
    	if (req._exists("x"))
    		rect = new Rectangle(req._getInt32("x"), req._getInt32("y"), req._getInt32("width"), req._getInt32("height"));
    	else //use entire screen boundaries
    		rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();

        BufferedImage screencapture = new Robot().createScreenCapture(rect);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ImageIO.write(screencapture, imageFormat, outStream);
        outStream.flush();

        //Check to see if we need to write screencapture to a file
        if (req._exists("outputFile"))
        {
        	String outputFile = req._getString("outputFile");
         	File file = new File(outputFile + (outputFile.endsWith("." + imageFormat)?"":"." + imageFormat));
            file.mkdirs();
        	ImageIO.write(screencapture, imageFormat, file);
        }

    	reply._addBuffer(outStream.toByteArray(), "imageData");
    }

    /**
     * Detects and image on the desktop and clicks it's center point if found.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void clickImage(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	int button = req._getInt32("button");
    	int clicks = req._getInt32("clicks");
    	int fuzzfactor = req._exists( "fuzzFactor" ) ? req._getInt32("fuzzFactor") : 40;
    	byte[] imageData = req._getBuffer("data");
    	boolean match = false;
		boolean stopSearching = false;
    	Point clickPoint = null;
    	
    	Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    	BufferedImage screencapture = new Robot().createScreenCapture(rect);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ImageIO.write(screencapture, "bmp", outStream);
        outStream.flush();
        byte[] screenData = outStream.toByteArray();
        
		if (imageData[0] == 'B' && imageData[1] == 'M')
		{
			int pixelSize= 3; //RGB
			//int scrFileSize = BaseChunk.convertByte4ToInt(screenData, 2, true);
			int scrDataStart = BaseChunk.convertByte4ToInt(screenData, 10, true);
			int scrwidth = BaseChunk.convertByte4ToInt(screenData, 18, true);
			int scrheight = BaseChunk.convertByte4ToInt(screenData, 22, true);
			int scrLineSize = pixelSize*scrwidth;
			int scrDataOffset= 0;
			
			//int imgFileSize = BaseChunk.convertByte4ToInt(imageData, 2, true);
			int imgDataStart = BaseChunk.convertByte4ToInt(imageData, 10, true);
			int imgwidth = BaseChunk.convertByte4ToInt(imageData, 18, true);
			int imgheight = BaseChunk.convertByte4ToInt(imageData, 22, true);		
			//int imgSize = BaseChunk.convertByte4ToInt(imageData, 34, true);		
			int imgLineSize = pixelSize*imgwidth;
			
			int imgByteAlgnmntDelta = imgLineSize % 4 > 0 ? 4 - imgLineSize % 4 : 0;
			int scrByteAlgnmntDelta = scrLineSize % 4 > 0 ? 4 - scrLineSize % 4 : 0;

			int imgPixel1Byte= imgDataStart;
			int imgPixel2Byte= imgDataStart;

			// Search for darkest and lightest pixels.  
			int imgByte = imgDataStart;
			for (int h = 0; h < imgheight; ++h)
			{					
				for (int i = 0; i < imgLineSize; i+=pixelSize)
				{
					int sum= (0xFF&imageData[imgByte+i]) + (0xFF&imageData[imgByte+i+1]) +  (0xFF&imageData[imgByte+i+2]);
					if( sum <= (0xFF&imageData[imgPixel1Byte]) + (0xFF&imageData[imgPixel1Byte+1]) +  (0xFF&imageData[imgPixel1Byte+2]) ) {
						imgPixel1Byte= imgByte+i;
					}
					else if( sum > (0xFF&imageData[imgPixel2Byte]) + (0xFF&imageData[imgPixel2Byte+1]) +  (0xFF&imageData[imgPixel2Byte+2]) ) {
						imgPixel2Byte= imgByte+i;
					}
				}
				
				//move to the start of the next line
				imgByte+= imgLineSize + imgByteAlgnmntDelta;
			}
			
			// Make sure pixel1 is the first pixel in the buffer else swap them
			if( imgPixel1Byte > imgPixel2Byte ) {
				int tmp= imgPixel1Byte;
				imgPixel1Byte= imgPixel2Byte;
				imgPixel2Byte= tmp;
			}
			
			//								number of rows into image file				 		 *		bytes per screen row		 +				number of bytes left in image row
			int scrPixel1offset= ((imgPixel1Byte-imgDataStart) / (imgLineSize+imgByteAlgnmntDelta)) * (scrLineSize+scrByteAlgnmntDelta) + ((imgPixel1Byte-imgDataStart) % (imgLineSize+imgByteAlgnmntDelta));
			int scrPixel2offset= ((imgPixel2Byte-imgDataStart) / (imgLineSize+imgByteAlgnmntDelta)) * (scrLineSize+scrByteAlgnmntDelta) + ((imgPixel2Byte-imgDataStart) % (imgLineSize+imgByteAlgnmntDelta));
											
			int scrByte = scrDataStart;
			for (int scrRow = 0; scrRow < scrheight; ++scrRow) 
			{
				if( stopSearching || match )
					break;
				
				int endOfLine= scrByte + scrLineSize < screenData.length ? scrByte + scrLineSize : screenData.length - 1;
				// Search the row
				for (int i = scrByte; i < endOfLine; i+=pixelSize) 
				{	
					match = false;
					if( i+scrPixel2offset >= screenData.length ) {
						i= endOfLine;
						break;
					}

					if(	Math.abs((0xFF&screenData[i+scrPixel1offset])   - (0xFF&imageData[imgPixel1Byte]))   <= fuzzfactor &&
						Math.abs((0xFF&screenData[i+scrPixel1offset+1]) - (0xFF&imageData[imgPixel1Byte+1])) <= fuzzfactor &&
						Math.abs((0xFF&screenData[i+scrPixel1offset+2]) - (0xFF&imageData[imgPixel1Byte+2])) <= fuzzfactor ) 
					{								
						//int x = ((i - scrDataStart + scrPixel1offset) % (scrLineSize+scrByteAlgnmntDelta)) / pixelSize;
						//int y = scrheight - (i - scrDataStart + scrPixel1offset) / (scrLineSize+scrByteAlgnmntDelta);
						//this.robot.mouseMove(x, y);
														
						if (Math.abs((0xFF&screenData[i+scrPixel2offset])   - (0xFF&imageData[imgPixel2Byte]))   <= fuzzfactor &&
							Math.abs((0xFF&screenData[i+scrPixel2offset+1]) - (0xFF&imageData[imgPixel2Byte+1])) <= fuzzfactor &&
							Math.abs((0xFF&screenData[i+scrPixel2offset+2]) - (0xFF&imageData[imgPixel2Byte+2])) <= fuzzfactor ) 
						{
							//int x = ((i - scrDataStart + scrPixel2offset) % (scrLineSize+scrByteAlgnmntDelta)) / pixelSize;
							//int y = scrheight - (i - scrDataStart + scrPixel2offset) / (scrLineSize+scrByteAlgnmntDelta);
							//this.robot.mouseMove(x, y);
							
							match = true;
							int imgDataOffset = imgDataStart;
							scrDataOffset= 0;
							
							for (int h = 0; h < imgheight && match && !stopSearching; ++h)
							{					
								for (int j = 0; j < imgLineSize && match && !stopSearching; ++j)
								{
									if (i+scrDataOffset+j >= screenData.length ) {
										stopSearching= true;
										match= false;
									}
									
									if (Math.abs((0xFF&screenData[i+scrDataOffset+j]) - (0xFF&imageData[imgDataOffset+j])) > fuzzfactor)
										match = false;
								}
								
								if (match)
								{	//move to next line and verify
									imgDataOffset += imgLineSize + imgByteAlgnmntDelta;
									scrDataOffset += scrLineSize + scrByteAlgnmntDelta;
								}
							}
						}
							
						if (match)
						{	
							int px = ((i - scrDataStart) % (scrwidth*pixelSize)) / pixelSize;
							int py = scrheight - (i + scrDataOffset - scrDataStart) / (scrwidth*pixelSize);
							
							int xoffset = (req._exists("xOffset")) ? req._getInt32("xOffset") : imgwidth/2;
							int yoffset = (req._exists("yOffset")) ? req._getInt32("yOffset") : imgheight/2;

							clickPoint = new Point(px + xoffset, py + yoffset);
							
							reply._addInt32(clickPoint.x, "x");
							reply._addInt32(clickPoint.y, "y");

							this.robot.mouseMove(clickPoint.x, clickPoint.y);
							Thread.sleep(10);
							for (int k = 0; k < clicks; ++k)
							{
								if( k > 0 )
									Thread.sleep(100);
								
								this.robot.mousePress(button);
								this.robot.mouseRelease(button);
							}
							
							break; //done!
						}
						
						if (stopSearching)
							break;
					}
				}
				
				//move to the start of the next line
				scrByte= endOfLine + scrByteAlgnmntDelta;
			}			
		}
		else
			reply._addString("Unsupported image type!", "Error");
		
		if (!match)
			reply._addString("Could not find image!", "Error");
    }

    public void clickImageOrig(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	int button = req._getInt32("button");
    	int clicks = req._getInt32("clicks");
    	byte[] imageData = req._getBuffer("data");
    	boolean match = false;
		boolean stopSearching = false;
    	Point clickPoint = null;
    	
    	Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    	BufferedImage screencapture = new Robot().createScreenCapture(rect);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ImageIO.write(screencapture, "bmp", outStream);
        outStream.flush();
        byte[] screenData = outStream.toByteArray();
        
		if (imageData[0] == 'B' && imageData[1] == 'M')
		{
			int pixelSize= 3;
			int scrFileSize = BaseChunk.convertByte4ToInt(screenData, 2, true);
			int scrDataStart = BaseChunk.convertByte4ToInt(screenData, 10, true);
			int scrwidth = BaseChunk.convertByte4ToInt(screenData, 18, true);
			int scrheight = BaseChunk.convertByte4ToInt(screenData, 22, true);
			int srcDataOffset = 0;
			
			//int imgFileSize = BaseChunk.convertByte4ToInt(imageData, 2, true);
			int imgDataStart = BaseChunk.convertByte4ToInt(imageData, 10, true);
			int imgwidth = BaseChunk.convertByte4ToInt(imageData, 18, true);
			int imgheight = BaseChunk.convertByte4ToInt(imageData, 22, true);		
			//int imgSize = BaseChunk.convertByte4ToInt(imageData, 34, true);		
			int imgLineSize = pixelSize*imgwidth;
			int scrLineSize = pixelSize*scrwidth;
			int imgByteAlgnmntDelta = imgLineSize % 4 > 0 ? 4 - imgLineSize % 4 : 0;
			int scrByteAlgnmntDelta = scrLineSize % 4 > 0 ? 4 - scrLineSize % 4 : 0;
						
			for (int srcByte = scrDataStart; srcByte < scrFileSize; srcByte+=3)
			{
				match = true;
				int imgDataOffset = imgDataStart;
				for (int h = 0; h < imgheight && match; ++h)
				{					
					for (int i = 0; i < imgLineSize; ++i)
					{
						if (srcByte+srcDataOffset+i >= screenData.length ) {
							stopSearching= true;
							match= false;
							break;
						}
						
						if (Math.abs(screenData[srcByte+srcDataOffset+i] - imageData[imgDataOffset+i]) > 30)
						{
							match = false;
							break;
						}
					}
					
					if (match)
					{	//move to next line and verify
						imgDataOffset += imgLineSize + imgByteAlgnmntDelta;
						srcDataOffset += scrLineSize + scrByteAlgnmntDelta;
					}
				}
				
				if (match)
				{	
					int x = ((srcByte - scrDataStart) % (scrLineSize + scrByteAlgnmntDelta)) / pixelSize;
					int y = scrheight - (srcByte + srcDataOffset - scrDataStart) / (scrLineSize + scrByteAlgnmntDelta);
					
					int xoffset = (req._exists("xOffset")) ? req._getInt32("xOffset") : imgwidth/2;
					int yoffset = (req._exists("yOffset")) ? req._getInt32("yOffset") : imgheight/2;

					clickPoint = new Point(x + xoffset, y + yoffset);
					
					this.robot.mouseMove(clickPoint.x, clickPoint.y);
					Thread.sleep(10);
					for (int i = 0; i < clicks; ++i)
					{
						if( i > 0 )
							Thread.sleep(100);
						
						this.robot.mousePress(button);
						this.robot.mouseRelease(button);
					}
					break; //done!
				}
				
				if (stopSearching)
					break;
				
				if (srcDataOffset != 0)
					srcDataOffset = 0;
			}
		}
		else
			reply._addString("Unsupported image type!", "Error");
		
		if (!match)
			reply._addString("Could not find image!", "Error");
		
		if (clickPoint != null)
		{
			reply._addInt32(clickPoint.x, "x");
			reply._addInt32(clickPoint.y, "y");
		}
    }

    /**
     * Wrapper for pressing a given key.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void keyPress(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	for (int i = 1; i < req._getCount(); ++i)
    		this.robot.keyPress(req._getInt32(i));
    }

    /**
     * Wrapper for releasing a given key.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void keyRelease(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	for (int i = 1; i < req._getCount(); ++i)
    		this.robot.keyRelease(req._getInt32(i));
    }

    /**
     * Wrapper for pressing and releasing a given key.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void keyType(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	for (int i = 2; i < req._getCount(); ++i)
    		this.robot.keyPress(req._getInt32(i));

		Thread.sleep( req._getInt32(1) ); //Delay time in ms
		
		// Release in reverse order
    	for (int i = req._getCount()-1; i > 1; --i)
    		this.robot.keyRelease(req._getInt32(i));
    }

    /**
     * Wrapper for simulating a mouse drag and drop operation
     *
     * @param req
     * @param reply
     * @param server
     * @throws Exception
     */
	public void mouseDragAndDrop(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	float clickX = req._exists("clickX") ? req._getInt32("clickX") : MouseInfo.getPointerInfo().getLocation().x;
    	float clickY = req._exists("clickY") ? req._getInt32("clickY") : MouseInfo.getPointerInfo().getLocation().y;
    	int dropX = req._getInt32("dropX");
    	int dropY = req._getInt32("dropY");
    	int pixPerLoopMax = req._getInt32("pixelsPerDrag");
    	boolean pressMouse = req._getBoolean("pressMouse");
    	boolean releaseMouse = req._getBoolean("releaseMouse");

    	int deltaX = dropX-(int)clickX;
    	int deltaY = dropY-(int)clickY;

    	float pixPerLoopX = Math.abs(deltaX)>=Math.abs(deltaY) ? pixPerLoopMax*((deltaX<0)?-1:1) : pixPerLoopMax*((float)Math.abs(deltaY)/deltaX);
    	float pixPerLoopY = Math.abs(deltaY)>=Math.abs(deltaX) ? pixPerLoopMax*((deltaY<0)?-1:1) : pixPerLoopMax*((float)deltaY/Math.abs(deltaX));

		this.robot.mouseMove((int)clickX, (int)clickY);

		if (pressMouse)
    		this.robot.mousePress(InputEvent.BUTTON1_MASK);

    	while (clickX != dropX || clickY != dropY)
    	{
    		Thread.sleep( 10 );

	    	if (pixPerLoopX >= 0)
	    		clickX = (clickX+pixPerLoopX>=dropX)?dropX:(clickX+pixPerLoopX);
	    	else
	    		clickX = (clickX+pixPerLoopX<=dropX)?dropX:(clickX+pixPerLoopX);

	    	if (pixPerLoopY >= 0)
	    		clickY = (clickY+pixPerLoopY>=dropY)?dropY:(clickY+pixPerLoopY);
	    	else
	    		clickY = (clickY+pixPerLoopY<=dropY)?dropY:(clickY+pixPerLoopY);

	        this.robot.mouseMove((int)clickX, (int)clickY);
    	}

    	if (releaseMouse)
    		this.robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    /**
     * Wrapper for moving the mouse pointer to given screen coordinates.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void mouseMove(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
        this.robot.mouseMove(req._getInt32("x"), req._getInt32("y"));
    }

    /**
     * Wrapper for pressing one or more mouse buttons.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void mousePress(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
       	for (int i = 1; i < req._getCount(); ++i)
       		this.robot.mousePress(req._getInt32(i));
    }

    /**
     * Wrapper for releasing one or more mouse buttons.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void mouseRelease(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	for (int i = 1; i < req._getCount(); ++i)
    		this.robot.mouseRelease(req._getInt32(i));
    }

    /**
     * Wrapper for clicking on a specific point.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void mouseClick(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
    	int clicks = req._getInt32("clicks");
    	
    	Point p = MouseInfo.getPointerInfo().getLocation();

    	if (req._exists("x"))
    		p.x = req._getInt32("x");
    	if (req._exists("y"))
    		p.y = req._getInt32("y");
    	
    	this.robot.mouseMove(p.x, p.y);
     	
    	for (int i = 0; i < clicks; ++i)
    	{
    	   	for (int b = 1; b < req._getCount(); ++b)
    	   		if (req._getName(b).equals("button"))
    	   			this.robot.mousePress(req._getInt32(b));
    	   	
    		Thread.sleep( 10 );
    		
    	   	for (int b = 1; b < req._getCount(); ++b)
    	   		if (req._getName(b).equals("button"))
    	   			this.robot.mouseRelease(req._getInt32(b));
    	}
    }

    /**
     * Wrapper for moving mouse wheel.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void mouseWheel(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
        this.robot.mouseWheel(req._getInt32("amount"));
    }
    
    /**
     * Wrapper for pasting text into the clipboard.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void setClipboardText(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
        StringSelection data = new StringSelection(req._getString("text"));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);

		if (req._getBoolean("pasteIt"))
		{
			int cmd_cntrl_key = (System.getProperty("os.name").contains("Mac"))?KeyEvent.VK_META:KeyEvent.VK_CONTROL;
	        this.robot.keyPress(cmd_cntrl_key);
	        this.robot.keyPress(KeyEvent.VK_V);
	        this.robot.keyRelease(cmd_cntrl_key);
	        this.robot.keyRelease(KeyEvent.VK_V);
		}
   }

    /**
     * Wrapper for pressing and releasing a sequence of keys.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void typeKeys(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
        String str = req._getString("chars");

        for(int i = 0; i < str.length(); i++)
            this.typeKey(str.charAt(i));
    }

    /**
     * Wrapper for waiting until all events currently on the remote event queue have been processed.
     *
     * @param req the request
     * @param reply the reply
     * @param server the server thread
     * @throws Exception
     */
    public void waitForIdle(IncomingRequest req, OutgoingReply reply, ServerThread server) throws Exception
    {
        this.robot.waitForIdle();
    }

    //-----------------------------------
	//         Private Methods
	//-----------------------------------

    /**
     * Helper for createScreenCapture.  This method actually writes the JPEG image to the file system.
     *
     * @param dev the graphics device
     * @param filePath the file path to write to
     * @throws Exception
     */
    private void captureScreen(GraphicsDevice dev, String filePath) throws Exception
    {
        Rectangle rect = dev.getDefaultConfiguration().getBounds();
        BufferedImage screencapture = new Robot().createScreenCapture(new Rectangle(rect));
        File file = new File(filePath);
        ImageIO.write(screencapture, "jpg", file);
    }

    /**
     * Handles typing the key
     *
     * @param key keyCode of the key to type
     * @param shift true if shift is needed
     */
    private void doKeyType(int key, boolean shift)
    {
    	if (shift)
            this.robot.keyPress(KeyEvent.VK_SHIFT);

        this.robot.keyPress(key);
        this.robot.keyRelease(key);

    	if (shift)
    		this.robot.keyRelease(KeyEvent.VK_SHIFT);
    }

    /**
     * Converts character to KeyEvent code and types the key.
     *
     * @param c the character
     * @throws Exception
     */
    private void typeKey(char c) throws Exception
    {
        switch(c)
        {
            case '1':
            case '!':
                this.doKeyType(KeyEvent.VK_1, c == '!');		break;
            case '2':
            case '@':
                this.doKeyType(KeyEvent.VK_2, c == '@');		break;
            case '3':
            case '#':
                this.doKeyType(KeyEvent.VK_3, c == '#');		break;
            case '4':
            case '$':
                this.doKeyType(KeyEvent.VK_4, c == '$');		break;
            case '5':
            case '%':
                this.doKeyType(KeyEvent.VK_5, c == '%');		break;
            case '6':
            case '^':
                this.doKeyType(KeyEvent.VK_6, c == '^');		break;
            case '7':
            case '&':
                this.doKeyType(KeyEvent.VK_7, c == '&');		break;
            case '8':
            case '*':
                this.doKeyType(KeyEvent.VK_8, c == '*');		break;
            case '9':
            case '(':
                this.doKeyType(KeyEvent.VK_9, c == '(');		break;
            case '0':
            case ')':
                this.doKeyType(KeyEvent.VK_0, c == ')');		break;
            case 'a':
            case 'A':
                this.doKeyType(KeyEvent.VK_A, c == 'A');		break;
            case 'b':
            case 'B':
                this.doKeyType(KeyEvent.VK_B, c == 'B');		break;
            case 'c':
            case 'C':
                this.doKeyType(KeyEvent.VK_C, c == 'C');		break;
            case 'd':
            case 'D':
                this.doKeyType(KeyEvent.VK_D, c == 'D');		break;
            case 'e':
            case 'E':
                this.doKeyType(KeyEvent.VK_E, c == 'E');		break;
            case 'f':
            case 'F':
                this.doKeyType(KeyEvent.VK_F, c == 'F');		break;
            case 'g':
            case 'G':
                this.doKeyType(KeyEvent.VK_G, c == 'G');		break;
            case 'h':
            case 'H':
                this.doKeyType(KeyEvent.VK_H, c == 'H');		break;
            case 'i':
            case 'I':
                this.doKeyType(KeyEvent.VK_I, c == 'I');		break;
            case 'j':
            case 'J':
                this.doKeyType(KeyEvent.VK_J, c == 'J');		break;
            case 'k':
            case 'K':
                this.doKeyType(KeyEvent.VK_K, c == 'K');		break;
            case 'l':
            case 'L':
                this.doKeyType(KeyEvent.VK_L, c == 'L');		break;
            case 'm':
            case 'M':
                this.doKeyType(KeyEvent.VK_M, c == 'M');		break;
            case 'n':
            case 'N':
                this.doKeyType(KeyEvent.VK_N, c == 'N');		break;
            case 'o':
            case 'O':
                this.doKeyType(KeyEvent.VK_O, c == 'O');		break;
            case 'p':
            case 'P':
                this.doKeyType(KeyEvent.VK_P, c == 'P');		break;
            case 'q':
            case 'Q':
                this.doKeyType(KeyEvent.VK_Q, c == 'Q');		break;
            case 'r':
            case 'R':
                this.doKeyType(KeyEvent.VK_R, c == 'R');		break;
            case 's':
            case 'S':
                this.doKeyType(KeyEvent.VK_S, c == 'S');		break;
            case 't':
            case 'T':
                this.doKeyType(KeyEvent.VK_T, c == 'T');		break;
            case 'u':
            case 'U':
                this.doKeyType(KeyEvent.VK_U, c == 'U');		break;
            case 'v':
            case 'V':
                this.doKeyType(KeyEvent.VK_V, c == 'V');		break;
            case 'w':
            case 'W':
                this.doKeyType(KeyEvent.VK_W, c == 'W');		break;
            case 'x':
            case 'X':
                this.doKeyType(KeyEvent.VK_X, c == 'X');		break;
            case 'y':
            case 'Y':
                this.doKeyType(KeyEvent.VK_Y, c == 'Y');		break;
            case 'z':
            case 'Z':
                this.doKeyType(KeyEvent.VK_Z, c == 'Z');		break;
            case '`':
            case '~':
                this.doKeyType(KeyEvent.VK_BACK_QUOTE, c == '~');	break;
            case '=':
            case '+':
                this.doKeyType(KeyEvent.VK_EQUALS, c == '+');		break;
            case '-':
            case '_':
                this.doKeyType(KeyEvent.VK_MINUS, c == '_');		break;
            case '[':
            case '{':
                this.doKeyType(KeyEvent.VK_OPEN_BRACKET, c == '{');	break;
            case ']':
            case '}':
                this.doKeyType(KeyEvent.VK_CLOSE_BRACKET, c == '}');break;
            case '|':
            case '\\':
                this.doKeyType(KeyEvent.VK_BACK_SLASH, c == '|');	break;
            case ';':
            case ':':
                this.doKeyType(KeyEvent.VK_SEMICOLON, c == ':');	break;
            case '\'':
            case '\"':
                this.doKeyType(KeyEvent.VK_QUOTE, c == '\"');		break;
            case ',':
            case '<':
                this.doKeyType(KeyEvent.VK_COMMA, c == '<');		break;
            case '.':
            case '>':
                this.doKeyType(KeyEvent.VK_PERIOD, c == '>');		break;
            case '/':
            case '?':
                this.doKeyType(KeyEvent.VK_SLASH, c == '?');		break;
            case '\n':
            case '\r':
                this.doKeyType(KeyEvent.VK_ENTER, 	false);			break;
            case ' ':
                this.doKeyType(KeyEvent.VK_SPACE, 	false);			break;
            case '\t':
                this.doKeyType(KeyEvent.VK_TAB, 	false);			break;
            case '→':
                this.doKeyType(KeyEvent.VK_RIGHT, 	false);			break;
            case '←':
                this.doKeyType(KeyEvent.VK_LEFT, 	false);			break;
            case '↑':
                this.doKeyType(KeyEvent.VK_UP, 		false);			break;
            case '↓':
                this.doKeyType(KeyEvent.VK_DOWN, 	false);			break;
            default:
                throw new Exception("can't translate character '" + c + "'");
       }
    }

}
