package com.example.chatbot.entity;
// package com.example.chatbot.entity;

// import java.time.LocalDateTime;
// import org.hibernate.annotations.UpdateTimestamp;
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// import lombok.AccessLevel;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;
// import lombok.experimental.FieldDefaults;

// @Entity
// @Table(name = "history_search")
// @Getter
// @Setter
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
// @FieldDefaults(level = AccessLevel.PRIVATE)
// public class HistorySearch {
//     @Id
//     @GeneratedValue(strategy = GenerationType.UUID)
//     @Column(columnDefinition = "CHAR(36)", updatable = false, nullable = false)
//     String id;

//     @Column(name = "content", length = 255, nullable = false)
//     String content;

//     @Column(name = "count", nullable = false)
//     int count;

//     @UpdateTimestamp // Tự động cập nhật trường này khi entity được update
//     @Column(name = "last_searched_at", nullable = false, insertable = false, updatable = false)
//     LocalDateTime lastSearchedAt;
// }
