package info.kgeorgiy.ja.okorochkova.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {

    /** Gettters */

    private static <T> Stream<T> convertStream(final List<Student> students, final Function<Student, T> func) {
        return students.stream()
                .map(func);
    }

    private static <T> List<T> convertStreamToList(final List<Student> students, final Function<Student, T> func) {
        return convertStream(students, func)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return convertStreamToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return convertStreamToList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return convertStreamToList(students, Student::getGroup);

    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return convertStreamToList(students, stud -> String.format("%s %s", stud.getFirstName(), stud.getLastName()));
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return convertStream(students, Student::getFirstName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");

    }

    /** Sorters */

    private static final Comparator<Student> STUDENT_COMPARATOR =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName).reversed()
                    .thenComparing(Student::compareTo);

    private static List<Student> studentSorter(final Collection<Student> students,
                                               final Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return studentSorter(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return studentSorter(students, STUDENT_COMPARATOR);
    }

    /** Finders */

    private static <T> List<Student> studentFinder(final Collection<Student> students,
                                                   final Function<Student, T> func,
                                                   final T flag) {
        return students.stream()
                .filter(stud -> flag.equals(func.apply(stud)))
                .sorted(STUDENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return studentFinder(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return studentFinder(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return studentFinder(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final GroupName group) {
        return students.stream()
                .filter(stud -> group.equals(stud.getGroup()))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }
}
