package com.github.hexabid.adapter.out.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Relacja manager-podwładny.
 * Używana przez authorized query do sprawdzania relacji DIRECT_SUBORDINATE.
 */
@Entity
@Table(name = "user_supervision")
@IdClass(UserSupervisionId.class)
public class UserSupervisionJpaEntity {

    @Id
    @Column(name = "manager_user_id", nullable = false, length = 64)
    private String managerUserId;

    @Id
    @Column(name = "subordinate_user_id", nullable = false, length = 64)
    private String subordinateUserId;

    public UserSupervisionJpaEntity() {
    }

    public UserSupervisionJpaEntity(String managerUserId, String subordinateUserId) {
        this.managerUserId = Objects.requireNonNull(managerUserId);
        this.subordinateUserId = Objects.requireNonNull(subordinateUserId);
    }

    public String getManagerUserId() {
        return managerUserId;
    }

    public void setManagerUserId(String managerUserId) {
        this.managerUserId = managerUserId;
    }

    public String getSubordinateUserId() {
        return subordinateUserId;
    }

    public void setSubordinateUserId(String subordinateUserId) {
        this.subordinateUserId = subordinateUserId;
    }

    /**
     * Composite key dla user_supervision.
     */
    public static class UserSupervisionId implements java.io.Serializable {
        private String managerUserId;
        private String subordinateUserId;

        public UserSupervisionId() {
        }

        public UserSupervisionId(String managerUserId, String subordinateUserId) {
            this.managerUserId = managerUserId;
            this.subordinateUserId = subordinateUserId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserSupervisionId that)) return false;
            return Objects.equals(managerUserId, that.managerUserId)
                    && Objects.equals(subordinateUserId, that.subordinateUserId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(managerUserId, subordinateUserId);
        }
    }
}
