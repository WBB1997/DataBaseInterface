package Adapter;

import Charactor.Student;
import Util.XmlUtil;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.util.*;

import static java.sql.JDBCType.valueOf;

public class OracleAdapterImpl implements Adapter {
    private Connection connection = null;
    private ArrayList<String> TableNameList = new ArrayList<>(); // 表的所有名字
    private Map<String, ArrayList<String>> PrimaryKeyNameList = new HashMap<>();
    private String DataBaseName;
    private String PassWord;
    private String UserName;
    private String BaseUrl;

    public OracleAdapterImpl() {
        DataBaseName = XmlUtil.getAttribute("DataBaseName");
        PassWord = XmlUtil.getAttribute("PassWord");
        UserName = XmlUtil.getAttribute("UserName");
        BaseUrl = XmlUtil.getAttribute("BaseUrl");
        getTableNameList(new SQLStateListenerAdapter(), TableNameList);
        for (String tableName : TableNameList) {
            PrimaryKeyNameList.put(tableName, new ArrayList<>());
            getPrimaryKeyNames(new SQLStateListenerAdapter(), tableName, PrimaryKeyNameList.get(tableName));
        }
    }

    public String getDataBaseName() {
        return DataBaseName;
    }

    public String getPassWord() {
        return PassWord;
    }

    public String getUserName() {
        return UserName;
    }

    public String getBaseUrl() {
        return BaseUrl;
    }

    public ArrayList<String> getTableNameList() {
        return TableNameList;
    }

    public Map<String, ArrayList<String>> getPrimaryKeyNameList() {
        return PrimaryKeyNameList;
    }

    // 获得当前用户所拥有的表
    public void getTableNameList(SQLStateListener stateListener, ArrayList<String> res) {
        String sql = "select table_name from all_tables where owner='" + UserName.toUpperCase() + "'";
        invoke(stateListener, res, sql);
    }

    // 获取指定表的列名
    public void getColumnNames(SQLStateListener stateListener, String TableName, ArrayList<String> res) {
        Statement st = null;
        ResultSet rs = null;
        String sql = "select * from " + TableName;
        try {
            stateListener.SQLStart();
            connection = DBConnection.getConnection(getBaseUrl(), getDataBaseName(), getUserName(), getPassWord());
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++)
                res.add(rsmd.getColumnName(i));
            stateListener.SQLEnd();
        } catch (SQLException s) {
            stateListener.SQLError(s.getSQLState(), s.getErrorCode());
        } finally {
            closeResource(connection, st, rs);
        }
    }

    // 获取指定表的主键名
    public void getPrimaryKeyNames(SQLStateListener stateListener, String TableName, ArrayList<String> res) {
        String sql = "select column_name from user_cons_columns where table_name = '" + TableName.toUpperCase() +
                "' and constraint_name in (select constraint_name from user_constraints where table_name = '" +
                TableName.toUpperCase() + "' and constraint_type='P')";
        invoke(stateListener, res, sql);
    }

    private void invoke(SQLStateListener stateListener, ArrayList<String> res, String sql) {
        Statement st = null;
        ResultSet rs = null;
        try {
            stateListener.SQLStart();
            connection = DBConnection.getConnection(getBaseUrl(), getDataBaseName(), getUserName(), getPassWord());
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next())
                res.add(rs.getString(1));
            stateListener.SQLEnd();
        } catch (SQLException s) {
            stateListener.SQLError(s.getSQLState(), s.getErrorCode());
        } finally {
            closeResource(connection, st, rs);
        }
    }

    public void add(SQLStateListener stateListener, String TableName, Object object) {
        PreparedStatement ps = null;
        try {
            stateListener.SQLStart();
            StringBuilder sql_part_1 = new StringBuilder();
            StringBuilder sql_part_2 = new StringBuilder();
            Map<String, Map<String, Object>> fieldsInfo = getFieldsInfo(object);
            int i = 0;
            for (Map.Entry<String, Map<String, Object>> entry : fieldsInfo.entrySet()) {
                sql_part_1.append(entry.getKey());
                sql_part_2.append('?');
                if (i != fieldsInfo.size() - 1) {
                    sql_part_1.append(',');
                    sql_part_2.append(',');
                }
                i++;
            }
            String sql = "insert into " + TableName.toUpperCase() + "(" + sql_part_1 + ")" + " values (" + sql_part_2 + ")";
            connection = DBConnection.getConnection(getBaseUrl(), getDataBaseName(), getUserName(), getPassWord());
            ps = connection.prepareStatement(sql);
            setAttribute(ps, fieldsInfo);
            ps.executeUpdate();
            stateListener.SQLEnd();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (SQLException s) {
            stateListener.SQLError(s.getSQLState(), s.getErrorCode());
        } finally {
            closeResource(connection, ps, null);
        }
    }

    public void delete(SQLStateListener stateListener, String TableName, Object object) {
        PreparedStatement ps = null;
        try {
            stateListener.SQLStart();
            StringBuilder sql_part = new StringBuilder();
            Map<String, Map<String, Object>> fieldsInfo = getFieldsInfo(object);
            ArrayList<String> PrimaryKeyList = PrimaryKeyNameList.get(TableName);
            for (int i = 0; i < PrimaryKeyList.size(); i++) {
                sql_part.append(PrimaryKeyList.get(i)).append(" = ?");
                if (i != PrimaryKeyList.size() - 1)
                    sql_part.append(" AND ");
            }
            connection = DBConnection.getConnection(getBaseUrl(), getDataBaseName(), getUserName(), getPassWord());
            String sql = "delete from " + TableName.toUpperCase() + " where " + sql_part;
            ps = connection.prepareStatement(sql);
            setAttribute(ps, fieldsInfo, PrimaryKeyList);
            ps.executeUpdate();
            stateListener.SQLEnd();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (SQLException s) {
            stateListener.SQLError(s.getSQLState(), s.getErrorCode());
        } finally {
            closeResource(connection, ps, null);
        }
    }

    public void update(SQLStateListener stateListener, String TableName, Object oldObject, Object newObject) {
        PreparedStatement ps = null;
        try {
            stateListener.SQLStart();
            StringBuilder sql_part_1 = new StringBuilder();
            StringBuilder sql_part_2 = new StringBuilder();
            Map<String, Map<String, Object>> fieldsInfo = getFieldsInfo(oldObject);
            ArrayList<String> PrimaryKeyList = PrimaryKeyNameList.get(TableName);
            int i = 0;
            for (Map.Entry<String, Map<String, Object>> entry : fieldsInfo.entrySet()) {
                sql_part_1.append(entry.getKey()).append("= ?");
                if (i != fieldsInfo.size() - 1) {
                    sql_part_1.append(',');
                }
                i++;
            }
            for (i = 0; i < PrimaryKeyList.size(); i++) {
                sql_part_2.append(PrimaryKeyList.get(i)).append(" = ");
                if ()
                    if (i != PrimaryKeyList.size() - 1)
                        sql_part_2.append(" AND ");
            }
            String sql = "update " + TableName + " SET " + sql_part_1 + " WHERE ";
            connection = DBConnection.getConnection(getBaseUrl(), getDataBaseName(), getUserName(), getPassWord());
            ps = connection.prepareStatement(sql);
            setAttribute(ps, fieldsInfo);
            ps.executeUpdate();
            stateListener.SQLEnd();
        } catch (SQLException s) {
            stateListener.SQLError(s.getSQLState(), s.getErrorCode());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            closeResource(connection, ps, null);
        }
    }

    public List<Student> get(String args, String keyword) {
        setRowCount("SELECT COUNT(*) FROM " + TableName + " WHERE " + args + "='" + keyword + "'");
        String sql = getPageSql("SELECT * FROM " + TableName + " WHERE " + args + "='" + keyword + "'", PageControl.getPage());
        return getList(sql);
    }

    public List<Student> get() {
        setRowCount("SELECT COUNT(*) FROM " + TableName);
        String sql = getPageSql("SELECT * FROM " + TableName, PageControl.getPage());
        return getList(sql);
    }

    private String getPageSql(String sql, int Page) {
        int pageSize = PageControl.getPageSize();
        int start = Page * pageSize + 1;
        int end = start + pageSize - 1;
        List<String> columnNames = getColumnNames();
        StringBuilder head = new StringBuilder();
        for (String str : columnNames)
            head.append(str).append(",");
        head.deleteCharAt(head.length() - 1);
        return "SELECT " + head + " FROM (SELECT tmp.*, ROWNUM rn FROM (" +
                sql +
//                " ORDER BY SNO) tmp WHERE ROWNUM <= " +
                ") tmp WHERE ROWNUM <= " +
                Integer.toString(end) +
                ") WHERE rn >= " +
                Integer.toString(start);
//                + "ORDER BY SNO";
    }

    private void setRowCount(String sql) {
        Statement st;
        ResultSet rs;
        int count = 0;
        Connection connection;
        try {
            connection = DBConnection.getConnection();
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next())
                count = rs.getInt(1);
        } catch (SQLException s) {
            s.printStackTrace();
            JOptionPane.showMessageDialog(null, s.getMessage(), "错误", JOptionPane.WARNING_MESSAGE);
        }
        PageControl.setRowCount(count);
    }

    private void setAttribute(PreparedStatement ps, Map<String, Map<String, Object>> fieldsInfo, ArrayList<String> PrimaryKeyList) throws SQLException {
        for (int i = 0; i < PrimaryKeyList.size(); i++) {
            Map<String, Object> objectMap = fieldsInfo.get(PrimaryKeyList.get(i));
            subSetAttribute(objectMap, ps, i);
        }
    }

    private void setAttribute(PreparedStatement ps, Map<String, Map<String, Object>> fieldsInfo) throws SQLException {
        int i = 0;
        for (Map<String, Object> objectMap : fieldsInfo.values()) {
            subSetAttribute(objectMap, ps, i);
            i++;
        }
    }

    private void subSetAttribute(Map<String, Object> objectMap, PreparedStatement ps, int index) throws SQLException {
        String type = (String) objectMap.get("type");
        Object value = objectMap.get("value");
        switch (valueOf(type)) {
            // int
            case INTEGER:
                ps.setInt(index, (int) value);
                break;
            // long
            case BIGINT:
                ps.setLong(index, (long) value);
                break;
            // short
            case SMALLINT:
                ps.setShort(index, (Short) value);
                break;
            // float
            case FLOAT:
                ps.setFloat(index, (float) value);
                break;
            // double
            case DOUBLE:
                ps.setDouble(index, (double) value);
                break;
            // BigDecimal
            case NUMERIC:
                ps.setBigDecimal(index, (BigDecimal) value);
                break;
            // String,Class,Locale,TimeZone,Currency
            case VARCHAR:
                ps.setString(index, (String) value);
                break;
            // byte
            case TINYINT:
                ps.setByte(index, (byte) value);
                break;
            // boolean
            case BIT:
                ps.setBoolean(index, (boolean) value);
                break;
            // Date
            case DATE:
                ps.setDate(index, (Date) value);
                break;
            // Time
            case TIME:
                ps.setTime(index, (Time) value);
                break;
            // Calendar
            case TIMESTAMP:
                ps.setTime(index, (Time) value, Calendar.getInstance());
                break;
            // BLOB
            case BLOB:
                ps.setBlob(index, (Blob) value);
                break;
            // byte[]
            case LONGVARBINARY:
                ps.setBytes(index, (byte[]) value);
                break;
            default:
                break;
        }
    }

    private void closeResource(Connection conn, Statement st, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Student> getResult(ResultSet rs) throws SQLException {
        List<Student> res = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
            list.add(rsmd.getColumnName(i));
        while (rs.next()) {
            Student student = new Student();
            List<String> infolist = new ArrayList<>();
            for (int i = 1; i <= list.size(); i++) {
                if (rsmd.getColumnType(i) == Types.INTEGER)
                    infolist.add(Integer.toString(rs.getInt(i)));
                else
                    infolist.add(rs.getString(i));
            }
            student.setList(infolist);
            res.add(student);
        }
        return res;
    }

    private List<Student> getList(String sql) {
        Statement st = null;
        ResultSet rs = null;
        try {
            connection = DBConnection.getConnection(BaseUrl, DataBaseName, UserName, PassWord);
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            return getResult(rs);
        } catch (SQLException s) {
            s.printStackTrace();
            JOptionPane.showMessageDialog(null, s.getMessage(), "错误", JOptionPane.WARNING_MESSAGE);
        } finally {
            closeResource(connection, st, rs);
        }
        return null;
    }

    private Map<String, Map<String, Object>> getFieldsInfo(Object o) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Field[] fields = o.getClass().getDeclaredFields();
        Map<String, Map<String, Object>> mapHashMap = new HashMap<>();
        for (Field field : fields) {
            Map<String, Object> infoMap = new HashMap<>();
            infoMap.put("type", field.getType().toString());
            infoMap.put("value", getFieldValueByName(field.getName(), o));
            mapHashMap.put("name", infoMap);
        }
        return mapHashMap;
    }

    // 获取对象的属性的值
    private Object getFieldValueByName(String fieldName, Object o) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String firstLetter = fieldName.substring(0, 1).toUpperCase();
        String getter = "get" + firstLetter + fieldName.substring(1);
        Method method = o.getClass().getMethod(getter);
        return method.invoke(o);
    }
}
