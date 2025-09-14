package com.studentdb.Student_Management.Controller;

import com.studentdb.Student_Management.Entity.Student;
import com.studentdb.Student_Management.Repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Create student (POST) - allowed for all
    @PostMapping("/post")
    public ResponseEntity<?> createStudent(@RequestBody Student student) {
        try {
            if (studentRepository.findByUsername(student.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("username number already exists");
            }

            student.setPassword(passwordEncoder.encode(student.getPassword()));

            if (student.getRoles().isEmpty()) {
                student.getRoles().add("USER");
            }

            Student created = studentRepository.save(student);

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered with username: " + created.getUsername());
        }catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occured during registration " + e.getMessage());
        }
    }

    // Get all students - allowed for USER and ADMIN
    @GetMapping
    public ResponseEntity<?>getAllStudents(Authentication authentication){
        List<Student> students= studentRepository.findAll();

        if(authentication.getAuthorities().stream().noneMatch(
                auth->auth.getAuthority().equals("ROLE_ADMIN"))){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Only ADMIN can view all students");
        }

        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("No students found");
        }
        return ResponseEntity.ok(students);
    }

    // Get student by rollNo - allowed for USER and ADMIN
    @GetMapping("/{rollNo}")
    public ResponseEntity<?> getStudent(@PathVariable Long rollNo) {
        return studentRepository.findById(rollNo)
                .map(ResponseEntity::ok).orElseThrow();
    }

    // Update student - only ADMIN
    @PutMapping("/{rollNo}")

        public ResponseEntity<?> updateStudent(@PathVariable Long rollNo, @RequestBody Student updatedStudent) {
            // Find the student by id
            Student student = studentRepository.findById(rollNo).orElse(null);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
            }
            // Update username
            if (updatedStudent.getUsername() != null) {
                student.setUsername(updatedStudent.getUsername());
            }
            // Update password (encode it before saving)
            if (updatedStudent.getPassword() != null) {
                student.setPassword(passwordEncoder.encode(updatedStudent.getPassword()));
            }
            // Update roles
            if (updatedStudent.getRoles() != null && !updatedStudent.getRoles().isEmpty()) {
                student.setRoles(updatedStudent.getRoles());
            }
            // Save updated student
            studentRepository.save(student);

            return ResponseEntity.ok("Student updated successfully");
        }

        // Delete student - only ADMIN
    @DeleteMapping("/{rollNo}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long rollNo) {
        return studentRepository.findById(rollNo).map(student -> {
            studentRepository.delete(student);
            return ResponseEntity.ok("Student deleted");
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found"));
    }
}
