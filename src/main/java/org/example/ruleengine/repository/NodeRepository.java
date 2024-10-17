package org.example.ruleengine.repository;

import org.example.ruleengine.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node, Long> {
    // Additional query methods can be defined here if needed
}
