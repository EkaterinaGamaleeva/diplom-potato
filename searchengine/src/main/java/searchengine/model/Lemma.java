package searchengine.model;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Index;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "lemma", indexes = @Index(name = "lemma_idx", columnList = "lemma, site_id", unique = true))
@ToString
public class Lemma implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_LEMMA_SITE_ID"),
            name = "site_id", referencedColumnName = "id")
    @ToString.Exclude
    private SiteModel site;

    @Column(name = "lemma", nullable = false, length = 200)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(name = "index", foreignKey = @ForeignKey(name = "FK_LEMMAS_INDEX_ID"),
            joinColumns = {@JoinColumn(name = "lemma_id")},
            inverseJoinColumns = {@JoinColumn(name = "page_id")})
    @ToString.Exclude
    private Set<PageModel> pageModels = new HashSet<>();

    @Transient
    private Float rank;

    @Override
    public int compareTo(@NotNull Lemma o) {
        return Integer.compare(this.frequency, o.frequency);
    }
}
