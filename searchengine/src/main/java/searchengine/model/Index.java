package searchengine.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;


@Entity
@Table(name = "`index`")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Immutable
@ToString
public class Index implements Comparable<Index> {
    @EmbeddedId
    private IndexKey id;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId("pageId")
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_INDEX_PAGE_ID"),
            name = "page_id", nullable = false)
    @ToString.Exclude
    private PageModel pageModel;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId("lemmaId")
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_INDEX_LEMMA_ID"),
            name = "lemma_id", nullable = false)
    private Lemma lemma;

    @Column(name = "`rank`", nullable = false)
    private Float rank;

    @Override
    public int compareTo(@NotNull Index o) {
        return Double.compare(o.rank, this.rank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Index index = (Index) o;
        return getId() != null && Objects.equals(getId(), index.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Embeddable
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IndexKey implements Serializable {
        @Column(name = "lemma_id")
        private Integer lemmaId;
        @Column(name = "page_id")
        private Integer pageId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
            IndexKey indexKey = (IndexKey) o;
            return getLemmaId() != null && Objects.equals(getLemmaId(), indexKey.getLemmaId())
                    && getPageId() != null && Objects.equals(getPageId(), indexKey.getPageId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(lemmaId, pageId);
        }
    }
}
