package cf.mindaugas.sdajavaree16springboot.model;

import lombok.*;

import java.util.Objects;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Person {
    private String badgeID; // 000-XXXX-M
    private String name;
    private String surname;
}










class Employee {
    private String badgeID; // 000-XXXX-M
    private String name;
    private String surname;

    public Employee() {
    }

    public Employee(String badgeID, String name, String surname) {
        this.badgeID = badgeID;
        this.name = name;
        this.surname = surname;
    }

    public String getBadgeID() {
        return badgeID;
    }

    public void setBadgeID(String badgeID) {
        this.badgeID = badgeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "badgeID='" + badgeID + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(badgeID, employee.badgeID) && Objects.equals(name, employee.name) && Objects.equals(surname, employee.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(badgeID, name, surname);
    }
}
