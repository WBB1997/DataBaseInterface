package Util;

import Charactor.Student;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class InfoModel extends AbstractTableModel {

    private List<String> columnNames;
    private List<Student> studentList;

    public InfoModel(List<Student> studentList, List<String> columnNames) {
        this.studentList = studentList;
        this.columnNames = columnNames;
    }

    public int getRowCount() {
        return studentList.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Student student = studentList.get(rowIndex);
        List<Object> list = student.getList();
        return list.get(columnIndex);
    }

    public List<String> getColumnNames() {
        return columnNames;
    }
}