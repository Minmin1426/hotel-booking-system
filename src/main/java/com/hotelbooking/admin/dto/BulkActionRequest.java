package com.hotelbooking.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkActionRequest {
    @NotEmpty(message = "At least one customer must be selected")
    private List<Long> customerIds;

    @NotNull(message = "Action is required")
    private String action; // SEND_NOTIFICATION, APPLY_TIER, APPLY_VOUCHER, LOCK_ACCOUNTS

    private Map<String, Object> payload;

    public List<Long> getCustomerIds() { return customerIds; }
    public void setCustomerIds(List<Long> customerIds) { this.customerIds = customerIds; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
