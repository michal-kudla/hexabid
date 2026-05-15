package com.github.hexabid.adapter.out.db;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key dla user_supervision (manager_user_id, subordinate_user_id).
 */
public class UserSupervisionId implements Serializable {

    private String managerUserId;
    private String subordinateUserId;

    public UserSupervisionId() {
    }

    public UserSupervisionId(String managerUserId, String subordinateUserId) {
        this.managerUserId = managerUserId;
        this.subordinateUserId = subordinateUserId;
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
