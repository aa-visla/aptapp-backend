package com.creatix.domain.dto.tenant.subs;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel
@Data
public class UpdateSubTenantRequest {
    @ApiModelProperty(value = "First name", required = true)
    @NotEmpty
    private String firstName;

    @ApiModelProperty(value = "Last name", required = true)
    @NotEmpty
    private String lastName;

    @ApiModelProperty(value = "Phone number", required = true)
    @NotNull
    private String primaryPhone;

    @ApiModelProperty(value = "Company name")
    private String companyName;
}
