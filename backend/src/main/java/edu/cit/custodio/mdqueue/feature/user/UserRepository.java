package edu.cit.custodio.mdqueue.feature.user;

import edu.cit.custodio.mdqueue.feature.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    java.util.List<User> findByRole(User.Role role);
    
    java.util.List<User> findByRoleAndIsApprovedTrue(User.Role role);
}
