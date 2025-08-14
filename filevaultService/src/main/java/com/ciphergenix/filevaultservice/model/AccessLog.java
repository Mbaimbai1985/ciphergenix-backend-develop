package com.ciphergenix.filevaultservice.model;

import jakarta.persistence.*;
//import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String action; // "UPLOAD", "ENCRYPT", "DOWNLOAD", etc.
    private String ipAddress;

    private LocalDateTime timestamp;
}
