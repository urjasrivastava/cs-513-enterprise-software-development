package com.iiitb.esdacademicerp.service;

import com.iiitb.esdacademicerp.dao.CourseRepository;
import com.iiitb.esdacademicerp.dao.PrerequisiteRepository;
import com.iiitb.esdacademicerp.dao.StudentCourseRepository;
import com.iiitb.esdacademicerp.dao.StudentRepository;
import com.iiitb.esdacademicerp.model.Course;
import com.iiitb.esdacademicerp.model.Student;
import com.iiitb.esdacademicerp.model.StudentCourse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// TODO : Work on this class please!
@Service
public class CourseEnrollmentService {

    @Autowired
    private StudentRepository student;


    @Autowired
    private StudentCourseRepository studentCourseRepository;


    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PrerequisiteRepository prerequisiteRepository;


    /* TODO : Function that returns the course enrollment data to the controller
    Return type: ArrayList<HashMap<String, ?>>
    Contents of the map: (For a course with pre-requisite)
    {
        "course_id" : 8,
        "name" : "Natural Language Processing"
        "code" : "AI829",
        "faculty" : "Prof. G Srinivasa",
        "description" : "blah blah blah",
        "prerequisite" : [
            {
                "name" : "Machine Learning",
                "code" : "AI101"
            },
            {
                "name" : "Mathematics for Machine Leaning",
                "code" : "AI102"
            }
         ], // If the course has no prerequisites then let this `prerequisite` array be empty
        "total_credits" : 4,
        "available_seats" : 100,
        "is_enrolled" : true // true -> Student is currently enrolled, false -> not enrolled
    }


     */
    // The student object will be sent by the controller
    public HashMap<Course,Short> getCourseEnrollmentData(Student student) {
        if(studentCourseRepository==null)
        {System.out.println("NULL");return null;}
        ArrayList<Long> studentcourselist= studentCourseRepository.getStudentCoursesByStudentId(student.getStudentId());
        ArrayList<Course> courses=courseRepository.getCourseByYear((short) 2020);
        for(int i=0;i<courses.size();i++)
        {
            ArrayList<Long> prerequisitelist= prerequisiteRepository.getPrerequisiteByCourseId(courses.get(i).getCourseId());
            int fl=1;
            for(int j=0;j<prerequisitelist.size();j++)
            {
                if(!studentcourselist.contains(prerequisitelist.get(j)))
                {
                    fl=0;
                    break;
                }
            }
            if(fl==0)
                courses.remove(i);

        }
        HashMap<Course,Short> courseHashMap=new HashMap<Course,Short>();
        for(int i=0;i< courses.size();i++)
        {
            if(studentcourselist.contains(courses.get(i).getCourseId()))
                courseHashMap.put(courses.get(i),(short)1);
            else
                courseHashMap.put(courses.get(i),(short)0);
        }
        if(courseHashMap.isEmpty())return null;
        return courseHashMap;
    }

    /* TODO: Function that enroll/de-enrolls the student from a course

        It takes a ArrayList<HashMap<String, ?> as the input
        Contents of the map
        {
            "course_id" : 8,
            "enroll" : true // true -> Student wants to enroll, false -> doesn't want to enroll (de-enroll)
            // NOTE : You'll have to verify whether student is already enrolled or not we can change this logic if needed

        }

        OPTIONAL TODO : You'll have to re-fetch the current available seat status before changing it (concurrency issues)
     */
    // The student object will be sent by the controller
    public void setCourseEnrollmentStatus(Student student,HashMap<Course,Short> map)
    {
        for (Map.Entry<Course,Short> entry :map.entrySet())
        {
            Course c= entry.getKey();
            short val= entry.getValue();
            if(val==1)
            {
                c.setAvailableSeats((short) (c.getAvailableSeats()-1));
                courseRepository.save(c);
                StudentCourse s=new StudentCourse();
                s.setCourse(c);
                s.setComments("Enrolled");
                s.setStudent(student);
                studentCourseRepository.save(s);
            }
            else if(val==2)
            {
                c.setAvailableSeats((short) (c.getAvailableSeats()+1));
                courseRepository.save(c);
                StudentCourse s= studentCourseRepository.getStudentCoursesByStudentIdAndCourseId(student.getStudentId(),c.getCourseId());
                studentCourseRepository.delete(s);
            }
        }
     return ;
    }

}
