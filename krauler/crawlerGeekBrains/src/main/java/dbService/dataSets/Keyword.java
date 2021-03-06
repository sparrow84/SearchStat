package dbService.dataSets;

import javax.persistence.*;

/**
 * Created by User on 22.12.2017.
 */

@Entity
@Table (name = "keywords")
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name="name", length = 2048)
    private String name;

    @ManyToOne
    @JoinColumn (name="person_id")
    private Person person;

    public Keyword() {
    }

    public Keyword(int id) {
        this.id = id;
    }

    public Keyword(Person person, String name) {
        this.name = name;
        this.person = person;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public String toString() {
        return "Keyword {" +
                "id=" + id +
                ", name='" + name +'\'' +
                "}";
    }
}
