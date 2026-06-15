package AI_Study_Hub.repository;

import AI_Study_Hub.entity.InvalidatedtokenSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidationRespository extends JpaRepository<InvalidatedtokenSession, String> {
}
