package repository;

import entity.CharityContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CharityContributionRepository extends JpaRepository<CharityContribution, UUID> {
    List<CharityContribution> findByUserId(UUID userId);
    List<CharityContribution> findByCharityId(UUID charityId);

    @Query("SELECT SUM(c.amount) FROM CharityContribution c WHERE c.charityId = :charityId")
    BigDecimal sumAmountByCharityId(@Param("charityId") UUID charityId);

    @Query("SELECT SUM(c.amount) FROM CharityContribution c WHERE c.userId = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") UUID userId);
}