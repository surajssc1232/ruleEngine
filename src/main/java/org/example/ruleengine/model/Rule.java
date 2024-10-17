package org.example.ruleengine.model;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "rules")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "root_node_id")
    private Node rootNode;
}
