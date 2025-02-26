package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBroadcastRequestDTO {
    private String title;
    private String description;
}