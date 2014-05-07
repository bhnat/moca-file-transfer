/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 MiTR Apps, LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mitrapps.mocaFileTransfer;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.client.MocaConnection;

public class RemoteListingTableModel extends AbstractListingTableModel {
	
	private String currentPath;
	
	public RemoteListingTableModel(MocaConnection moca, String path) {
		this.updateDirectoryListing(moca, path);
	}
	
	public void updateDirectoryListing(MocaConnection moca, String path) {
		
		super.initializeTableModel();
		if (moca == null) {
			this.currentPath = "";
		} else {
			this.updateFromMocaInstance(moca, path);
		}
	}

	public String getCurrentPath() {
		return this.currentPath;
	}
	
	private void updateFromMocaInstance(MocaConnection moca, String path) {
		this.fileNames.add("..");
		this.fileTypes.add("D");
		this.fileSizes.add(new Long(0));
		this.fileDates.add("----");
		this.currentPath = path;
		if (moca != null) {
			String buffer = String.format("{"
					+ "    find file where filnam = '%s' "
					+ "    | "
					+ "    publish data where root_path = @pathname "
					+ "} "
					+ "| "
					+ "[[ "
					+ "    String myPath = \"\"; "
					+ "    File file = new File(root_path); "
					+ "    myPath = file.getCanonicalPath(); "
					+ "    path = myPath; "
					+ "]] "
					+ "| "
					+ "find file where filnam = '%s/*' "
					+ "| "
					+ "get file info where filnam = @pathname catch(@?)" 
					+ "| "
                    + "publish data where file = @filename and type = @type "
                    + "   and lastModified = @modified and size = @size and path = @path",
                    path, path);
			//System.out.println(buffer);
			try {
				MocaResults rs = moca.executeCommand(buffer);
				while (rs.next()) {
					if (!rs.isNull("file")) {
						this.fileNames.add(rs.getString("file"));
						this.fileTypes.add(rs.getString("type"));
						this.fileSizes.add("D".equalsIgnoreCase(rs.getString("type")) ? 0 : new Long(rs.getInt("size")));
						this.fileDates.add(rs.getString("lastModified"));
					}
					if (!rs.isNull("path")) {
						this.currentPath = rs.getString("path");
					}
				}
			} catch (MocaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
