package repository;


import entity.Charity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CharityRepository extends JpaRepository<Charity, UUID> {
    List<Charity> findByIsActiveTrue();
    List<Charity> findByIsFeaturedTrueAndIsActiveTrue();
    List<Charity> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}