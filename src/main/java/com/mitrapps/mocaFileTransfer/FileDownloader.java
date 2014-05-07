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

import java.io.FileOutputStream;
import java.io.IOException;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.client.MocaConnection;

public class FileDownloader {

    /**
     * @param inputPath
     * @param outputPath
     * @param conn
     */
    public static void transferFile(String inputPath, String outputPath, MocaConnection conn) {
        try {
			String command = String.format("[[" +
					"  boolean shouldCompress = false;" +
					"  File file = new File('%s');" +
					"  byte[] fileContents = com.redprairie.moca.util.IOUtils.readFile(file, shouldCompress);" +
					"]]" +
					" | " +
					"encode to base64 where bin_data = @result",
					inputPath.replace("\\", "\\\\"));
			//System.out.println(command);
			MocaResults res = conn.executeCommand(command);
			if (res.next()) {
				String encodedFileContents = res.getString(0);
				FileOutputStream out1 = new FileOutputStream(outputPath);  
				byte[] outbytes = com.redprairie.util.Base64.decode(encodedFileContents);
	 			try {  
	 			    out1.write(outbytes);  
	 			} finally {  
	 			    out1.close();  
	 			}  
			}
		} catch (MocaException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
