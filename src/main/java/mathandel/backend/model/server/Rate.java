package mathandel.backend.model.server;


import mathandel.backend.model.server.enums.RateName;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "rates")
public class Rate {

    // todo pododawac size w TO
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User rater;

    private RateName rateName;

    @OneToOne
    private Result result;

    @Size(min = 4, max = 160)
    private String comment;

    public Long getId() {
        return id;
    }

    public Rate setId(Long id) {
        this.id = id;
        return this;
    }

    public User getRater() {
        return rater;
    }

    public Rate setRater(User rater) {
        this.rater = rater;
        return this;
    }

    public RateName getRateName() {
        return rateName;
    }

    public Rate setRateName(RateName rateName) {
        this.rateName = rateName;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Rate setComment(String comment) {
        this.comment = comment;
        return this;
    }


    public Result getResult() {
        return result;
    }

    public Rate setResult(Result result) {
        this.result = result;
        return this;
    }
}
