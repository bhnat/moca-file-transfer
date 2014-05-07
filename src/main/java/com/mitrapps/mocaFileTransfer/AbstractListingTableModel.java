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

import java.util.ArrayList;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public abstract class AbstractListingTableModel implements TableModel {
	
	protected ArrayList<String> fileNames;
	protected ArrayList<String> fileTypes;
	protected ArrayList<Long> fileSizes;
	protected ArrayList<String> fileDates;
	
	public static final int FILE_TYPE_COLUMN_INDEX = 0;
	public static final int FILE_NAME_COLUMN_INDEX = 1;
	public static final int FILE_SIZE_COLUMN_INDEX = 2;
	public static final int FILE_DATE_COLUMN_INDEX = 3;
	public static final String FILE_TYPE = "F";
	public static final String DIRECTORY_TYPE = "D";
	
	protected void initializeTableModel() {
		this.fileNames = new ArrayList<String>();
		this.fileTypes = new ArrayList<String>();
		this.fileSizes = new ArrayList<Long>();
		this.fileDates = new ArrayList<String>();
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return fileNames.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public String getColumnName(int columnIndex) {
		// TODO Auto-generated method stub
		switch (columnIndex) {
			case 0: 
				return "Type";
			case 1:
				return "Name";
			case 2:
				return "Size";
			case 3:
				return "Date";
			default:
				return "?";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 2:
				return Long.class;
			default:
				return String.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		switch (columnIndex) {
			case 0: 
				return this.fileTypes.get(rowIndex);
			case 1:
				return this.fileNames.get(rowIndex);
			case 2:
				return this.fileSizes.get(rowIndex);
			case 3:
				return this.fileDates.get(rowIndex);
			default:
				return "?";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

}
