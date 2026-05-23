package edu.cit.custodio.mdqueue.feature.clinic;

import edu.cit.custodio.mdqueue.feature.clinic.Clinic;
import edu.cit.custodio.mdqueue.feature.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    List<Clinic> findByOwner(User owner);

    List<Clinic> findByNameContainingIgnoreCase(String name);
}
