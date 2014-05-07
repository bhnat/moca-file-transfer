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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.client.MocaConnection;

public class FileUploader {

	public static void transferFile(MocaConnection conn, String inputFileName, String outputPath) {

		try {
			// get pointer to the file and print ack message to user
			File file = new File(inputFileName);
			//System.out.println("File is size " + file.length());

			// Convert contents to byte array and print ack message to user
			InputStream in = new FileInputStream(file); 
			byte[] fileContents = IOUtils.toByteArray(in);
			//System.out.println("Byte array size is " + fileContents.length);

			String command = String.format(
					"decode from base64" +
					" where str = '%s'" +
					" | " +
					"write stream to file " +
					" where dataStream = @base64_decoded" +
					"   and filePath = '%s'",
					com.redprairie.util.Base64.encode(fileContents), 
					outputPath);
			conn.executeCommand(command);			
		} catch (MocaException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
		}
	}
}
