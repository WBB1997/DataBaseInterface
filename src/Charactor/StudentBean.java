package Charactor;

import java.util.ArrayList;
import java.util.List;

public class StudentBean {
    private String Sno;
    private String Sname;
    private String Ssex;
    private String Sage;
    private String Sdept;

    private List<String> List = new ArrayList<>();

    public String getSno() {
        return Sno;
    }

    public void setSno(String sno) {
        Sno = sno;
    }

    public String getSname() {
        return Sname;
    }

    public void setSname(String sname) {
        Sname = sname;
    }

    public String getSsex() {
        return Ssex;
    }

    public void setSsex(String ssex) {
        Ssex = ssex;
    }

    public String getSage() {
        return Sage;
    }

    public void setSage(String sage) {
        Sage = sage;
    }

    public String getSdept() {
        return Sdept;
    }

    public void setSdept(String sdept) {
        Sdept = sdept;
    }

    public void setList(List<String> list){
        this.List = list;
        for(int i = 0; i < List.size(); i++){
            switch (i){
                case 0: Sno = List.get(i);
                case 1: Sname = List.get(i);
                case 2: Ssex = List.get(i);
                case 3: Sage = List.get(i);
                case 4: Sdept = List.get(i);
            }
        }
    }

    public List<String> getList(){
        return List;
    }

    public int getAttrCounts(){
        return this.getClass().getDeclaredFields().length - 1;
    }
}
