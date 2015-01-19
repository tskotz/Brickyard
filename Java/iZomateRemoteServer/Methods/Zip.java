package iZomateRemoteServer.Methods;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *  Provides methods for zipping and unzipping files and folders.
 */
class Zip
{
	/**
	 * Archive all files and folders in folderPath to a zip file.
	 *
	 * @param folderPath path to the folder to zip
	 * @param pathToZip path to the resulting zip file
	 * @throws Exception
	 */
	public void _archive(String folderPath, String pathToZip) throws Exception
	{
		ZipOutputStream zip = null;

		try
		{
			//Verify 'folderPath' is a directory
			if (!new File(folderPath).isDirectory())
				throw new Exception("Not a directory: " + folderPath);

			//Change separators based on opearting system
			pathToZip = pathToZip.replace('/', File.separatorChar);
			folderPath = folderPath.replace('/', File.separatorChar);

			//Create zip file's parent directory, if needed
			this.createFullPath(pathToZip.substring(0, pathToZip.lastIndexOf(File.separator)));

			//Perform the Zip operation
			zip = new ZipOutputStream(new FileOutputStream(pathToZip));
			this.addFolderToZip("", folderPath, zip);
		}
		finally
		{
			if (zip != null)
				zip.close();
		}
	}

	/**
	 * Extracts the contents of a zip file.
	 *
	 * @param pathToZip path to the zip file
	 * @param targetPath path to the destination folder where we want to unzip
	 * @param mergeDirs if TRUE - existing directory's contents will be merged, if FALSE then existing dirs will be deleted before extracting
	 * @throws IOException
	 */
	public void _extract (String pathToZip, String targetPath, boolean mergeDirs) throws IOException
	{
		ZipFile zipFile = null;

		try
		{
			//Create target path's parent directory, if needed
			this.createFullPath(targetPath);

			//Open zip file
			zipFile = new ZipFile(pathToZip);

			//Extract zip file entries
			Enumeration<? extends ZipEntry> files = zipFile.entries();
			while (files.hasMoreElements())
				this.extractFromZip(targetPath, zipFile, (ZipEntry) files.nextElement(), mergeDirs);
		}
		finally
		{
			if (zipFile != null)
				zipFile.close();
		}
	}

    //-----------------------------------
	//     Private Methods
	//-----------------------------------

	/**
	 * Adds a file to a zip file.
	 *
	 * @param path the path to the parent directory of the source file to add
	 * @param srcFile the source file to add to zip file
	 * @param zip the zip file output stream to archive into
	 * @throws Exception
	 */
	private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception
	{
		File folder = new File(srcFile);

		if (folder.isDirectory())
		{
			//Add directory to zip archive
			if (this.cutPath(path).isEmpty()) // If the directory in the root of the zip archive
				zip.putNextEntry(new ZipEntry(folder.getName() + "/"));
			else
				zip.putNextEntry(new ZipEntry(this.cutPath(path) + File.separator + folder.getName() + "/"));

			this.addFolderToZip(path, srcFile, zip);
		}
		else
		{
			int len;
			byte[] buf = new byte[1024];
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(this.cutPath(path) + File.separator + folder.getName()));

			while ((len = in.read(buf)) > 0)
				zip.write(buf, 0, len);
		
			in.close();
		}
	}

	/**
	 * Adds a folder and its contents to a zip file.
	 *
	 * @param path the path to the parent directory of the source file to add
	 * @param srcFile the source file to add to zip file
	 * @param zip the zip file output stream to archive into
	 * @throws Exception
	 */
	private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception
	{
		File folder = new File(srcFolder);

		if (path.isEmpty())
			path = folder.getName();
		else
			path += File.separator + folder.getName();

		for (String fileName : folder.list())
			this.addFileToZip(path, srcFolder + File.separator + fileName, zip);
	}

	/**
	 * Extracts an entry from the zip file and copies it to the specified extract path.
	 *
	 * @param extractPath the path to copy the extracted data to
	 * @param zipFile the current zip file
	 * @param zipEntry the zip entry to extract
	 * @param mergeDirs flag to determine whether we are merging or cleaning directories
	 * @throws IOException
	 */
	private void extractFromZip(String extractPath, ZipFile zipFile, ZipEntry zipEntry, boolean mergeDirs) throws IOException
	{
		//Change separators based on opearting system
		String entryName = zipEntry.getName().replace('/', File.separatorChar);

		//Create directory if zEntry is directory and return to the next iteration. Also used for creating empty directories.
		if (zipEntry.isDirectory())
		{
			//If needed, check if zipEntry is a folder in the root of zip file. If it is then delete folder in the destination (extractPath) folder (if exists)
			File dir = new File(extractPath + File.separator + zipEntry.toString());
			if (!mergeDirs && this.isFolderInRoot(zipEntry.toString()) && dir.exists())
				this.delete(dir);
			dir.mkdirs();
		}
		else
		{
			int nLength = 0;
			byte[] buf = new byte[1024];
			FileOutputStream fos = new FileOutputStream(extractPath + File.separator + entryName);
			InputStream is = zipFile.getInputStream(zipEntry);

			try
			{
				//Extract file and copy data
				while ((nLength = is.read(buf)) > 0)
					fos.write(buf, 0, nLength);
			}
			finally
			{
				is.close();
				fos.close();
			}
		}
	}

    //-----------------------------------
	//     Private Utility Methods
	//-----------------------------------

	/**
	 * Create full path to targetPath if it is needed
	 *
	 * @param targetPath path to target folder
	 */
	private void createFullPath(String targetPath)
	{
		File targetPathFolder = new File(targetPath);
		if (!(targetPathFolder.exists()))
			targetPathFolder.mkdirs();
	}

	/**
	 * Cut the root directory from the path
	 *
	 * @param path the path to cut
	 */
	private String cutPath(String path)
	{
		if (!path.isEmpty() && path.contains(File.separator))
			return path.substring(path.indexOf(File.separator) + 1, path.length());

		return "";
	}

	/**
	 * Delete folder and its contents
	 *
	 * @param file
	 */
	private void delete(File file)
	{
	    if (file.isDirectory())
	    {
	    	for (File f : file.listFiles())
	    		this.delete(f);

	    	file.delete();
	    }
	    else
	      file.delete();
	}

	/**
	 * Returns boolean - TRUE if the folder is in the root of zip file and FALSE if it is not
	 *
	 * @param folderName the full path of the folder to check
	 * @return boolean
	 */
	private boolean isFolderInRoot(String folderName)
	{
		return (folderName.split("/").length == 1);
	}

}