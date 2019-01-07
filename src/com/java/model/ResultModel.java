package com.java.model;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ResultModel extends AbstractTableModel {
	private static final long serialVersionUID = -2955515915355875254L;
	private transient Object[][] data = null;

	public ResultModel() {

	}

	public ResultModel(List<Object> list) {
		if (list != null) {
			data = new Object[list.size()][2];
			for (int i = 0; i < list.size(); i++) {
				data[i][0] = false;
				data[i][1] = list.get(i);
			}
		}
	}

	@Override
	public int getRowCount() {
		return data == null ? 0 : data.length;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}

	@Override
	public String getColumnName(int column) {
		if (column == 1) {
			return "表名";
		} else {
			return "";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Boolean.class;
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = aValue;
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}

	public Object[][] getData() {
		return data;
	}

	public void setData(Object[][] data) {
		this.data = data;
	}

}
