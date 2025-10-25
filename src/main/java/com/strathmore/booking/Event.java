package com.strathmore.booking;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "events") // Matches your PHP table
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // Good for potentially long descriptions
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate eventDate; // Use LocalDate for date only

    @Column(nullable = false)
    private LocalTime eventTime; // Use LocalTime for time only

    @Column(nullable = false)
    private String location;

    private String imagePath;
}
