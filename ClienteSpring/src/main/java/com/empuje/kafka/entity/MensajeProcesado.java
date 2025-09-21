package com.empuje.kafka.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_procesados", uniqueConstraints = {
        @UniqueConstraint(name = "uk_topic_part_offset", columnNames = { "topic", "partition_no", "offset_no" })
}, indexes = {
        @Index(name = "idx_msg_topic", columnList = "topic"),
        @Index(name = "idx_msg_partition", columnList = "partition_no"),
        @Index(name = "idx_msg_offset", columnList = "offset_no")
})
public class MensajeProcesado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic", length = 200, nullable = false)
    private String topic;

    @Column(name = "message_key", length = 200)
    private String messageKey;

    @Column(name = "partition_no", nullable = false)
    private Integer partitionNo;

    @Column(name = "offset_no", nullable = false)
    private Long offsetNo;

    // ⬇ Ajustado para que apunte a processed_at (NO created_at)
    @Column(name = "processed_at", insertable = false, updatable = false)
    private LocalDateTime processedAt;

    public MensajeProcesado() {
    }

    public MensajeProcesado(String topic, String messageKey, Integer partitionNo, Long offsetNo) {
        this.topic = topic;
        this.messageKey = messageKey;
        this.partitionNo = partitionNo;
        this.offsetNo = offsetNo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public Integer getPartitionNo() {
        return partitionNo;
    }

    public void setPartitionNo(Integer partitionNo) {
        this.partitionNo = partitionNo;
    }

    public Long getOffsetNo() {
        return offsetNo;
    }

    public void setOffsetNo(Long offsetNo) {
        this.offsetNo = offsetNo;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
