package searchengine.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteModel, Integer> {
    Optional<SiteModel> findFirstByUrlIgnoreCase(String url);

    @NotNull
    List<SiteModel> findAll();
}
