package searchengine.model;


import lombok.*;

import javax.persistence.Index;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "page", indexes = @Index(name = "path_idx", columnList = "path, site_id", unique = true))
@ToString
public class PageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_PAGE_SITE_ID"),
            name = "site_id", referencedColumnName = "id")
    @ToString.Exclude
    private SiteModel site;

    @Column(name = "path", length = 200)
    private String path;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "content", nullable = false,
            columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;

    @Transient
    private String url;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "pageModels", cascade = CascadeType.MERGE)
    @ToString.Exclude
    private Set<Lemma> lemmas = new HashSet<>();
}
