package Util;

import Charactor.StudentBean;

import java.util.List;

public interface InfoManager {
    //增加
    String add(StudentBean bean);

    //删除
    String delete(StudentBean bean);

    //修改
    String update(StudentBean oldone, StudentBean newone);

    //查询
    List<StudentBean> get(String args, String keyword);

    //获取数据
    List<StudentBean> get();

    //获取列名
    List<String> getColumnNames();

}
