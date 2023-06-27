package searchengine.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Field;

@Repository
public interface FieldRepository extends CrudRepository<Field,Integer> {


}
