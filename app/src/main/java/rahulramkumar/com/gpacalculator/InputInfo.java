package rahulramkumar.com.gpacalculator;

/**
 * Created by Rahul Ramkumar on 25-03-2018.
 */

public class InputInfo {
    String course,grade;

    public InputInfo() {

    }

    public InputInfo(String course, String grade) {
        this.course = course;
        this.grade = grade;
    }

    public String getCourse() {return course;}
    public void setCourse(String course) {this.course = course;}
    public String getGrade() {return grade;}
    public void setGrade(String grade) {this.grade = grade;}
}
