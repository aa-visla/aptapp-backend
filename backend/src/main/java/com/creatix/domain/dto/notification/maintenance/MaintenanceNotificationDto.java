package com.creatix.domain.dto.notification.maintenance;

import com.creatix.domain.dto.apartment.BasicApartmentDto;
import com.creatix.domain.dto.notification.NotificationDto;
import com.creatix.domain.dto.property.slot.MaintenanceReservationDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceNotificationDto extends NotificationDto {
    @ApiModelProperty(value = "Target apartment", required = true)
    private BasicApartmentDto targetApartment;
    @ApiModelProperty(value = "Target apartment accessibility if tenant is not at home", required = true)
    private Boolean accessIfNotAtHome;
    @ApiModelProperty(value = "Maintenance reservation")
    private MaintenanceReservationDto reservation;
}
