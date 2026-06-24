package com.example.golf.dto.request;


import jakarta.validation.constraints.*;
        import lombok.Data;

@Data
public class DrawRequest {
    @NotNull
    @Min(1) @Max(12)
    private Integer drawMonth;

    @NotNull
    private Integer drawYear;

    // RANDOM or ALGORITHMIC
    private String drawType = "RANDOM";
}