package com.creatix.domain.dto.notification.maintenance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceNotificationResponseRequest {

    @ApiModel
    public enum ResponseType {
        Confirm, Reschedule
    }


    @ApiModelProperty(value = "Response", required = true)
    @NotNull
    private ResponseType response;
    @ApiModelProperty(value = "ID of the selected slot unit")
    private Long slotUnitId;
    @ApiModelProperty(value = "Maintenance employee note to reservation")
    private String note;
}
